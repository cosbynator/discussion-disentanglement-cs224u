package edu.stanford.cs224u.disentanglement.evaluation;

import com.google.common.collect.Lists;
import edu.stanford.cs224u.disentanglement.structures.MessageNode;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;

import java.util.List;
import java.util.Set;

public class UnderRootF1Evaluator implements Evaluator {
    PairwiseF1Evaluator underRootEvaluator = new PairwiseF1Evaluator();
    PairwiseF1Evaluator largeDepthEvaluator = new PairwiseF1Evaluator();

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

    private List<MessageTree> largeDepthTrees(MessageTree mt) {
        List<MessageTree> ret = Lists.newArrayList();
        for(MessageNode n : mt.getRoot().getChildren()) {
            MessageNode root = new MessageNode(n.getMessage());
            for(MessageNode c : n.getChildren()) {
                root.addChildren(new MessageNode(c));
            }
            ret.add(new MessageTree(root, mt.getTitle()));
        }

        return ret;
    }


    @Override
    public Evaluation getEvaluation() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
