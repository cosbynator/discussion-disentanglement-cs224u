package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import org.joda.time.Days;
import org.joda.time.Years;
import weka.core.Attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class UserStatsFeatureFactory extends AbstractFeatureFactory {
    @Override
    public ArrayList<Attribute> getAttributeList() {
        return Lists.newArrayList(
                new Attribute("Karma Parent / Child"),
                new Attribute("Karma Child / Parent"),
                new Attribute("User Join Time Parent - Child")
        );
    }

    @Override
    public Map<Integer, Double> processExample(MessagePair example) {
        if(example.getFirst().getUser() == null || example.getSecond().getUser() == null) {
            return Collections.emptyMap();
        }

        ImmutableMap.Builder<Integer, Double> ret = ImmutableMap.builder();
        int parentKarma = example.getFirst().getUser().karma;
        int childKarma = example.getSecond().getUser().karma;
        if(parentKarma >= 1 && childKarma >= 1) {
            ret.put(0, (double)parentKarma / childKarma);
            ret.put(1, (double)childKarma / parentKarma);
        }

        ret.put(2, Days.daysBetween(example.getFirst().getUser().joinDate, example.getSecond().getUser().joinDate).getDays() / 365.0);
        return ret.build();
    }
}
