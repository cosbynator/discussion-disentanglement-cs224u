require 'erb'
require 'java'

java_import "edu.stanford.cs224u.disentanglement.structures.DataSets"
MessageTree = Java::edu.stanford.cs224u.disentanglement.structures.MessageTree


class RenderTree
  attr_accessor :id
  attr_accessor :gold_tree
  attr_accessor :guess_tree
end


class Visualizer
  attr_reader :master_list
  attr_reader :node, :alt

  def initialize(output_file, gold_trees, guess_trees)
    @output_file = output_file
    @master_list = []
    @tree_node_erb = ERB.new(open("./code/src/data_visualizer_tree.erb.html").read)
    @data_visualizer_erb = ERB.new(open("./code/src/data_visualizer.erb.html").read)
    i = 0
    gold_trees.zip(guess_trees) do |gold_tree, guess_tree|
      rt = RenderTree.new
      rt.id = i
      rt.gold_tree = gold_tree
      rt.guess_tree = guess_tree
      @master_list << rt
      i += 1
    end
  end

  def render_tree_node(node, alt=false)
    old_node = node
    old_alt = alt
    @node = node
    @alt = alt
    ret = @tree_node_erb.result(get_binding)
    @node = old_node
    @alt = old_alt
    ret
  end

  def render_all
    res = @data_visualizer_erb.result(get_binding)
    open(@output_file, 'w') { |f| f.write(res)  }
    puts "Wrote to #{@output_file}"
  end

  def get_binding
    binding()
  end

end


if __FILE__ == $0
  Visualizer.new("visuals/last_run.html", DataSets::ASK_REDDIT_DEV.read, DataSets::ASK_REDDIT_DEV.read).render_all
end