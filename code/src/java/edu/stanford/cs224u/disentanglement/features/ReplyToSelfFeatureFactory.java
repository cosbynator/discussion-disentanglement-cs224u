package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.ImmutableMap;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;

import java.util.Map;

public class ReplyToSelfFeatureFactory extends AbstractFeatureFactory {
    @Override
    public Map<Integer, Double> processExample(MessagePair example) {
        return ImmutableMap.of(
                0, example.getFirst().getAuthorName().toLowerCase().equals(example.getSecond().getAuthorName().toLowerCase()) ? 1.0 : 0.0
        );
    }
}
