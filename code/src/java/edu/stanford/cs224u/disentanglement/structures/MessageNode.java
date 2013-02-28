package edu.stanford.cs224u.disentanglement.structures;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class MessageNode implements Serializable {
    private static final long serialVersionUID = -3910005688986138369L;
    private final Message message;
    private final List<MessageNode> children;

    public MessageNode(Message m) {
        this.message = m;
        this.children = Lists.newArrayList();
    }

    public MessageNode(Message m, List<MessageNode> children) {
        this(m);
        addChildren(children);
    }

    // TODO: Need to remove this and integrate with constructor in order to keep the whole thing immutable
    public void addChildren(List<MessageNode> messages) {
        this.children.addAll(messages);
    }

    // TODO: Need to remove this and integrate with constructor in order to keep the whole thing immutable
    public void addChildren(MessageNode... messages) {
        for (MessageNode m : messages) {
            children.add(m);
        }
    }

    public Message getMessage() {
        return message;
    }

    public List<MessageNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void walk(Function<MessageNode, Void> walker) {
        walker.apply(this);
        for(MessageNode n : children) {
            n.walk(walker);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("message", message)
                .add("numChildren", children.size())
                .toString();
    }
}