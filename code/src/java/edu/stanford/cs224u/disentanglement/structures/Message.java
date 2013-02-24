package edu.stanford.cs224u.disentanglement.structures;

import com.google.common.base.Objects;
import org.joda.time.DateTime;

public class Message {
    private final String id;
    private final String authorName;
    private final DateTime timestamp;
    private final String body;

    public Message(String id, String authorName, DateTime timestamp, String body) {
        this.id = id;
        this.authorName = authorName;
        this.timestamp = timestamp;
        this.body = body;
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
