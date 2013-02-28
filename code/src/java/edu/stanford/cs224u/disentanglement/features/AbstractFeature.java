package edu.stanford.cs224u.disentanglement.features;

public abstract class AbstractFeature implements Feature {
    protected static final double ONE = Double.valueOf(1);

    @Override
    public final int getMaxLength() {
        return getAttributeList().size();
    }
}
