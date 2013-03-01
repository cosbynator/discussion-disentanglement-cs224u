package edu.stanford.cs224u.disentanglement.evaluation;

import edu.stanford.cs224u.disentanglement.structures.*;


public interface Evaluator {
    public double evaluate(Iterable<MessageTree> target, Iterable<MessageTree> predicted);
}
