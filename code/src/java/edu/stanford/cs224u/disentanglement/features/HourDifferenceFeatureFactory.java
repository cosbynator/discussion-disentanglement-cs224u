package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.ImmutableMap;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import org.joda.time.Minutes;

import java.util.Map;

/**
 * User: Milind Ganjoo <mganjoo@stanford.edu>
 * Date: 3/7/13
 * Time: 6:57 AM
 */
public class HourDifferenceFeatureFactory extends AbstractFeatureFactory {
    @Override
    public Map<Integer, Double> processExample(MessagePair example) {
        return ImmutableMap.of(
                0, Minutes.minutesBetween(example.getFirst().getTimestamp(), example.getSecond().getTimestamp()).getMinutes() / 60.0
        );
    }
}
