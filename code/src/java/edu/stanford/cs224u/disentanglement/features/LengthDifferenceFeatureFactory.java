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
        return ImmutableMap.of(
                0, ((double)example.getFirst().getNormalizedBodyString().length()) / example.getSecond().getNormalizedBodyString().length(),
                1, ((double)example.getSecond().getNormalizedBodyString().length()) / example.getFirst().getNormalizedBodyString().length()
                );
    }
}
