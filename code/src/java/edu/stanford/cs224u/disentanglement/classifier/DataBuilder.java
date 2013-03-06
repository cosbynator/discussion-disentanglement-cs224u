package edu.stanford.cs224u.disentanglement.classifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import edu.stanford.cs224u.disentanglement.features.Feature;
import edu.stanford.cs224u.disentanglement.features.FeatureFactory;
import edu.stanford.cs224u.disentanglement.structures.Message;
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
    private List<FeatureFactory> featureFactories;
    private Map<MessagePair, String> data;
    private String datasetId;

    private Class categoriesClass;
    private Attribute categoryAttribute;
    private Instances trainingSet;
    private Instances instances;

    public <E extends Enum<E>> DataBuilder(Class<E> mCategoriesClass, String mDatasetId,
                                           FeatureFactory... featureFactories) {
        data = Maps.newHashMap();
        categoriesClass = mCategoriesClass;
        datasetId = mDatasetId;
        ArrayList<String> categories = Lists.newArrayList();
        for (Enum<E> enumVal : mCategoriesClass.getEnumConstants()) {
            categories.add(enumVal.toString());
        }
        Collections.sort(categories);
        categoryAttribute = new Attribute("@@category@@", categories);
        this.featureFactories = ImmutableList.copyOf(featureFactories);
        instances = new Instances(datasetId, getMergedAttributes(), 0);
        instances.setClassIndex(0);
    }

    public Instances getInstances() {
        return instances;
    }

    public class TreeData {
        List<Feature> features;

        public TreeData(List<Feature> features) {
            this.features = features;
        }

        public <E extends Enum<E>> void addExample(MessagePair example, E exampleCategory) {
            if (!categoriesClass.equals(exampleCategory.getDeclaringClass())) {
                throw new IllegalArgumentException(
                        String.format("Expected Enum class for category to be %s, got %s",
                                categoriesClass.getName(),
                                exampleCategory.getDeclaringClass().getName()));
            }
            Instance instance = getMergedInstance(example);
            instance.setDataset(instances);
            instance.setValue(0, exampleCategory.toString());
            instances.add(instance);
        }

        public Instance buildClassificationInstance(MessagePair messagePair) {
            return getMergedInstance(messagePair);
        }

        private Instance getMergedInstance(MessagePair example) {
            Instance mergedInstance = new SparseInstance(1);
            for (Feature f : features) {
                Map<Integer, Double> values = f.processExample(example);
                SparseInstance instance = new SparseInstance(1.0, Doubles.toArray(values.values()),
                        Ints.toArray(values.keySet()), f.getMaxLength());
                mergedInstance = mergedInstance.mergeInstance(instance);
            }
            return mergedInstance;
        }
    }

    public TreeData createTreeData(List<Message> messages) {
        List<Feature> features = Lists.newArrayList();
        for(FeatureFactory ff : featureFactories) {
            features.add(ff.createFeature(messages));
        }

        return new TreeData(features);
    }


    private ArrayList<Attribute> getMergedAttributes() {
        ArrayList<Attribute> mergedList = Lists.newArrayList();
        mergedList.add(categoryAttribute);
        for (FeatureFactory ff : featureFactories) {
            mergedList.addAll(ff.getAttributeList());
        }
        return mergedList;
    }
}
