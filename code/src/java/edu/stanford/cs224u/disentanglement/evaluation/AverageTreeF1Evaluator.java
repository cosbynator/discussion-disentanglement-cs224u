package edu.stanford.cs224u.disentanglement.evaluation;

import edu.stanford.cs224u.disentanglement.structures.*;

import java.util.Iterator;
import java.util.Set;


public class AverageTreeF1Evaluator implements Evaluator {

    public double evaluate(Iterable<MessageTree> targetTrees, Iterable<MessageTree> predictedTrees) {
        Iterator<MessageTree> predictedIterator = predictedTrees.iterator();
        double totalF1 = 0.0;
        int treeCount = 0;
        for (MessageTree target : targetTrees) {
            double correctPredictions = 0.0;
            double totalPredictions = 0.0;
            double correctRetrievals = 0.0;
            double totalRetrievals = 0.0;
            Set<MessagePair> targetEdges = target.extractEdges();
            // Assumes predictedTrees has the same number of elements as targetTrees
            Set<MessagePair> predictedEdges = predictedIterator.next().extractEdges();
            for (MessagePair predictedEdge : predictedEdges) {
                if (targetEdges.contains(predictedEdge)) correctPredictions += 1.0;
                totalPredictions += 1.0;
            }
            for (MessagePair targetEdge : targetEdges) {
                if (predictedEdges.contains(targetEdge)) correctRetrievals += 1.0;
                totalRetrievals += 1.0;
            }
            double precision = correctPredictions / totalPredictions;
            double recall = correctRetrievals / totalRetrievals;
            totalF1 += 2 * precision * recall / (precision + recall);
            treeCount++;
        }
        return totalF1 / treeCount;
    }

}
