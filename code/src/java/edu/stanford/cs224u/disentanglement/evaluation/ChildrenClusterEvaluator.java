package edu.stanford.cs224u.disentanglement.evaluation;

import edu.stanford.cs224u.disentanglement.structures.MessageTree;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.nlp.util.Sets;

import java.util.List;
import java.util.Set;

public class ChildrenClusterEvaluator implements Evaluator {
    private static final int MAX_DEPTH = 4;

    private double totalF1[];
    private int numTrees[];

    @Override
    public void addPrediction(MessageTree gold, MessageTree guess) {
        totalF1 = new double[MAX_DEPTH];
        numTrees = new int[MAX_DEPTH];
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

            double precision = b3PrecisionTotal / numElements;
            double recall = b3RecallTotal / numElements;
            totalF1[depth - 1] += 2 * precision * recall / (precision + recall);
            numTrees[depth - 1]++;
        }
    }

    @Override
    public EvaluationResult getEvaluation() {
        EvaluationResult.Builder builder = new EvaluationResult.Builder("ChildrenClusterB3");
        for (int i = 0; i < MAX_DEPTH; i++) {
            if (numTrees[i] == 0) {
                continue;
            }
            builder.addMetric(String.format("b3Depth%d", i + 1), totalF1[i] / numTrees[i]);
        }
        return builder.build();
    }
}
