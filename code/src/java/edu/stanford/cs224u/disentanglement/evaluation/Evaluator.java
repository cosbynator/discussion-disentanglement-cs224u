package edu.stanford.cs224u.disentanglement.evaluation;

import edu.stanford.cs224u.disentanglement.structures.*;


public interface Evaluator {
    public void addPrediction(MessageTree gold, MessageTree guess);
    public EvaluationResult getEvaluation();
}
