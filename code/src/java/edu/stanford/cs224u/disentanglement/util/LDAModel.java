package edu.stanford.cs224u.disentanglement.util;

import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import com.google.common.base.Joiner;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessageNode;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class LDAModel implements Serializable {

    private static Instance createInstance(Message m) {
        String messageText = m.getNormalizedBodyString();
        String target = m.getId();
        return new Instance(messageText, target, target, m.getBody());
    }


    private static Pipe createPipe() {
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("(\\p{L}[\\p{L}\\p{P}]+\\p{L}|[?!])")) );
        pipeList.add(new TokenSequenceRemoveStopwords(false).addStopWords(new String[] {
                "and", "is", "the", "for", "n't", "lrb", "rrb", "'s"
        }));
        //pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );
        return new SerialPipes(pipeList);
    }

    public static LDAModel createFromTrees(int numTopics, Iterable<MessageTree> trees) throws IOException {
        final Pipe pipe = createPipe();
        final InstanceList instances = new InstanceList(pipe);

        for(final MessageTree t : trees) {
            t.getRoot().preorderWalk(new MessageNode.TreeWalker() {
                @Override
                public void preorderVisit(MessageNode m, MessageNode parent, int depth) {
                    instances.addThruPipe(createInstance(m.getMessage()));
                }
            });
        }

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(4);

        // Run the model for 50 iterations and stop (this is for testing only,
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(1000);
        model.estimate();
        return new LDAModel(model, pipe);
    }

    private final ParallelTopicModel model;
    private final Pipe pipe;
    public LDAModel(ParallelTopicModel model, Pipe pipe) {
        this.model = model;
        this.pipe = pipe;
    }

    public double [] inferTopics(Message message) {
        // Create a new instance named "test instance" with empty target and source fields.
        InstanceList testing = new InstanceList(pipe);
        testing.addThruPipe(createInstance(message));
        TopicInferencer inferencer = model.getInferencer();
        return inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
    }

    public void printTopics(int numWords) {
        System.out.println(
            "Topics:\n"
        );

        Object[][] topicWords = model.getTopWords(numWords);
        for(int i = 0; i < topicWords.length; i++) {
            System.out.println("Topic " + i + ": " + Joiner.on(",").join(topicWords[i]));
        }
    }

    public void saveModel(File fileName) {
        ObjectOutputStream stream = null;
        try {
            stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
            stream.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        System.out.println("Wrote model to "+ fileName);
    }

    public static LDAModel loadModel(File file) {
        ObjectInputStream input = null;
        ParallelTopicModel model;
        try {
            input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            return (LDAModel) input.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

}
