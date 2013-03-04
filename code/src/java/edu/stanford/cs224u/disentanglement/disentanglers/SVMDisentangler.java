package edu.stanford.cs224u.disentanglement.disentanglers;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.stanford.cs224u.disentanglement.classifier.DataBuilder;
import edu.stanford.cs224u.disentanglement.features.BagOfWordsIntersectingFeature;
import edu.stanford.cs224u.disentanglement.features.JaccardSimilarityFeature;
import edu.stanford.cs224u.disentanglement.structures.*;
import edu.stanford.cs224u.disentanglement.util.Benchmarker;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

enum MessagePairCategories {
    NOT_RELATED,
    RELATED,
}

public class SVMDisentangler implements Disentangler {
    private SMO classifier;
    private final DataBuilder dataBuilder;
    private Instances trainData;

    public SVMDisentangler() {

        dataBuilder = new DataBuilder(MessagePairCategories.class, "SVMDisentangler");
    }

    @Override
    public void train(Iterable<MessageTree> trainingData) {
        Benchmarker.push("Generate Vocabulary");
        List<MessageTree> train = Lists.newArrayList(trainingData);
        final List<String> sentences = Lists.newArrayList();
        for(MessageTree tree : train) {
            tree.getRoot().walk(new Function<MessageNode, Void>() {
                @Override
                public Void apply(MessageNode messageNode) {
                    sentences.add(messageNode.getMessage().getBody());
                    return null;
                }
            });
        }
        Benchmarker.pop();

        Benchmarker.push("Create data builder");
        dataBuilder.addFeature(new JaccardSimilarityFeature());
        dataBuilder.addFeature(new BagOfWordsIntersectingFeature(sentences, 5));
        Benchmarker.pop();

        Benchmarker.push("Adding examples");
        for(MessageTree tree : trainingData) {
            for(MessagePair p : tree.extractEdges()) {
                dataBuilder.addExample(p, MessagePairCategories.RELATED);

                // Pick a random other choice from the tree
                List<Message> otherChoices = Lists.newArrayList(tree.linearize());

                // Replace first for one bad example
                Collections.shuffle(otherChoices);
                Message randomReplacement = otherChoices.get(0).equals(p.getFirst()) ? otherChoices.get(1) : otherChoices.get(0);
                MessagePair pReplace = new MessagePair(randomReplacement, p.getSecond());
                dataBuilder.addExample(pReplace, MessagePairCategories.NOT_RELATED);

                // Replace second for another bad example
                Collections.shuffle(otherChoices);
                randomReplacement = otherChoices.get(0).equals(p.getSecond()) ? otherChoices.get(1) : otherChoices.get(0);
                pReplace = new MessagePair(p.getFirst(), randomReplacement);
                dataBuilder.addExample(pReplace, MessagePairCategories.NOT_RELATED);
            }
        }

        classifier = new SMO();
        classifier.setBuildLogisticModels(true);
        trainData = dataBuilder.buildData();
        try {
            Benchmarker.push("Build classifier");
            classifier.buildClassifier(trainData);
        } catch (Exception e) {
            Benchmarker.popError();
            throw new RuntimeException(e);
        }
    }

    @Override
    public MessageTree predict(List<Message> test) {
        Preconditions.checkArgument(test.size() > 0, "Tried to predict empty tree!");

        Map<Message, MessageNode> nodeForMessage = Maps.newHashMap();
        MessageNode root = new MessageNode(test.get(0));
        System.out.println(test.get(0).getId());
        nodeForMessage.put(test.get(0), root);

        for(int i = 1; i < test.size(); i++) {
            Message m = test.get(i);
            MessageNode maxParent = null;
            double maxProb = Double.NEGATIVE_INFINITY;
            for(int p = 0; p < i; p++) {
                MessageNode parentCandidate = nodeForMessage.get(test.get(p));
                MessagePair candidatePair = MessagePair.of(parentCandidate.getMessage(), m);
                Instance instance = dataBuilder.buildClassificationInstance(candidatePair);
                instance.setDataset(trainData);

                double []classProbs;
                try {
                     classProbs = classifier.distributionForInstance(instance);
                     System.out.println("---");
                     System.out.println(classifier.classifyInstance(instance));
                     System.out.println(classProbs[0]);
                     System.out.println(classProbs[1]);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }


                double prob = classProbs[1];
                if(prob > maxProb) {
                    maxProb = prob;
                    maxParent = parentCandidate;
                }
            }
            MessageNode mn = new MessageNode(m);
            maxParent.addChildren(mn);
            nodeForMessage.put(m, mn);
        }

        return new MessageTree(root, "Predicted reddit tree");
    }

}
