package edu.stanford.cs224u.disentanglement.evaluation;

import edu.stanford.cs224u.disentanglement.structures.*;

import java.util.Set;

public class PairwiseF1Evaluator implements Evaluator {

    double correctPredictions = 0.0;
    double totalPredictions = 0.0;
    double correctRetrievals = 0.0;
    double totalRetrievals = 0.0;

    @Override
    public void addPrediction(MessageTree gold, MessageTree guess) {
        Set<MessagePair> targetEdges = gold.extractEdges();
        Set<MessagePair> predictedEdges = guess.extractEdges();
        for (MessagePair predictedEdge : predictedEdges) {
            if (targetEdges.contains(predictedEdge)) correctPredictions += 1.0;
            totalPredictions += 1.0;
        }
        for (MessagePair targetEdge : targetEdges) {
            if (predictedEdges.contains(targetEdge)) correctRetrievals += 1.0;
            totalRetrievals += 1.0;
        }

    }

    @Override
    public F1Evaluation getEvaluation() {
        double precision = correctPredictions / totalPredictions;
        double recall = correctRetrievals / totalRetrievals;
        return new F1Evaluation(precision, recall);
    }
}
