package edu.stanford.cs224u.disentanglement;

import edu.stanford.cs224u.disentanglement.evaluation.*;
import edu.stanford.cs224u.disentanglement.learners.*;
import edu.stanford.cs224u.disentanglement.structures.DataSets;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Test {
    public static void main(String[] args) throws FileNotFoundException, ScriptException {
         testBaseline(5);
    }

    public static void testBaseline(int numTrees) {
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

    public static void testPrintData() throws FileNotFoundException, ScriptException {
        for(MessageTree tree : DataSets.ASK_REDDIT_TRAIN.read()) {
            System.out.println("****");
            System.out.println(tree);
            for(Message m : tree.linearize()) {
                System.out.println("\t" + m);
            }
        }
    }
}
