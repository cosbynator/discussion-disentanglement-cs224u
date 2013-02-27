package edu.stanford.cs224u.disentanglement.learners;

import edu.stanford.cs224u.disentanglement.structures.*;
import edu.stanford.cs224u.disentanglement.util.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Learner {
    public abstract void learn(Iterable<MessageTree> trainingData);

    public abstract MessageTree predict(List<Message> testInstance);

    public Iterable<MessageTree> LinearizeAndPredictMany(Iterable<MessageTree> testSet) {
        ArrayList<MessageTree> predictions = new ArrayList<MessageTree>();
        for (MessageTree instance : testSet) {
            predictions.add(predict(instance.linearize()));
        }
        return predictions;
    }
}
