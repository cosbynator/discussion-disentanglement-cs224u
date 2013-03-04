package edu.stanford.cs224u.disentanglement.evaluation;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import edu.stanford.cs224u.disentanglement.structures.*;
import edu.stanford.cs224u.disentanglement.util.Pair;

import java.util.Iterator;
import java.util.Set;


public class AverageTreeF1Evaluator implements Evaluator {
    double totalF1 = 0.0;
    int treeCount = 0;

    @Override
    public void addPrediction(MessageTree gold, MessageTree guess) {
        PairwiseF1Evaluator evaluator = new PairwiseF1Evaluator();
        evaluator.addPrediction(gold,guess);
        treeCount++;
        totalF1 += evaluator.getEvaluation().f1;
    }

    @Override
    public Evaluation getEvaluation() {
        final double averageF1 = totalF1 / treeCount;
        return new Evaluation() {
            @Override
            public String toString() {
                return Objects.toStringHelper("AveragePairwiseF1")
                        .add("averageF1", averageF1)
                        .toString();
            }
        };
    }

}
