package edu.stanford.cs224u.disentanglement.structures;

import com.google.common.collect.Lists;

import java.util.List;

public class MessageTree {
    private final Message node;
    private final List<Message> children;

    public MessageTree(Message m) {
        this.node = m;
        this.children = Lists.newArrayList();
    }

    public MessageTree(Message m, List<Message> messages) {
        this(m);
        addChildren(messages);
    }

    public void addChildren(List<Message> messages) {
        this.children.addAll(messages);
    }

    public void addChildren(Message... messages) {
        for (Message m : messages) {
            children.add(m);
        }
    }

    public Message getNode() {
        return node;
    }

    public List<Message> getChildren() {
        return children;
    }
}