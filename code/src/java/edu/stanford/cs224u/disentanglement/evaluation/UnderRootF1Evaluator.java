package edu.stanford.cs224u.disentanglement.evaluation;

import edu.stanford.cs224u.disentanglement.structures.MessageNode;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;

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
    public EvaluationResult getEvaluation() {
        EvaluationResult ret = underRootEvaluator.getEvaluation();
        EvaluationResult.Builder builder = new EvaluationResult.Builder("UnderRootF1Result", ret);
        return builder.build();
    }
}
