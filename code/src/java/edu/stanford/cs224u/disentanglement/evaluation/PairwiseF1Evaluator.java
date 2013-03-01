package edu.stanford.cs224u.disentanglement.evaluation;

import edu.stanford.cs224u.disentanglement.structures.*;

import java.util.Iterator;
import java.util.Set;


public class PairwiseF1Evaluator implements Evaluator {

    public double evaluate(Iterable<MessageTree> targetTrees, Iterable<MessageTree> predictedTrees) {
        double correctPredictions = 0.0;
        double totalPredictions = 0.0;
        double correctRetrievals = 0.0;
        double totalRetrievals = 0.0;
        Iterator<MessageTree> predictedIterator = predictedTrees.iterator();
        for (MessageTree target : targetTrees) {
            Set<MessagePair> targetEdges = target.extractEdges();
            Set<MessagePair> predictedEdges = predictedIterator.next().extractEdges();
            for (MessagePair predictedEdge : predictedEdges) {
                if (targetEdges.contains(predictedEdge)) correctPredictions += 1.0;
                totalPredictions += 1.0;
            }
            for (MessagePair targetEdge : targetEdges) {
                if (predictedEdges.contains(targetEdge)) correctRetrievals += 1.0;
                totalRetrievals += 1.0;
            }
        }

        double precision = correctPredictions / totalPredictions;
        double recall = correctRetrievals / totalRetrievals;
        // Return F1
        return 2 * precision * recall / (precision + recall);

    }

}
