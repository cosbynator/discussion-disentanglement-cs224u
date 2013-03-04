package edu.stanford.cs224u.disentanglement.evaluation;

import com.google.common.base.Objects;
import edu.stanford.cs224u.disentanglement.structures.*;
import edu.stanford.cs224u.disentanglement.util.Pair;

import java.util.Iterator;
import java.util.Set;

public class PairwiseF1Evaluator implements Evaluator {
    public class PairwiseF1Evaluation implements Evaluation {
        public final double precision;
        public final double recall;
        public final double f1;

        public PairwiseF1Evaluation(double precision, double recall) {
            this.precision = precision;
            this.recall = recall;
            this.f1 = 2 * precision * recall / (precision + recall);
        }

        public String toString() {
            return Objects.toStringHelper(this)
                    .add("f1", f1)
                    .add("precision", precision)
                    .add("recall", recall)
                    .toString();
        }
    }

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
    public PairwiseF1Evaluation getEvaluation() {
        double precision = correctPredictions / totalPredictions;
        double recall = correctRetrievals / totalRetrievals;
        return new PairwiseF1Evaluation(precision, recall);
    }
}
