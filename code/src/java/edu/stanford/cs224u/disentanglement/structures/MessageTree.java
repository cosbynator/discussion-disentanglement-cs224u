package edu.stanford.cs224u.disentanglement.structures;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
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
        final List<Message> linearizedMessages = Lists.newArrayList();
        this.root.preorderWalk(new MessageNode.CopyToCollectionWalker(linearizedMessages));
        Collections.sort(linearizedMessages);
        return linearizedMessages;
    }

    public List<Set<Message>> getChildrenBags(int startDepth) {
        List<Set<Message>> childrenBags = Lists.newArrayList();
        this.root.preorderWalk(new MessageNode.BagifyChildrenWalker(childrenBags, startDepth));
        return childrenBags;
    }

    public MessageNode getRoot() {
        return root;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("root", root)
                .add("title", title)
                .add("metadata", metadata)
                .toString();
    }

    /**
     * Extract an immutable set of edges from the tree. An edge is represented by a <code>MessagePair</code> object.
     *
     * @return an immutable set of edges from the tree
     */
    public Set<MessagePair> extractEdges() {
        Stack<MessageNode> stack = new Stack<MessageNode>();
        stack.push(getRoot());
        ImmutableSet.Builder<MessagePair> builder = new ImmutableSet.Builder<MessagePair>();
        while (!stack.empty()) {
            MessageNode parent = stack.pop();
            for (MessageNode child : parent.getChildren()) {
                builder.add(new MessagePair(parent.getMessage(), child.getMessage()));
                stack.push(child);
            }
        }
        return builder.build();
    }
}
