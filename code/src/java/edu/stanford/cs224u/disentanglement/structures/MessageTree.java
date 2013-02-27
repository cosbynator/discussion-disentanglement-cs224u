package edu.stanford.cs224u.disentanglement.structures;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.*;

public class MessageTree implements Serializable {
    private static final long serialVersionUID = -3368739921774391871L;
    private final MessageNode root;
    private final String title;

    private final Map<String, Object> metadata;

    public MessageTree(MessageNode root, String title) {
        this.root = root;
        this.title = title;
        metadata = Maps.newHashMap();
    }

    public void addMetadata(String name, Object value) {
        metadata.put(name, value);
    }

    public List<Message> linearize() {
        final List<Message> ret = Lists.newArrayList();
        this.root.walk(new Function<MessageNode, Void>() {
            @Override
            public Void apply(MessageNode messageNode) {
                ret.add(messageNode.getMessage());
                return null;
            }
        });
        Collections.sort(ret, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });
        return ret;
    }

    public MessageNode getRoot() {
        return root;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("root", root)
                .add("title", title)
                .add("metadata", metadata)
                .toString();
    }
    public Set<MessagePair> extractEdges() {
        Stack<MessageNode> stack = new Stack<MessageNode>();
        Set<MessagePair> edges = new HashSet<MessagePair>();
        stack.push(getRoot());
        while (!stack.empty()) {
            MessageNode parent = stack.pop();
            for (MessageNode child : parent.getChildren()) {
                edges.add(new MessagePair(parent.getMessage(), child.getMessage()));
                stack.push(child);
            }
        }
        return edges;
    }

}
