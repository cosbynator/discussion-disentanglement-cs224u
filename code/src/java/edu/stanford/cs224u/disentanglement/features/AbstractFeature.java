package edu.stanford.cs224u.disentanglement.features;

import edu.stanford.cs224u.disentanglement.structures.Message;

import java.util.List;

public abstract class AbstractFeature implements Feature, FeatureFactory {
    protected static final double ONE = Double.valueOf(1);

    @Override
    public final int getMaxLength() {
        return getAttributeList().size();
    }

    @Override
    public Feature createFeature(List<Message> messages) {
        return this;
    }
}
