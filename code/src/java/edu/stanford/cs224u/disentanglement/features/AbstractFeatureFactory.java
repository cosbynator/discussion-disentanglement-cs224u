package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.Lists;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import weka.core.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractFeatureFactory implements Feature, FeatureFactory {
    protected static final double ONE = Double.valueOf(1);

    @Override
    public ArrayList<Attribute> getAttributeList() {
        return Lists.newArrayList(
            new Attribute(this.getClass().getSimpleName())
        );
    }

    @Override
    public final int getMaxLength() {
        return getAttributeList().size();
    }

    @Override
    public Feature createFeature(List<Message> messages) {
        return this;
    }

    @Override
    public Map<Integer, Double> processExample(MessagePair example) {
        throw new UnsupportedOperationException("I have not been implemented!") ;
    }
}
