package edu.stanford.cs224u.disentanglement.evaluation;

import edu.stanford.cs224u.disentanglement.structures.*;

import java.util.Set;

public class PairwiseF1Evaluator implements Evaluator {

    public static final String PRECISION_METRIC = "precision";
    public static final String RECALL_METRIC = "recall";
    public static final String F1_METRIC = "f1";

    private final int maxDepth;

    int correctPredictions;
    int totalPredictions;
    int correctRetrievals;
    int totalRetrievals;

    public PairwiseF1Evaluator() {
        this(-1);
    }

    public PairwiseF1Evaluator(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public void addPrediction(MessageTree gold, MessageTree guess) {
        Set<MessagePair> targetEdges;
        Set<MessagePair> predictedEdges;
        if (maxDepth > 0) {
            targetEdges = gold.extractEdges(maxDepth);
            predictedEdges = guess.extractEdges(maxDepth);
        } else {
            targetEdges = gold.extractEdges();
            predictedEdges = guess.extractEdges();
        }
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
    public EvaluationResult getEvaluation() {
            double precision = (double) correctPredictions / totalPredictions;
            double recall = (double) correctRetrievals / totalRetrievals;
        EvaluationResult.Builder builder = new EvaluationResult.Builder("F1Result");
        builder.addMetric(PRECISION_METRIC, precision);
        builder.addMetric(RECALL_METRIC, recall);
        builder.addMetric(F1_METRIC, 2 * precision * recall / (precision + recall));
        return builder.build();
    }
}
