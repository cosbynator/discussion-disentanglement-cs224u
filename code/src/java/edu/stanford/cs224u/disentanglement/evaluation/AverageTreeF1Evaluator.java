package edu.stanford.cs224u.disentanglement.evaluation;

import com.google.common.base.Objects;
import edu.stanford.cs224u.disentanglement.structures.*;


public class AverageTreeF1Evaluator implements Evaluator {

    public static final String AVERAGE_F1_METRIC = "averageF1";

    double totalF1 = 0.0;
    int treeCount = 0;

    @Override
    public void addPrediction(MessageTree gold, MessageTree guess) {
        PairwiseF1Evaluator evaluator = new PairwiseF1Evaluator();
        evaluator.addPrediction(gold,guess);
        treeCount++;
        totalF1 += evaluator.getEvaluation().getMetric(PairwiseF1Evaluator.F1_METRIC);
    }

    @Override
    public EvaluationResult getEvaluation() {
        EvaluationResult.Builder builder = new EvaluationResult.Builder("AveragePairwiseF1");
        builder.addMetric(AVERAGE_F1_METRIC, totalF1 / treeCount);
        return builder.build();
    }

}
