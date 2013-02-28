package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import weka.core.Attribute;

import java.util.ArrayList;
import java.util.Map;

public class JaccardSimilarityFeature extends AbstractFeature {

    @Override
    public ArrayList<Attribute> getAttributeList() {
        return Lists.newArrayList(new Attribute("Jaccard"));
    }

    @Override
    public Map<Integer, Double> processExample(MessagePair example) {
        Map<Integer, Double> map = Maps.newHashMap();
        map.put(0, example.getWordIntersection().size() / (double) example.getWordUnion().size());
        return map;
    }
}
