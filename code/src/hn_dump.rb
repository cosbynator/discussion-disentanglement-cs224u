require 'java'
require 'json'
require 'zlib'
require 'ostruct'
require 'open-uri'
require 'fileutils'
require 'peach'

CRAWL_DELAY = 4.0
TRAIN_SPLIT = 0.7
DEV_SPLIT = 0.1
TEST_SPLIT = 0.2

java_import org.joda.time.DateTime
java_import org.joda.time.DateTimeZone
java_import java.io.ObjectOutputStream
java_import java.io.BufferedOutputStream
java_import java.io.FileOutputStream
java_import java.util.zip.GZIPOutputStream

class String
  def strip_tags
    self.gsub( %r{</?[^>]+?>}, '' )
  end
end

class HNDump
  include_package "edu.stanford.cs224u.disentanglement.structures"

  def initialize
    @annotator = MessageBodyAnnotator.new
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


  def parse_message_tree(json_gz)
    response = Zlib::GzipReader.open(json_gz) { |f| JSON.parse(f.read()) }
    root_json, discussions_json = response

    root_title = root_json["title"]
    root_username = root_json["username"]
    root_text = root_json["text"].strip_tags
    root_sigid = root_json["_id"]
    num_comments = root_json["num_comments"]
    p root_json
    root_time = DateTime.parse(root_json["create_ts"]).with_zone(DateTimeZone::UTC)

    root = MessageNode.new(Message.new(root_sigid, root_username, root_time, "#{root_title} #{root_text}", @annotator.annotateBody(root_text)))

    comments = discussions_json["results"].map { |c| c["item"]}
    message_by_sigid = {root.message.id => root}
    comments.each do |comment|
      username = comment["username"]
      points = comment["points"]
      sigid = comment["_id"]
      text = comment["text"]
      time = DateTime.parse(comment["create_ts"]).with_zone(DateTimeZone::UTC)

      begin
        message = MessageNode.new(Message.new(sigid, username, time, text, @annotator.annotateBody(text)))
        message_by_sigid[message.message.id] = message
      rescue => e
        puts "Error while creating node: #{e}"
      end
    end

    comments.each do |comment|
      sigid = comment["_id"]
      parent_id = comment["parent_sigid"]
      if(message_by_sigid[parent_id])
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
        tree = parse_message_tree(filename)
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
end


if __FILE__ == $0
  dump = HNDump.new
  #dump.parse_message_tree "data/raw/AskHN/127952-83197.json.gz"
  #dump.crawl_ids(dump.read_ids("data/raw/ask_hn_ids.json"))
  dump.make_dataset
end