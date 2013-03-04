package edu.stanford.cs224u.disentanglement.disentanglers;

import edu.stanford.cs224u.disentanglement.structures.*;

import java.util.Iterator;
import java.util.List;

// For any test instance, guess that all messages are replies to the root.

public class AllReplyToOpDisentangler implements Disentangler {
    public AllReplyToOpDisentangler() {
        super();
    }

    @Override
    public void train (Iterable<MessageTree> trainingData) {

    }

    @Override
    public MessageTree predict(List<Message> testInstance) {
        // Assumes test instance has at least one message
        Iterator<Message> iter = testInstance.iterator();
        MessageTree prediction = new MessageTree(new MessageNode(iter.next()), "");
        while (iter.hasNext()) {
            MessageNode nextNode = new MessageNode(iter.next());
            prediction.getRoot().addChildren(nextNode);
        }
        return prediction;
    }

}