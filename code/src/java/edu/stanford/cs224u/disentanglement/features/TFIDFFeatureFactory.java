package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.*;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;

import java.util.*;

public class TFIDFFeatureFactory extends AbstractFeatureFactory {

    @Override
    public Feature createFeature(List<Message> messages) {
        final TFIDFWeighting tfidf = new TFIDFWeighting(messages);
        return new Feature() {
            @Override
            public Map<Integer, Double> processExample(MessagePair example) {
                return ImmutableMap.of(
                        0, tfidf.cosineSimilarity(example)
                );
            }

            @Override
            public int getMaxLength() {
                return 1;
            }
        };
    }
}
