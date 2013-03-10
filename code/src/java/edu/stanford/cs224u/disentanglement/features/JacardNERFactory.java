package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import edu.stanford.nlp.util.Sets;
import weka.core.Attribute;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class JacardNERFactory extends AbstractFeatureFactory {
    @Override
    public ArrayList<Attribute> getAttributeList() {
        return Lists.newArrayList(
            new Attribute("Jacard_LOCATION"),
            new Attribute("Jacard_PERSON"),
            new Attribute("Jacard_ORGANIZATION"),
            new Attribute("Jacard_MISC")
        );
    }

    private double jacardForType(MessagePair example, String type) {
        Set<String> first = example.getFirst().getNamedEntitiesOfType(type);
        Set<String> second = example.getSecond().getNamedEntitiesOfType(type);
        if(first.isEmpty() && second.isEmpty()) {
            return 0.0;
        }

        return (double)Sets.intersection(first, second).size() /  Sets.union(first, second).size();
    }

    @Override
    public Map<Integer, Double> processExample(MessagePair example) {
        return ImmutableMap.of(
                0, jacardForType(example, "LOCATION"),
                1, jacardForType(example, "PERSON"),
                2, jacardForType(example, "ORGANIZATION"),
                3, jacardForType(example, "MISC")
        );
    }
}
