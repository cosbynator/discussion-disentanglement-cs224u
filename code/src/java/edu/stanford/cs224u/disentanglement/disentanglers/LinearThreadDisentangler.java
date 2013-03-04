package edu.stanford.cs224u.disentanglement.disentanglers;

import edu.stanford.cs224u.disentanglement.structures.*;

import java.util.Iterator;
import java.util.List;

// For any test instance, guess that it's a single linear thread, where each
// message is responding to the closest previous message.

public class LinearThreadDisentangler implements  Disentangler {
    @Override
    public void train(Iterable<MessageTree> trainingData) {

    }

    @Override
    public MessageTree predict(List<Message> testInstance) {
        // Assumes test instance has at least one message
        Iterator<Message> iter = testInstance.iterator();
        MessageTree prediction = new MessageTree(new MessageNode(iter.next()), "");
        MessageNode current = prediction.getRoot();
        while (iter.hasNext()) {
            MessageNode nextNode = new MessageNode(iter.next());
            current.addChildren(nextNode);
            current = nextNode;
        }
        return prediction;
    }

}