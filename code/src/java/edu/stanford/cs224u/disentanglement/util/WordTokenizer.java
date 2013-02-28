package edu.stanford.cs224u.disentanglement.util;

import com.google.common.base.Splitter;

import java.util.regex.Pattern;

public class WordTokenizer {
    public static Iterable<String> tokenizeWhitespace(String text) {
        return Splitter.on(Pattern.compile("\\s+")).trimResults().omitEmptyStrings().split(text);
    }
}
