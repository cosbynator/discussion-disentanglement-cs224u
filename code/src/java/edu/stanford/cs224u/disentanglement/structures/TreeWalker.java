package edu.stanford.cs224u.disentanglement.structures;

public interface TreeWalker {
    void preorderVisit(MessageNode m, MessageNode parent, int depth);
}
