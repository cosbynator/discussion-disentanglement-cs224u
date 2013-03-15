package edu.stanford.cs224u.disentanglement.evaluation;

import edu.stanford.cs224u.disentanglement.structures.MessageTree;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.nlp.util.Sets;

import java.util.List;
import java.util.Set;

public class ChildrenClusterEvaluator implements Evaluator {
    private double totalF1;

    @Override
    public void addPrediction(MessageTree gold, MessageTree guess) {
        // Just begin with depth 1
        List<Set<Message>> referencePartition = gold.getChildrenBags(1);
        List<Set<Message>> responsePartition = guess.getChildrenBags(1);

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
        System.out.printf("P %.3f R %.3f\n", precision, recall);
        totalF1 += 2 * precision * recall / (precision + recall);
    }

    @Override
    public Evaluation getEvaluation() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
