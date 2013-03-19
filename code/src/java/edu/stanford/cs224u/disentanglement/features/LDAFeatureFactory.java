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
    private final LDAModel model;
    private final int numTopics;
    private final boolean buildCartesianProduct;

    public LDAFeatureFactory(LDAModel model) {
        this(model, false);
    }

    public LDAFeatureFactory(LDAModel model, boolean buildCartesianProduct) {
        this.model = model;
        this.numTopics = model.getModel().getNumTopics();
        this.buildCartesianProduct = buildCartesianProduct;
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
        ArrayList<Attribute> ret = Lists.newArrayList(
                new Attribute("KL_LDA_PARENT_CHILD"),
                new Attribute("KL_LDA_CHILD_PARENT")
        );

        if(buildCartesianProduct) {
            for(int i = 0; i < numTopics; i++) {
                for(int j = 0; j < numTopics; j++) {
                    ret.add(new Attribute("LDA Topic " + i + "*" + j));
                }
            }
        }
        return ret;
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
                ImmutableMap.Builder<Integer, Double> b = ImmutableMap.builder();
                b.put(0, klDivergence(topicDistribution1, topicDistribution2));
                b.put(1, klDivergence(topicDistribution2, topicDistribution1));
                if(buildCartesianProduct) {
                    int cnt = 2;
                    for(int i = 0; i < numTopics; i++) {
                        for(int j = 0; j < numTopics; j++) {
                            b.put(cnt, (topicDistribution1[i] * 2 -1) * (topicDistribution2[j] * 2 - 1));
                            cnt++;
                        }
                    }
                }

                return b.build();
            }

            @Override
            public int getMaxLength() {
                return 2 + (buildCartesianProduct ? numTopics * numTopics : 0);
            }
        };
    }
}
