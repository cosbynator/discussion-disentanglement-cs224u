package edu.stanford.cs224u.disentanglement.evaluation;

import edu.stanford.cs224u.disentanglement.structures.*;

import java.util.Set;

public class PairwiseF1Evaluator implements Evaluator {

    int correctPredictions;
    int totalPredictions;
    int correctRetrievals;
    int totalRetrievals;

    @Override
    public void addPrediction(MessageTree gold, MessageTree guess) {
        Set<MessagePair> targetEdges = gold.extractEdges();
        Set<MessagePair> predictedEdges = guess.extractEdges();
        for (MessagePair predictedEdge : predictedEdges) {
            if (targetEdges.contains(predictedEdge)) correctPredictions++;
            totalPredictions++;
        }
        for (MessagePair targetEdge : targetEdges) {
            if (predictedEdges.contains(targetEdge)) correctRetrievals++;
            totalRetrievals++;
        }

    }

    @Override
    public F1Evaluation getEvaluation() {
        double precision = (double) correctPredictions / totalPredictions;
        double recall = (double) correctRetrievals / totalRetrievals;
        return new F1Evaluation(precision, recall);
    }
}
