require 'net/http'
require 'uri'
require 'json'
require 'fileutils'
require 'zlib'

require 'java'
java_import org.joda.time.DateTime
java_import org.joda.time.DateTimeZone
java_import "edu.stanford.cs224u.disentanglement.structures.Message"
java_import java.io.ObjectOutputStream
java_import java.io.BufferedOutputStream
java_import java.io.FileOutputStream
java_import java.util.zip.GZIPOutputStream

DATA_DIR = "data"
RAW_DUMP_DIR = "#{DATA_DIR}/raw"
TEST_DIR = "#{DATA_DIR}/test"

REDDIT_CRAWL_RATE = 25.0 / 60.0 #25 requests every minute

class RedditDump
  include_package "edu.stanford.cs224u.disentanglement.structures"

  HTTP_PARAMS = {'User-Agent' => 'reddit-dientanglement-bot by cosbynator'}

  TRAIN_SPLIT = 0.7
  DEV_SPLIT = 0.1
  TEST_SPLIT = 0.2

  def initialize
    @http = Net::HTTP.new("www.reddit.com", 80)
    @http.read_timeout = 100
    @annotator = MessageBodyAnnotator.new
  end

  def split_raw_subreddit(subreddit)
    good_files = []
    Dir.glob("#{RAW_DUMP_DIR}/#{subreddit}/*.gz") do |filename|
      tree = Zlib::GzipReader.open(filename) { |f| conversation_metadata(JSON.parse(f.read())[0]) }
      #Check some properties to filter out if we want
      puts "Checking #{filename}"
      if true
        good_files << filename
      end
    end

    raise "No files found for #{subreddit}" if good_files.empty?
    good_files.shuffle!
    train_length = good_files.length * TRAIN_SPLIT
    dev_length = good_files.length * DEV_SPLIT
    test_length = good_files.length * TEST_SPLIT

    train_files = good_files[0, train_length]
    dev_files = good_files[train_length, dev_length]
    test_files = good_files[dev_length + train_length, good_files.length - train_length - dev_length + 1]

    puts "Splitting #{good_files.length} message threads into"
    puts "\tTrain #{train_files.length}"
    puts "\tDev #{dev_files.length}"
    puts "\tTest #{test_files.length}"

    subreddit_dir = "#{DATA_DIR}/#{subreddit}"
    FileUtils.rmtree(subreddit_dir)
    puts "Cleaning #{subreddit_dir}"
    [[train_files, "train"], [dev_files, "dev"], [test_files, "test"]].each do |files, type|
      output_directory = "#{subreddit_dir}/#{type}"
      FileUtils.mkdir_p(output_directory)
      files.each do |filename|
        puts "Reading #{filename}"
        tree = Zlib::GzipReader.open(filename) { |f| response_to_tree(JSON.parse(f.read())) }
        output_name = "#{output_directory}/#{File.basename(filename, '.json.gz')}.dmt.gz"
        output_stream = ObjectOutputStream.new(GZIPOutputStream.new(BufferedOutputStream.new(FileOutputStream.new(output_name))))
        begin
          output_stream.writeObject(tree)
        ensure
          output_stream.close()
        end
        puts "Wrote #{output_name}"
      end
    end
  end

  def fetch_listing(subreddit, after=nil)
    operation = "/r/#{subreddit}/top.json?sort=top&t=month"
    operation += "&after=#{after}" if after
    puts "Fetching #{operation}"
    req = Net::HTTP::Get.new(operation, HTTP_PARAMS)
    response = @http.request(req)
    puts "Response code #{response.code}"
    raise "Bad response code #{response.code}" if response.code.to_i != 200
    JSON.parse response.body
  end

  def fetch_thread(id)
    req = Net::HTTP::Get.new("/comments/#{id}.json?depth=9", HTTP_PARAMS)
    puts "Fetching #{id}"
    response = @http.request(req)

    puts "Response code #{response.code}"
    raise "Bad response code #{response.code}" if response.code.to_i != 200
    JSON.parse response.body
  end

  def listing_ids_after(listing_json)
    raise "Wrong kind of listing" unless listing_json["kind"] == "Listing"
    listing_data = listing_json["data"]
    after = listing_data["after"]
    ids = listing_data["children"].map do |child|
      child["data"]["id"]
    end
    [ids, after]
  end

  def message_tree(root_json)
    raise "Wrong root kind #{root_json["kind"]}" unless root_json["kind"] == "Listing"
    children = root_json["data"]["children"]
    raise "Not the right amount of children" if children.length != 1

    child = children.first
    child_data = child["data"]

    title =  child_data["title"]
    author = child_data["author"]
    body = child_data["selftext"]
    id = child_data["id"]
    date = DateTime.new(child_data["created_utc"] * 1000, DateTimeZone::UTC)

    root = MessageNode.new(Message.new(id, author, date, body, @annotator.annotateBody(body)))
    ret = MessageTree.new root, title
    ret.add_metadata "subreddit", child_data["subreddit"]
    ret.add_metadata "score", child_data["score"]
    ret.add_metadata "num_comments", child_data["num_comments"]
    ret
  end

  def conversation_metadata(root_json)
    raise "Wrong root kind #{root_json["kind"]}" unless root_json["kind"] == "Listing"
    children = root_json["data"]["children"]
    raise "Not the right amount of children" if children.length != 1

    child = children.first
    child_data = child["data"]
    child_data
  end

  def message_nodes(msg_json)
    raise "Wrong root kind #{msg_json["kind"]}" unless msg_json["kind"] == "Listing"
    msg_json["data"]["children"].map do |child|
      kind = child["kind"]
      if kind != "t1"
        nil
      else
        data = child["data"]
        id = data["id"]
        author = data["author"]
        date = DateTime.new(data["created_utc"] * 1000, DateTimeZone::UTC)
        body = data["body"]

        root  = MessageNode.new(Message.new(id, author, date, body, @annotator.annotateBody(body)))
        root.addChildren(message_nodes(data["replies"]).to_java(MessageNode)) unless data["replies"].empty?
        root
      end
    end.compact
  end

  def response_to_tree(response)
    tree = message_tree(response[0])
    tree.root.add_children(message_nodes(response[1]))
    tree
  end

  def dump_subreddit(subreddit)
    puts "Dumping #{subreddit}"

    FileUtils.mkdir_p("#{RAW_DUMP_DIR}/#{subreddit}")
    after = nil
    while true
      listing_tries = 0
      begin
        listing_tries += 1
        sleep (1.0/REDDIT_CRAWL_RATE)
        discussion_ids, after = listing_ids_after(fetch_listing(subreddit, after))
      rescue => e
        puts "Error while fetching listing after #{after}: #{e}"
        retry if listing_tries <= 3
        raise e
      end

      discussion_ids.each do |discussion_id|
        dump_file = "#{RAW_DUMP_DIR}/#{subreddit}/#{discussion_id}.json.gz"
        if File.exist? dump_file
          puts "Skipping #{discussion_id} since we already dumped it"
        else
          tries = 0
          begin
            tries += 1
            sleep (1.0/REDDIT_CRAWL_RATE)
            thread = fetch_thread discussion_id
          rescue => e
            puts "Error while fetching #{discussion_id}: #{e}"
            retry if tries <= 3
            raise e
          end

          Zlib::GzipWriter.open dump_file do |f|
            f.write JSON.pretty_generate(thread)
          end
          puts "Wrote #{dump_file}"
        end
      end

      break unless after
    end
  end

  def run_test
    #dump_subreddit("AskReddit")
    #m = Message.new "id", "authorName", DateTime.new(), "body"
    #thread = fetch_thread "sm9lb"
    #File.open "#{TEST_DIR}/test.json", 'w' do |f|
    #  f.write JSON.pretty_generate(thread)
    #end
    #t = response_to_tree(JSON.parse(File.open("#{TEST_DIR}/test.json") { |f| f.read }))
    #puts t
  end
end


#RedditDump.new.dump_subreddit "AskReddit"
RedditDump.new.split_raw_subreddit "AskReddit"


