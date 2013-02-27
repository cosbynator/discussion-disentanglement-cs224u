package edu.stanford.cs224u.disentanglement.learners;

import edu.stanford.cs224u.disentanglement.structures.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// For any test instance, guess that all messages are replies to the root.

public class AllReplyToOpBaselineLearner extends Learner {
    public AllReplyToOpBaselineLearner() {
        super();
    }

    public void learn(Iterable<MessageTree> trainingData) {}

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