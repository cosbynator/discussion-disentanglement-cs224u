package edu.stanford.cs224u.disentanglement.evaluation;

import com.google.common.collect.Lists;
import edu.stanford.cs224u.disentanglement.structures.MessageNode;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;

import java.util.List;
import java.util.Set;

public class UnderRootF1Evaluator implements Evaluator {
    PairwiseF1Evaluator underRootEvaluator = new PairwiseF1Evaluator();

    @Override
    public void addPrediction(MessageTree gold, MessageTree guess) {
        underRootEvaluator.addPrediction(underRootTree(gold), underRootTree(guess));
    }

    private MessageTree underRootTree(MessageTree mt) {
        MessageNode n = mt.getRoot();
        MessageNode root = new MessageNode(n.getMessage());
        for(MessageNode c : n.getChildren()) {
            root.addChildren(new MessageNode(c.getMessage()));
        }

        return new MessageTree(root, mt.getTitle());
    }

    @Override
    public Evaluation getEvaluation() {
        F1Evaluation ret = underRootEvaluator.getEvaluation();
        ret.setName("UnderRootF1Evaluation");
        return ret;
    }
}
