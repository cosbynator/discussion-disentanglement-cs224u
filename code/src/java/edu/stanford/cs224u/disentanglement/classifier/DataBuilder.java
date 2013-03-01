package edu.stanford.cs224u.disentanglement.classifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import edu.stanford.cs224u.disentanglement.features.Feature;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DataBuilder {
    private List<Feature> features;
    private Map<MessagePair, String> data;
    private String datasetId;

    private Class categoriesClass;
    private Attribute categoryAttribute;

    public <E extends Enum<E>> DataBuilder(Class<E> mCategoriesClass, String mDatasetId) {
        data = Maps.newHashMap();
        categoriesClass = mCategoriesClass;
        datasetId = mDatasetId;
        ArrayList<String> categories = Lists.newArrayList();
        for (Enum<E> enumVal : mCategoriesClass.getEnumConstants()) {
            categories.add(enumVal.toString());
        }
        Collections.sort(categories);
        categoryAttribute = new Attribute("@@category@@", categories);
        features = new ArrayList<Feature>();
    }

    public <E extends Enum<E>> DataBuilder(Class<E> mCategoriesClass, String mDatasetId, List<Feature> mFeatures) {
        this(mCategoriesClass, mDatasetId);
        features.addAll(mFeatures);
    }

    public void addFeature(Feature feature) {
        features.add(feature);
    }

    public <E extends Enum<E>> void addExample(MessagePair example, E exampleCategory) {
        if (!categoriesClass.equals(exampleCategory.getDeclaringClass())) {
            throw new IllegalArgumentException(
                    String.format("Expected Enum class for category to be %s, got %s",
                            categoriesClass.getName(),
                            exampleCategory.getDeclaringClass().getName()));
        }
        data.put(example, exampleCategory.toString());
    }

    public Instances buildData() {
        Instances instances = new Instances(datasetId, getMergedAttributes(), 0);
        instances.setClassIndex(0);

        for (MessagePair pair : data.keySet()) {
            Instance instance = getMergedInstance(pair, instances);
            instance.setValue(0, data.get(pair));
            instances.add(instance);
        }

        return instances;
    }

    private ArrayList<Attribute> getMergedAttributes() {
        ArrayList<Attribute> mergedList = Lists.newArrayList();
        mergedList.add(categoryAttribute);
        for (Feature f : features) {
            mergedList.addAll(f.getAttributeList());
        }
        return mergedList;
    }

    private Instance getMergedInstance(MessagePair example, Instances instances) {
        Instance mergedInstance = new SparseInstance(1);
        for (Feature f : features) {
            Map<Integer, Double> values = f.processExample(example);
            SparseInstance instance = new SparseInstance(1.0, Doubles.toArray(values.values()),
                    Ints.toArray(values.keySet()), f.getMaxLength());
            mergedInstance = mergedInstance.mergeInstance(instance);
        }
        mergedInstance.setDataset(instances);
        return mergedInstance;
    }
}
