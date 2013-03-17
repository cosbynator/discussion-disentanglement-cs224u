package edu.stanford.cs224u.disentanglement.structures;

import com.beust.jcommander.internal.Sets;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import org.joda.time.DateTime;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Message implements Serializable, Comparable<Message> {
    private static final long serialVersionUID = 6553324886609049459L;
    private final String id;
    private final String authorName;
    private final DateTime timestamp;
    private final String body;
    private final Annotation bodyAnnotation;

    @CheckForNull
    private final MessageUser user;

    private transient Map<String, Set<String>> nersByType;
    private transient List<String> bodyWords;
    private transient String normalizedBodyString;

    public Message(String id, String authorName, DateTime timestamp, String body, Annotation bodyAnnotation, MessageUser user) {
        this.id = id;
        this.authorName = authorName;
        this.timestamp = timestamp;
        this.body = body;
        this.bodyAnnotation = bodyAnnotation;
        this.user = user;
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

    @CheckForNull
    public MessageUser getUser() {
        return user;
    }

    // Memoized
    public String getNormalizedBodyString() {
        if(normalizedBodyString == null) {
            normalizedBodyString  = Joiner.on(" ").join(getBodyWords());
        }

        return normalizedBodyString;
    }

    // Memoized
    public List<String> getBodyWords() {
        if(bodyWords == null) {
            List<CoreLabel> coreLabels = bodyAnnotation.get(CoreAnnotations.TokensAnnotation.class);
            bodyWords = Lists.newArrayListWithCapacity(coreLabels.size());
            for(CoreLabel label : coreLabels) {
                bodyWords.add(label.value().toLowerCase());
            }
        }

        return bodyWords;
    }

    // Memoized
    public Set<String> getNamedEntitiesOfType(String type) {
        if(nersByType == null) {
            nersByType = Maps.newHashMap();
        }

        if(nersByType.containsKey(type)) {
            return nersByType.get(type);
        }

        Set<String> ret = Sets.newHashSet();
        List<CoreLabel> coreLabels = bodyAnnotation.get(CoreAnnotations.TokensAnnotation.class);
        bodyWords = Lists.newArrayListWithCapacity(coreLabels.size());
        for(CoreLabel label : coreLabels) {
            if(label.ner().equals(type)) {
                ret.add(label.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase());
            }
        }

        nersByType.put(type, ret);
        return ret;
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

    @Override
    public int compareTo(Message o) {
        return this.getTimestamp().compareTo(o.getTimestamp());
    }
}
