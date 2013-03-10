package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.*;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import edu.stanford.cs224u.disentanglement.util.WordTokenizer;

import java.util.*;

public class TFIDFFeatureFactory extends AbstractFeatureFactory {
    public static class TFIDF {
        private final int docCount;
        private final Map<Message, HashMap<String, Double>> tfidfs = Maps.newHashMap();
        private final Map<Message, Double> norms = Maps.newHashMap();


        public TFIDF(List<Message> messages) {
            docCount = messages.size();
            Multiset<String> df = HashMultiset.create();
            List<Multiset<String>> tfs = Lists.newArrayList();
            for(Message m : messages) {
                Multiset<String> messageBag = HashMultiset.create(m.getBodyWords());
                tfs.add(messageBag);
                df.addAll(messageBag.elementSet());
            }

            for(int i = 0; i < messages.size(); i++) {
                HashMap<String, Double> tfidf = Maps.newHashMap();
                double norm = 0;
                for(Multiset.Entry<String> e : tfs.get(i).entrySet()) {
                    double weight = tfidf(e.getCount(), df.count(e.getElement()));
                    tfidf.put(e.getElement(), weight);
                    norm += weight * weight;
                }
                norms.put(messages.get(i), Math.sqrt(norm));
                tfidfs.put(messages.get(i), tfidf);
            }
        }

        private double tfidf(int tf, int df) {
            return (1 + Math.log(tf)) * Math.log(((double)docCount + 1.0) / df);
        }

        public double cosineSimilarity(MessagePair mp) {
            HashMap<String, Double> tfidf1 = tfidfs.get(mp.getFirst());
            HashMap<String, Double> tfidf2 = tfidfs.get(mp.getSecond());
            double dot = 0.0;
            for(Map.Entry<String, Double> e1 : tfidf1.entrySet()) {
                Double weight2 = tfidf2.get(e1.getKey());
                if(weight2 != null) {
                    dot += e1.getValue() * weight2;
                }
            }

            return dot / (norms.get(mp.getFirst()) * norms.get(mp.getSecond()));
        }
    }

    @Override
    public Feature createFeature(List<Message> messages) {
        final TFIDF tfidf = new TFIDF(messages);
        return new Feature() {
            @Override
            public Map<Integer, Double> processExample(MessagePair example) {
                return ImmutableMap.of(
                        0, tfidf.cosineSimilarity(example)
                );
            }

            @Override
            public int getMaxLength() {
                return 1;
            }
        };
    }
}
