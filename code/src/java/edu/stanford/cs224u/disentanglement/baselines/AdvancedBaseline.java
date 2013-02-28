package edu.stanford.cs224u.disentanglement.baselines;

import com.google.common.collect.Lists;
import edu.stanford.cs224u.disentanglement.classifier.DataBuilder;
import edu.stanford.cs224u.disentanglement.features.BagOfWordsIntersectingFeature;
import edu.stanford.cs224u.disentanglement.features.JaccardSimilarityFeature;
import edu.stanford.cs224u.disentanglement.structures.DataSets;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;

import java.util.Collections;
import java.util.List;
import java.util.Random;

enum MessagePairCategories {
    NOT_RELATED,
    RELATED,
}

public class AdvancedBaseline {
    public static void main(String[] args) throws Exception {
        System.out.println("Generating vocabulary...");
        List<String> sentences = Lists.newArrayList();
        for(MessageTree tree : DataSets.ASK_REDDIT_TRAIN.read()) {
            for (Message m : tree.linearize()) {
                sentences.add(m.getBody());
            }
        }

        DataBuilder builder = new DataBuilder(MessagePairCategories.class, "AdvancedBaseline");
        builder.addFeature(new JaccardSimilarityFeature());
        builder.addFeature(new BagOfWordsIntersectingFeature(sentences, 500));

        // TODO: Add more features here...

        System.out.println("Generating training examples...");
        int k = 0;
        for(MessageTree tree : DataSets.ASK_REDDIT_TRAIN.read()) {
            for(MessagePair p : tree.extractEdges()) {
                builder.addExample(p, MessagePairCategories.RELATED);

                // Pick a random other choice from the tree
                List<Message> otherChoices = Lists.newArrayList(tree.linearize());

                // Replace first for one bad example
                Collections.shuffle(otherChoices);
                Message randomReplacement = otherChoices.get(0).equals(p.getFirst()) ? otherChoices.get(1) : otherChoices.get(0);
                MessagePair pReplace = new MessagePair(randomReplacement, p.getSecond());
                builder.addExample(pReplace, MessagePairCategories.NOT_RELATED);

                // Replace second for another bad example
                Collections.shuffle(otherChoices);
                randomReplacement = otherChoices.get(0).equals(p.getSecond()) ? otherChoices.get(1) : otherChoices.get(0);
                pReplace = new MessagePair(p.getFirst(), randomReplacement);
                builder.addExample(pReplace, MessagePairCategories.NOT_RELATED);

                if (++k > 3) break;
            }
        }

        System.out.println("Building classifier...");
        SMO classifier = new SMO();
        Instances trainData = builder.buildData();
        classifier.buildClassifier(trainData);
        System.out.println(classifier);

        // TODO: Write advanced testing/evaluation code...
        Evaluation eval = new Evaluation(trainData);
        eval.crossValidateModel(classifier, trainData, 10, new Random(1));
        System.out.println(eval.toSummaryString("\nResults\n======\n", false));
    }
}
