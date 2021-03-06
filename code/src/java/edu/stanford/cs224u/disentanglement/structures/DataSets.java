package edu.stanford.cs224u.disentanglement.structures;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

public enum DataSets {
    ASK_REDDIT_DEV("data/AskReddit/dev"),
    ASK_REDDIT_TRAIN("data/AskReddit/train"),
    ASK_REDDIT_TRAIN_SMALL("data/AskReddit/train", 5),
    ASK_REDDIT_TEST("data/AskReddit/test"),

    ASK_HN_TRAIN("data/AskHN/train"),
    ASK_HN_DEV("data/AskHN/dev"),
    ASK_HN_TEST("data/AskHN/test"),

    ASK_HN_TRAIN_SMALL("data/AskHN/train", 100),
    ASK_HN_DEV_SMALL("data/AskHN/dev", 100),
    ASK_HN_TEST_SMALL("data/AskHN/test", 100);

    private final File location;
    private Integer sampleSize;

    private DataSets(String location) {
        this.location = new File(location);
    }

    private DataSets(String location, Integer sampleSize) {
        this.location = new File(location);
        this.sampleSize = sampleSize;
    }


    public Iterable<MessageTree> read() {
        List<MessageTree> ret = Lists.newArrayList();
        if(!location.isDirectory()) {
            throw new RuntimeException("Bad location for " + this.name() + ": " + location);
        }

        List<File> files = Arrays.asList(location.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.toString().contains(".gz");
            }
        }));

        if(sampleSize != null) {
            files = files.subList(0,Math.min(sampleSize, files.size()));
        }

        return Collections2.transform(files, new Function<File, MessageTree>() {
            @Override
            public MessageTree apply(File file) {
                ObjectInputStream inputStream;
                try {
                    inputStream = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    try {
                        return (MessageTree) inputStream.readObject();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }
        });
    }
}
