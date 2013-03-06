package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;

import java.util.Map;

public class JaccardSimilarityFeatureFactory extends AbstractFeatureFactory {
    @Override
    public Map<Integer, Double> processExample(MessagePair example) {
        return ImmutableMap.of(
            0, example.getWordIntersection().size() / (double) example.getWordUnion().size()
        );
    }
}
