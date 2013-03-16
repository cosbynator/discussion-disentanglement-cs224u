package edu.stanford.cs224u.disentanglement.structures;

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

public class TreeWalkers {
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
        private int maxDepth;

        public CopyEdgesWalker(Collection<MessagePair> coll) {
            this(coll, -1);
        }

        public CopyEdgesWalker(Collection<MessagePair> coll, int maxDepth) {
            this.coll = coll;
            this.maxDepth = maxDepth;
        }

        @Override
        public void preorderVisit(MessageNode m, MessageNode parent, int depth) {
            if (parent != null && (maxDepth < 0 || depth <= maxDepth)) {
                coll.add(new MessagePair(parent.getMessage(), m.getMessage()));
            }
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
}
