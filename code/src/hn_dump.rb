require 'java'
require 'json'
require 'set'
require 'zlib'
require 'ostruct'
require 'open-uri'
require 'fileutils'
require 'peach'
require 'sanitize'

CRAWL_DELAY = 4.0
TRAIN_SPLIT = 0.7
DEV_SPLIT = 0.1
TEST_SPLIT = 0.2

USERS_DUMP_FILE = "data/raw/AskHN/users.json"

java_import org.joda.time.DateTime
java_import org.joda.time.DateTimeZone
java_import java.io.ObjectOutputStream
java_import java.io.BufferedOutputStream
java_import java.io.FileOutputStream
java_import java.util.zip.GZIPOutputStream
java_import "edu.stanford.cs224u.disentanglement.structures.DataSets"

def walk(tree, parent=nil, depth=1, &block)
  block.call(tree, parent, depth)
  tree.getChildren().each do |child|
    walk(child, tree, depth+1, &block)
  end
end

class Array
  # File activesupport/lib/active_support/core_ext/array/grouping.rb, line 19
  def in_groups_of(number, fill_with = nil)
    if fill_with == false
      collection = self
    else
      # size % number gives how many extra we have;
      # subtracting from number gives how many to add;
      # modulo number ensures we don't add group of just fill.
      padding = (number - size % number) % number
      collection = dup.concat([fill_with] * padding)
    end

    if block_given?
      collection.each_slice(number) { |slice| yield(slice) }
    else
      groups = []
      collection.each_slice(number) { |group| groups << group }
      groups
    end
  end
end

