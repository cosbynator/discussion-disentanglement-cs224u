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
    private final int numFalseExamples = 1;
    private Random random;

    public SVMDisentangler() {
        random = new Random(1); // fixed seed for now
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
        //dataBuilder.addFeature(new JaccardSimilarityFeature());
        dataBuilder.addFeature(new BagOfWordsIntersectingFeature(sentences, 5));
        Benchmarker.pop();

        Benchmarker.push("Adding examples");
        for(MessageTree tree : trainingData) {
            List<Message> linearized = tree.linearize();
            for(MessagePair p : tree.extractEdges()) {
                dataBuilder.addExample(p, MessagePairCategories.RELATED);
                int foundExamples = 0;
                int iterations = 0;
                while(iterations < 100 && foundExamples < numFalseExamples) {
                    iterations++;
                    Message example = linearized.get(random.nextInt(linearized.size()));
                    if(example.equals(p.getSecond()) || example.equals(p.getFirst())) {
                        continue;
                    }
                    MessagePair pReplace = new MessagePair(example, p.getSecond());
                    dataBuilder.addExample(pReplace, MessagePairCategories.NOT_RELATED);
                    foundExamples++;
                }

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
                    if(classifier.classifyInstance(instance) == 1) {
                     System.out.println("---");
                     System.out.println(classifier.classifyInstance(instance));
                     System.out.println(classProbs[0]);
                     System.out.println(classProbs[1]);
                    }
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
