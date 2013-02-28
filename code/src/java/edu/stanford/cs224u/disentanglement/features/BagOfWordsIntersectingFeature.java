package edu.stanford.cs224u.disentanglement.features;

import com.google.common.collect.*;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import edu.stanford.cs224u.disentanglement.util.WordTokenizer;
import weka.core.Attribute;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A binary bag-of-words feature indicating presence of strings from a large vocabulary.
 */
public class BagOfWordsIntersectingFeature extends AbstractFeature {
    private final List<String> vocabulary;
    private final ArrayList<Attribute> attributeList;

    /**
     * Initialize the bag of words collection, with vocabulary derived from <code>sentences</code>.
     * Sentences will be normalized to lowercase and tokenized around whitespaces before extracting vocabulary.
     *
     * @param sentences        the iterable of sentences to extract vocabulary from
     * @param maximumVocabSize the maximum size of the vocabulary to use for the bag of words
     */
    public BagOfWordsIntersectingFeature(Iterable<String> sentences, int maximumVocabSize) {
        attributeList = Lists.newArrayList();

        Multiset<String> vocabularySet = HashMultiset.create();
        for (String s : sentences) {
            Iterable<String> tokens = WordTokenizer.tokenizeWhitespace(s);
            vocabularySet.addAll(Lists.newArrayList(tokens));
        }
        vocabulary = Lists.newArrayList();
        int i = 1;
        for (String word : Multisets.copyHighestCountFirst(vocabularySet).elementSet()) {
            vocabulary.add(word);
            if (++i > maximumVocabSize) {
                break;
            }
        }
        Collections.sort(vocabulary);

        for (String word : vocabulary) {
            attributeList.add(new Attribute("Word_" + word));
        }
    }

    @Override
    public ArrayList<Attribute> getAttributeList() {
        return attributeList;
    }

    @Override
    public Map<Integer, Double> processExample(MessagePair example) {
        Map<Integer, Double> values = Maps.newHashMap();
        for (String token : example.getWordIntersection()) {
            int ind = Collections.binarySearch(vocabulary, token);
            if (ind >= 0) {
                values.put(ind, ONE);
            }
        }
        return values;
    }
}