package edu.stanford.cs224u.disentanglement.structures;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

public class MessageBodyAnnotator {
    private final StanfordCoreNLP pipeline;

    public MessageBodyAnnotator() {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        pipeline = new StanfordCoreNLP(props);
    }

    public Annotation annotateBody(String text) {
        Annotation ret = new Annotation(text);
        pipeline.annotate(ret);
        return ret;
    }
}
