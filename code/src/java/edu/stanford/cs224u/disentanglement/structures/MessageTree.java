package edu.stanford.cs224u.disentanglement.structures;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

public class MessageTree implements Serializable {
    private final MessageNode root;
    private final String title;

    private final Map<String, Object> metadata;

    public MessageTree(MessageNode root, String title) {
        this.root = root;
        this.title = title;
        metadata = Maps.newHashMap();
    }

    public void addMetadata(String name, Object value) {
        metadata.put(name, value);
    }

    public MessageNode getRoot() {
        return root;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("root", root)
                .add("title", title)
                .add("metadata", metadata)
                .toString();
    }
}
