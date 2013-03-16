package edu.stanford.cs224u.disentanglement.structures;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.*;

public class MessageNode implements Serializable {
    private static final long serialVersionUID = -3910005688986138369L;
    private final Message message;
    private final List<MessageNode> children;

    public interface TreeWalker {
        public void preorderVisit(MessageNode m, MessageNode parent, int depth);
    }

    public static class CopyVerticesWalker implements TreeWalker {
        private Collection<Message> coll;
        public CopyVerticesWalker(Collection<Message> coll) {
            this.coll = coll;
        }

        @Override
        public void preorderVisit(MessageNode m, MessageNode parent, int depth) {
            coll.add(m.getMessage());
        }
    }

    public static class CopyEdgesWalker implements TreeWalker {
        private Collection<MessagePair> coll;
        public CopyEdgesWalker(Collection<MessagePair> coll) {
            this.coll = coll;
        }

        @Override
        public void preorderVisit(MessageNode m, MessageNode parent, int depth) {
            coll.add(new MessagePair(parent.getMessage(), m.getMessage()));
        }
    }

    public static class BagifyChildrenWalker implements TreeWalker {
        private Collection<Set<Message>> bagCollection;
        private Set<Message> currentSet;
        private int startDepth;

        public BagifyChildrenWalker(Collection<Set<Message>> bagCollection) {
            this(bagCollection, 1);
        }

        public BagifyChildrenWalker(Collection<Set<Message>> bagCollection, int startDepth) {
            this.bagCollection = bagCollection;
            this.startDepth = startDepth;
        }

        @Override
        public void preorderVisit(MessageNode m, MessageNode parent, int depth) {
            if (depth >= startDepth) {
                if (depth == startDepth) {
                    currentSet = Sets.newHashSet();
                    bagCollection.add(currentSet);
                }
                currentSet.add(m.getMessage());
            }
        }
    }

    public MessageNode(MessageNode mn) {
        this.message = mn.getMessage();
        this.children = Lists.newArrayListWithCapacity(mn.getChildren().size());
        for(MessageNode child : mn.getChildren()) {
            this.children.add(new MessageNode(child));
        }
    }

    public MessageNode(Message m) {
        this.message = m;
        this.children = Lists.newArrayList();
    }

    public MessageNode(Message m, List<MessageNode> children) {
        this(m);
        addChildren(children);
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
        return Collections.unmodifiableList(children);
    }

    public void preorderWalk(TreeWalker walker) {
        preorderWalk(walker, null, 0);
    }

    public void preorderWalk(TreeWalker walker, MessageNode parent, int depth) {
        walker.preorderVisit(this, parent, depth);
        for(MessageNode n : children) {
            if(n != null) {
                n.preorderWalk(walker, this, depth + 1);
            }
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