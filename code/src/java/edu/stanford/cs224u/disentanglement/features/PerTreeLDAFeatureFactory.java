package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.Lists;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;
import edu.stanford.cs224u.disentanglement.util.LDAModel;
import weka.core.Attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PerTreeLDAFeatureFactory extends AbstractFeatureFactory {
    @Override
    public ArrayList<Attribute> getAttributeList() {
        ArrayList<Attribute> attrs = Lists.newArrayList();
        for(Attribute a :new LDAFeatureFactory(null).getAttributeList()) {
            attrs.add(new Attribute("PER-TREE " + a.name()));
        }
        return attrs;
    }

    @Override
    public Feature createFeature(List<Message> messages) {
        LDAModel model;
        try {
            model = LDAModel.createFromMessages(10, messages);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LDAFeatureFactory ldaFeature = new LDAFeatureFactory(model);
        return ldaFeature.createFeature(messages);
    }
}
