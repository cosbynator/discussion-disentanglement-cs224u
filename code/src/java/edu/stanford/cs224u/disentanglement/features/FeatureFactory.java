package edu.stanford.cs224u.disentanglement.features;

import edu.stanford.cs224u.disentanglement.structures.Message;
import weka.core.Attribute;

import java.util.ArrayList;
import java.util.List;

public interface FeatureFactory {
    /**
     * Return the list of Weka attributes defined for this feature. Note that this
     * must be an <code>ArrayList</code>, as that is the format required for Weka.
     *
     * @return an <code>ArrayList</code> of features
     */
    public ArrayList<Attribute> getAttributeList();
    public Feature createFeature(List<Message> messages);
}
