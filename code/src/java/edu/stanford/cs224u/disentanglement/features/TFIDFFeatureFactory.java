package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.ImmutableMap;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;

import java.util.List;
import java.util.Map;

public class TFIDFFeatureFactory extends AbstractFeatureFactory {
    @Override
    public Feature createFeature(List<Message> messages) {
        return new Feature() {
            @Override
            public Map<Integer, Double> processExample(MessagePair example) {
                return ImmutableMap.of();
            }

            @Override
            public int getMaxLength() {
                return 0;
            }
        };
    }
}
