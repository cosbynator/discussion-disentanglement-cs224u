package edu.stanford.cs224u.disentanglement.structures;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    private static final long serialVersionUID = 6535394886609049459L;
    private final String id;
    private final String authorName;
    private final DateTime timestamp;
    private final String body;
    private final Annotation bodyAnnotation;
    private List<String> bodyWords;

    public Message(String id, String authorName, DateTime timestamp, String body, Annotation bodyAnnotation) {
        this.id = id;
        this.authorName = authorName;
        this.timestamp = timestamp;
        this.body = body;
        this.bodyAnnotation = bodyAnnotation;
    }

    @Override
    public boolean equals(Object m) {
        if(m == null || !(m instanceof Message)) {
            return false;
        }
        Message om = (Message)m;
        return Objects.equal(id, om.id);
    }

    public String getId() {
        return id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public String getBody() {
        return body;
    }

    public List<String> getBodyWords() {
        if(bodyWords != null) {
            return bodyWords;
        }

        List<CoreLabel> coreLabels = bodyAnnotation.get(CoreAnnotations.TokensAnnotation.class);
        bodyWords = Lists.newArrayListWithCapacity(coreLabels.size());
        for(CoreLabel label : coreLabels) {
            bodyWords.add(label.value().toLowerCase());
        }

        return bodyWords;
    }

    public Annotation getBodyAnnotation() {
        return bodyAnnotation;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("author", authorName)
                .add("time", timestamp).add("body", body.substring(0, (Math.min(body.length(), 100)))).toString();
    }
}
