package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import org.joda.time.Days;
import weka.core.Attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LengthDifferenceFeatureFactory extends AbstractFeatureFactory {
    @Override
    public ArrayList<Attribute> getAttributeList() {
        return Lists.newArrayList(
                new Attribute("Length Parent / Child"),
                new Attribute("Length Child / Parent")
        );
    }

    @Override
    public Map<Integer, Double> processExample(MessagePair example) {
        double l1 = (double) example.getFirst().getNormalizedBodyString().length();
        double l2 = (double) example.getSecond().getNormalizedBodyString().length();
        if(l1 == 0 || l2 == 0) {
            return ImmutableMap.of();
        }

        return ImmutableMap.of(
                0, l1 / l2,
                1, l2 / l1
                );
    }
}