class HNDump
  include_package "edu.stanford.cs224u.disentanglement.structures"

  def initialize
  end

  def annotator
    @annotator ||= MessageBodyAnnotator.new
  end

  def crawl_ids(ids)
    dump_file = lambda { |id| "data/raw/AskHN/#{id["_id"]}.json.gz"  }
    uniqs = (ids.uniq {|id| id["_id"]}).select{|id| (id["text"] || "").size >= 200 && id["num_comments"] < 100 && !File.exist?(dump_file.call(id))}
    puts "#{uniqs.size} to go"
    uniqs.each do |uniq|
      endpoint = "http://api.thriftdb.com/api.hnsearch.com/items/_search?limit=100&filter[fields][discussion.sigid]=#{uniq["_id"]}"
      begin
        open(endpoint,
             "User-Agent" => "Cosbynator's Discussion Disentanglement Project (Ruby/#{RUBY_VERSION})"
        ) do |api_f|
          str = api_f.read()
          Zlib::GzipWriter.open dump_file.call(uniq) do |f|
            f.write JSON.pretty_generate([uniq, JSON.parse(str)])
          end
          puts "Downloaded #{uniq}"
        end
      rescue => e
        FileUtils.rm(dump_file.call(uniq))
        puts "Error while crawling #{endpoint}: #{e}"
      end

      sleep CRAWL_DELAY
    end
  end

  def crawl_users(users)
    already_crawled = Set.new

    if File.exist?(USERS_DUMP_FILE)
      open(USERS_DUMP_FILER) do |f|
        f.each_line do |l|
          j = JSON.parse(l)
          already_crawled << j["item"]["username"]
        end
      end
    end


    to_crawl = users - already_crawled
    puts "Crawling #{to_crawl.size} users"

    open(USERS_DUMP_FILE, 'a') do |f|
      to_crawl.to_a.in_groups_of(90) do |users|
        endpoint = "http://api.thriftdb.com/api.hnsearch.com/users/_search?q=#{users.join('+OR+')}&limit=100"
        begin
          puts "Fetching #{endpoint}"
          open(endpoint,
               "User-Agent" => "Discussion Disentanglement Project (Ruby/#{RUBY_VERSION})"
          ) do |api_f|
            j = JSON.parse(api_f.read())
            j["results"].each do |result|
              f.write JSON.generate(result)
              f.write "\n"
            end

            f.flush

            puts "Wrote #{users.join ','}"
          end
        rescue => e
          puts "Error while crawling #{endpoint}: #{e}"
        end
        sleep CRAWL_DELAY
      end
    end
  end

  def parse_message_tree(json_gz, user_hash)
    response = Zlib::GzipReader.open(json_gz) { |f| JSON.parse(f.read()) }
    root_json, discussions_json = response

    root_title = root_json["title"]
    root_username = root_json["username"]
    root_text = Sanitize.clean(root_json["text"])
    root_sigid = root_json["_id"]
    num_comments = root_json["num_comments"]
    root_time = DateTime.parse(root_json["create_ts"]).with_zone(DateTimeZone::UTC)

    root = MessageNode.new(Message.new(root_sigid, root_username, root_time,
                                       "#{root_title} #{root_text}",
                                       annotator.annotateBody(root_text),
                                       user_hash[root_username]
                           ))

    comments = discussions_json["results"].map { |c| c["item"]}
    message_by_sigid = {root.message.id => root}
    comments.each do |comment|
      username = comment["username"]
      points = comment["points"]
      sigid = comment["_id"]
      text = Sanitize.clean(comment["text"])
      time = DateTime.parse(comment["create_ts"]).with_zone(DateTimeZone::UTC)

      begin
        message = MessageNode.new(Message.new(sigid, username, time, text, annotator.annotateBody(text), user_hash[username]))
        message_by_sigid[message.message.id] = message
      rescue => e
        puts "Error while creating node: #{e}"
      end
    end

    comments.each do |comment|
      sigid = comment["_id"]
      parent_id = comment["parent_sigid"]
      if(message_by_sigid[parent_id] && message_by_sigid[sigid])
        message_by_sigid[parent_id].add_children([message_by_sigid[sigid]].to_java(MessageNode))
      end
    end

    ret = MessageTree.new root, root_title
    ret.add_metadata "num_comments", num_comments
    ret
  end

  def read_ids(filename)
    ret = []
    open(filename) do |f|
      f.each_line do |line|
        ret << JSON.parse(line)["item"]
      end
    end
    ret
  end

  def make_dataset
    good_files = []
    Dir.glob("data/raw/AskHN/*.gz") do |filename|
      good_files << filename
    end

    users_hash = users_hash()

    good_files.shuffle!
    train_length = good_files.length * TRAIN_SPLIT
    dev_length = good_files.length * DEV_SPLIT
    test_length = good_files.length * TEST_SPLIT

    train_files = good_files[0, train_length]
    dev_files = good_files[train_length, dev_length]
    test_files = good_files[dev_length + train_length, good_files.length - train_length - dev_length + 1]

    base_dir = "data/AskHN"
    FileUtils.rmtree(base_dir)
    [[train_files, "train"], [dev_files, "dev"], [test_files, "test"]].each do |files, type|
      output_directory = "#{base_dir}/#{type}"
      FileUtils.mkdir_p(output_directory)

      files.peach(8) do |filename|
        puts "Reading #{filename}"
        tree = parse_message_tree(filename, users_hash)
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

  def extract_usernames(*datasets)
    users = Set.new
    datasets.each do |data|
      data.read.each do |mt|
        walk(mt.root) do |node, parent, depth|
          users << node.message.author_name
        end
      end
    end
    users
  end

  def users_hash
    message_users = {}
    open(USERS_DUMP_FILE) do |f|
      f.each_line do |line|
        j = JSON.parse(line)
        item = j["item"]
        username = item["username"]
        time = DateTime.parse(item["create_ts"]).with_zone(DateTimeZone::UTC)
        karma  = item["karma"]
        message_users[username] = MessageUser.new(username, karma, time)
      end
    end
    message_users
  end
end


if __FILE__ == $0
  dump = HNDump.new
  dump.parse_message_tree "data/raw/AskHN/127952-83197.json.gz", dump.users_hash()
  #dump.crawl_ids(dump.read_ids("data/raw/ask_hn_ids.json"))
  #dump.make_dataset
  #dump.crawl_users(dump.extract_usernames DataSets::ASK_HN_TRAIN, DataSets::ASK_HN_TEST, DataSets::ASK_HN_DEV)
  #puts dump.calc_average_karma(dump.users_hash.values)
end