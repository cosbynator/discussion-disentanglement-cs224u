package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import edu.stanford.cs224u.disentanglement.util.LDAModel;
import weka.core.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LDAFeatureFactory extends AbstractFeatureFactory {
    private LDAModel model;

    public LDAFeatureFactory(LDAModel model) {
        this.model = model;
    }

    public static final double log2 = Math.log(2);
    public static double klDivergence(double[] p1, double[] p2) {
        double klDiv = 0.0;

        for (int i = 0; i < p1.length; ++i) {
            if (p1[i] == 0) { continue; }
            if (p2[i] == 0.0) { continue; } // Limin

            klDiv += p1[i] * Math.log( p1[i] / p2[i] );
        }

        return klDiv / log2; // moved this division out of the loop -DM
    }

    @Override
    public ArrayList<Attribute> getAttributeList() {
        return Lists.newArrayList(
                new Attribute("KL_LDA_PARENT_CHILD"),
                new Attribute("KL_LDA_CHILD_PARENT")
        );
    }

    @Override
    public Feature createFeature(List<Message> messages) {
        return new Feature() {
            private Map<Message, double[]> inferenceMap = Maps.newHashMap();

            private double []topicInferenceForMessage(Message message) {
                if(!inferenceMap.containsKey(message)) {
                    inferenceMap.put(message, model.inferTopics(message));
                }
                return inferenceMap.get(message);
            }

            @Override
            public Map<Integer, Double> processExample(MessagePair example) {
                double []topicDistribution1 = topicInferenceForMessage(example.getFirst());
                double []topicDistribution2 = topicInferenceForMessage(example.getSecond());
                return ImmutableMap.of(
                    0, klDivergence(topicDistribution1, topicDistribution2),
                    1, klDivergence(topicDistribution2, topicDistribution1)
                );
            }

            @Override
            public int getMaxLength() {
                return 2;
            }
        };
    }
}
