package edu.stanford.cs224u.disentanglement.baselines;

import edu.stanford.cs224u.disentanglement.evaluation.AverageTreeF1Evaluator;
import edu.stanford.cs224u.disentanglement.evaluation.Evaluator;
import edu.stanford.cs224u.disentanglement.learners.AllReplyToOpBaselineLearner;
import edu.stanford.cs224u.disentanglement.learners.Learner;
import edu.stanford.cs224u.disentanglement.structures.DataSets;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;

import java.util.ArrayList;

public class BasicBaseline {
    public static void runBaseline() {
        int numTrees = 5;
        ArrayList<MessageTree> targetTrees = new ArrayList<MessageTree>();
        int i = 0;
        for (MessageTree instance : DataSets.ASK_REDDIT_TEST.read()) {
            if (i >= numTrees) break;
            targetTrees.add(instance);
            i++;
        }

        Learner learner = new AllReplyToOpBaselineLearner();
        // The baselines don't do anything with the training data for now, but for good measure
        learner.learn(DataSets.ASK_REDDIT_TRAIN.read());
        Iterable<MessageTree> predictions = learner.LinearizeAndPredictMany(targetTrees);
        Evaluator evaluator = new AverageTreeF1Evaluator();
        double score = evaluator.evaluate(targetTrees, predictions);
        System.out.println("Baseline F1: " + score);
    }
}