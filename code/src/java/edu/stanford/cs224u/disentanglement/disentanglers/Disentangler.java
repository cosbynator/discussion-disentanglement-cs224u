package edu.stanford.cs224u.disentanglement.disentanglers;

import edu.stanford.cs224u.disentanglement.structures.*;

import java.util.List;

public interface Disentangler {
    public void train(Iterable<MessageTree> trainingData);
    public MessageTree predict(List<Message> testInstance);
}
