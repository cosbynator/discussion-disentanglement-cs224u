package edu.stanford.cs224u.disentanglement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import edu.stanford.cs224u.disentanglement.disentanglers.Disentangler;
import edu.stanford.cs224u.disentanglement.evaluation.AverageTreeF1Evaluator;
import edu.stanford.cs224u.disentanglement.evaluation.Evaluator;
import edu.stanford.cs224u.disentanglement.evaluation.PairwiseF1Evaluator;
import edu.stanford.cs224u.disentanglement.structures.DataSets;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;
import edu.stanford.cs224u.disentanglement.util.Benchmarker;

import java.util.List;

public class DisentanglementPipeline {
    private DataSets trainData;
    private DataSets testData;
    private Disentangler disentangler;
    private List<Evaluator> evaluators;

    public DisentanglementPipeline() {
        evaluators = ImmutableList.of(
                new PairwiseF1Evaluator(),
                new AverageTreeF1Evaluator()
        );
    }

    public DisentanglementPipeline withEvaluators(Iterable<Evaluator> evaluators) {
        this.evaluators = ImmutableList.copyOf(evaluators);
        return this;
    }

    public DisentanglementPipeline withLearner(Disentangler disentangler) {
        this.disentangler = disentangler;
        return this;
    }

    public DisentanglementPipeline withTrainData(DataSets trainData) {
        this.trainData = trainData;
        return this;
    }

    public DisentanglementPipeline withTestData(DataSets testData) {
        this.testData = testData;
        return this;
    }

    public void run() {
        Preconditions.checkNotNull(trainData, "Must specify training data for pipeline!");
        Preconditions.checkNotNull(testData, "Must specify testing data for pipeline!");
        Preconditions.checkNotNull(disentangler, "Must specify a disentangler for pipeline!");


        Benchmarker.push("Training");
        disentangler.train(trainData.read());
        Benchmarker.pop();

        Benchmarker.push("Evaluation");
        for(MessageTree gold : trainData.read()) {
            MessageTree guess = disentangler.predict(gold.linearize());
            for(Evaluator evaluator : evaluators) {
                evaluator.addPrediction(gold, guess);
            }
        }
        Benchmarker.pop();

        System.out.println("Evaluations: ");
        for(Evaluator e : evaluators) {
            System.out.println("\t" + e.getEvaluation());
        }
    }
}
