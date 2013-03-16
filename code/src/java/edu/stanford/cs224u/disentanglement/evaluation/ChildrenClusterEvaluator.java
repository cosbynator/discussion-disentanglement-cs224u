package edu.stanford.cs224u.disentanglement.evaluation;

import edu.stanford.cs224u.disentanglement.structures.MessageTree;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.nlp.util.Sets;

import java.util.List;
import java.util.Set;

public class ChildrenClusterEvaluator implements Evaluator {
    private static final int MAX_DEPTH = 4;
    public static final String B3_DEPTH_METRIC_PREFIX = "b3Depth";
    public static final String F1_DEPTH_METRIC_PREFIX = "f1Depth";

    private double f1[];
    private double bagF1[];
    private int numTrees[];

    public ChildrenClusterEvaluator() {
        bagF1 = new double[MAX_DEPTH];
        f1 = new double[MAX_DEPTH];
        numTrees = new int[MAX_DEPTH];
    }

    @Override
    public void addPrediction(MessageTree gold, MessageTree guess) {
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            List<Set<Message>> referencePartition = gold.getChildrenBags(depth);
            List<Set<Message>> responsePartition = guess.getChildrenBags(depth);

            if (referencePartition.isEmpty() || responsePartition.isEmpty()) {
                break;
            }

            double b3PrecisionTotal = 0;
            double b3RecallTotal = 0;
            int numElements = 0;
            for (Set<Message> refSet : referencePartition) {
                numElements += refSet.size();
                for (Message m : refSet) {
                    for (Set<Message> respSet : responsePartition) {
                        if (respSet.contains(m)) {
                            int intersectionSize = Sets.intersection(refSet, respSet).size();
                            b3PrecisionTotal += (double) intersectionSize / respSet.size();
                            b3RecallTotal += (double) intersectionSize / refSet.size();
                        }
                    }
                }
            }

            // Calculate F1 score till that depth
            Evaluator eval = new PairwiseF1Evaluator(depth);
            eval.addPrediction(gold, guess);
            EvaluationResult result = eval.getEvaluation();

            double precision = b3PrecisionTotal / numElements;
            double recall = b3RecallTotal / numElements;
            double b3f1 = 2 * precision * recall / (precision + recall);
            if (!Double.isNaN(b3f1)) {
                f1[depth - 1] += result.getMetric(PairwiseF1Evaluator.F1_METRIC);
                bagF1[depth - 1] += b3f1;
                numTrees[depth - 1]++;
            }
        }
    }

    @Override
    public EvaluationResult getEvaluation() {
        EvaluationResult.Builder builder = new EvaluationResult.Builder("ChildrenClusterB3");
        for (int i = 0; i < MAX_DEPTH; i++) {
            if (numTrees[i] > 0) {
                builder.addMetric(String.format(B3_DEPTH_METRIC_PREFIX + "%d", i + 1),
                        bagF1[i] / numTrees[i]);
                builder.addMetric(String.format(F1_DEPTH_METRIC_PREFIX + "%d", i + 1),
                        f1[i] / numTrees[i]);
            }
        }
        return builder.build();
    }
}
