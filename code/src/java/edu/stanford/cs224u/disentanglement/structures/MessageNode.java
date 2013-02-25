package edu.stanford.cs224u.disentanglement.structures;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

public class MessageNode implements Serializable {
    private final Message message;
    private final List<MessageNode> children;

    public MessageNode(Message m) {
        this.message = m;
        this.children = Lists.newArrayList();
    }

    public MessageNode(Message m, List<MessageNode> messages) {
        this(m);
        addChildren(messages);
    }

    public void addChildren(List<MessageNode> messages) {
        this.children.addAll(messages);
    }

    public void addChildren(MessageNode... messages) {
        for (MessageNode m : messages) {
            children.add(m);
        }
    }

    public Message getMessage() {
        return message;
    }

    public List<MessageNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("message", message)
                .add("numChildren", children.size())
                .toString();
    }

}