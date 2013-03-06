package edu.stanford.cs224u.disentanglement.features;

import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import weka.core.Attribute;

import java.util.ArrayList;
import java.util.Map;

/**
 * An interface for defining <code>MessagePair</code>-based features. The <code>AbstractFeature</code> class implements
 * this interface, and new feature classes should extend that class.
 */
public interface Feature {

    /**
     * Extract feature-related information from a single training example.
     * @param example the training example to process
     *
     * @return the feature-related information to extract
     */
    Map<Integer, Double> processExample(MessagePair example);

    /**
     * Get the maximum number of attributes to be represented by this feature. This is useful
     * for <code>SparseInstance</code> implementations to determine the maximum number of attributes
     * to expect.
     */
    int getMaxLength();
}
