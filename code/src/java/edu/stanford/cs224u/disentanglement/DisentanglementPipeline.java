package edu.stanford.cs224u.disentanglement;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import edu.stanford.cs224u.disentanglement.disentanglers.Disentangler;
import edu.stanford.cs224u.disentanglement.evaluation.*;
import edu.stanford.cs224u.disentanglement.structures.DataSets;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;
import edu.stanford.cs224u.disentanglement.util.Benchmarker;

import java.util.List;

public class DisentanglementPipeline {
    private DataSets trainData;
    private DataSets testData;
    private Disentangler disentangler;
    private List<Evaluator> evaluators;
    private String visualizationFile;

    public DisentanglementPipeline() {
        evaluators = ImmutableList.of(
                new UnderRootF1Evaluator(),
                new PairwiseF1Evaluator(),
                new AverageTreeF1Evaluator(),
                new ChildrenClusterEvaluator()
        );
        visualizationFile = "visuals/last_run.html";
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

    public DisentanglementPipeline withVisualizationFile(String visualizationFile) {
        this.visualizationFile = visualizationFile;
        return this;
    }

    public void run() {
        Preconditions.checkNotNull(trainData, "Must specify training data for pipeline!");
        Preconditions.checkNotNull(testData, "Must specify testing data for pipeline!");
        Preconditions.checkNotNull(disentangler, "Must specify a disentangler for pipeline!");

        List<MessageTree> goldTrees = Lists.newArrayList();
        List<MessageTree> guessTrees = Lists.newArrayList();

        Benchmarker.push("Training");
        disentangler.train(trainData.read());
        Benchmarker.pop();

        Benchmarker.push("Evaluation");
        for(MessageTree gold : testData.read()) {
            MessageTree guess = disentangler.predict(gold.linearize());
            for(Evaluator evaluator : evaluators) {
                evaluator.addPrediction(gold, guess);
            }
            goldTrees.add(gold);
            guessTrees.add(guess);
        }

        if(visualizationFile != null) {
            Benchmarker.push("Write visualization");
            writeVisualization(goldTrees, guessTrees);
            Benchmarker.pop();
        }

        System.out.println("Evaluations: ");
        for(Evaluator e : evaluators) {
            System.out.println("\t" + e.getEvaluation());
        }
        Benchmarker.pop();
    }

    private void writeVisualization(List<MessageTree> goldTrees, List<MessageTree> guessTrees) {
        ScriptEngineManager m = new ScriptEngineManager();
        ScriptEngine rubyEngine = m.getEngineByName("jruby");
        ScriptContext context = rubyEngine.getContext();

        context.setAttribute("goldTrees", goldTrees, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("guessTrees", guessTrees, ScriptContext.ENGINE_SCOPE);

        try {
            rubyEngine.eval("require \"./code/src/data_visualizer\";" +
                    "Visualizer.new(\"" + visualizationFile +"\", $goldTrees, $guessTrees).render_all", context);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}
