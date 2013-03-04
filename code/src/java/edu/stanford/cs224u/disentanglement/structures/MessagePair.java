package edu.stanford.cs224u.disentanglement.structures;

import com.google.common.collect.Sets;
import edu.stanford.cs224u.disentanglement.util.Pair;
import edu.stanford.cs224u.disentanglement.util.WordTokenizer;

import java.util.Set;

/**
 * Utility class for representing a pair of messages, and utility functions to act on it.
 */
public class MessagePair extends Pair<Message, Message> {
    public static MessagePair of(Message first, Message second) {
        return new MessagePair(first, second);
    }

    public MessagePair(Message first, Message second) {
        super(first, second);
    }

    public Set<String> getWordIntersection() {
        Set<String> firstTokens = Sets.newHashSet(WordTokenizer.tokenizeWhitespace(getFirst().getBody()));
        Set<String> secondTokens = Sets.newHashSet(WordTokenizer.tokenizeWhitespace(getSecond().getBody()));
        return Sets.intersection(firstTokens, secondTokens).immutableCopy();
    }

    public Set<String> getWordUnion() {
        Set<String> firstTokens = Sets.newHashSet(WordTokenizer.tokenizeWhitespace(getFirst().getBody()));
        Set<String> secondTokens = Sets.newHashSet(WordTokenizer.tokenizeWhitespace(getSecond().getBody()));
        return Sets.union(firstTokens, secondTokens).immutableCopy();
    }
}
