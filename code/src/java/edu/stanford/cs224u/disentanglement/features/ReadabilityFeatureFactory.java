package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.representqueens.lingua.en.Fathom;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import weka.core.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReadabilityFeatureFactory extends AbstractFeatureFactory{
    @Override
    public ArrayList<Attribute> getAttributeList() {
        return Lists.newArrayList(
                new Attribute("Readability Parent / Child"),
                new Attribute("Readability Child / Parent")
        );
    }

    double fleschKincaid(Fathom.Stats stats) {
        return 206.835 - (1.015 * ((double)stats.getNumWords() / stats.getNumSentences())) - (84.6 * (double)stats.getNumSyllables() / stats.getNumWords());
    }

    @Override
    public Map<Integer, Double> processExample(MessagePair example) {
        double f1 = fleschKincaid(example.getFirst().fathomBody());
        double f2 = fleschKincaid(example.getSecond().fathomBody());

        if(f1 <= 0 || f2 <= 0) {
            return ImmutableMap.of();
        }

        return ImmutableMap.of(
                0, f1 / f2,
                1, f2 / f1
        );
    }
}
