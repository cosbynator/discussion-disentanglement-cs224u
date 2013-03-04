package edu.stanford.cs224u.disentanglement.evaluation;

import edu.stanford.cs224u.disentanglement.structures.*;
import edu.stanford.cs224u.disentanglement.util.Pair;


public interface Evaluator {
    public void addPrediction(MessageTree gold, MessageTree guess);
    public Evaluation getEvaluation();
}
