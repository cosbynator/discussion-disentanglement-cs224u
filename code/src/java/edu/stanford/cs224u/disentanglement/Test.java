package edu.stanford.cs224u.disentanglement;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import MM.M4;
import com.google.common.primitives.Doubles;
import edu.stanford.cs224u.disentanglement.disentanglers.AllReplyToOpDisentangler;
import edu.stanford.cs224u.disentanglement.disentanglers.LinearThreadDisentangler;
import edu.stanford.cs224u.disentanglement.disentanglers.SVMDisentangler;
import edu.stanford.cs224u.disentanglement.structures.*;
import edu.stanford.cs224u.disentanglement.util.LDAModel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.mutable.MutableInt;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Test {
    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new CommandLineParser();
        new JCommander(parser, args);

        String experimentName = parser.experiment;
        try {
            Method m = Test.class.getDeclaredMethod("test" + experimentName);
            m.setAccessible(true);
            m.invoke(null);
        } catch (NoSuchMethodException e) {
            System.err.println("Could not find experiment named '" + experimentName + "'!");
            System.exit(1);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            } else {
                e.printStackTrace();
            }
            System.exit(1);
        } catch (IllegalAccessException e) {
            System.err.println("Insufficient access privileges while executing experiment named '" + experimentName + "'!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void testLDA() throws IOException {
        LDAModel model = LDAModel.loadModel(new File("test_model"));
        Message m = DataSets.ASK_HN_TEST_SMALL.read().iterator().next().getRoot().getMessage();
        System.out.println(Doubles.join(",", model.inferTopics(m)));
        m = DataSets.ASK_HN_TEST_SMALL.read().iterator().next().getRoot().getChildren().get(0).getMessage();
        System.out.println(Doubles.join(",", model.inferTopics(m)));

        /*
        LDAModel model = LDAModel.createFromTrees(20, DataSets.ASK_HN_TRAIN.read());
        model.printTopics(20);
        System.out.println(m.getBody());

        model.saveModel(new File("test_model"));
        model = LDAModel.loadModel(new File("test_model"));
        model.printTopics(20);
        System.out.println(Doubles.join(",", model.inferTopics(m)));
        */

        /*
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("(\\p{L}[\\p{L}\\p{P}]+\\p{L}|[?!.])")) );
        pipeList.add(new TokenSequenceRemoveStopwords(false).addStopWords(new String[] {
            "and", "is", "the", "for", "n't", "lrb", "rrb", "'s"
        }));
        //pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        final InstanceList instances = new InstanceList(new SerialPipes(pipeList));

        DataSets ds = DataSets.ASK_HN_TRAIN_SMALL;

        for(final MessageTree t : ds.read()) {
            t.getRoot().preorderWalk(new MessageNode.TreeWalker() {
                @Override
                public void preorderVisit(MessageNode m, MessageNode parent, int depth) {
                    String messageText = Joiner.on(" ").join(m.getMessage().getBodyWords());
                    String target = m.getMessage().getId();
                    instances.addThruPipe(new Instance(messageText, target, target, m.getMessage().getBody()));
                }
            });
        }

        //Reader fileReader = new InputStreamReader(new FileInputStream(new File(args[0])), "UTF-8");
        //instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
        //        3, 2, 1)); // data, label, name fields

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        int numTopics = 10;
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(4);

        // Run the model for 50 iterations and stop (this is for testing only,
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(50);
        model.estimate();

        // Show the words and topics in the first instance

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();

        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        LabelSequence topics = model.getData().get(0).topicSequence;

        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for (int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        }
        System.out.println(out);

        // Estimate the topic distribution of the first instance,
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

        // Show top 5 words in topics with proportions for the first document
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < 10) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rank++;
            }
            System.out.println(out);
        }

        // Create a new instance with high probability of topic 0
        StringBuilder topicZeroText = new StringBuilder();
        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

        int rank = 0;
        while (iterator.hasNext() && rank < 5) {
            IDSorter idCountPair = iterator.next();
            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
            rank++;
        }

        // Create a new instance named "test instance" with empty target and source fields.
        InstanceList testing = new InstanceList(instances.getPipe());
        testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

        TopicInferencer inferencer = model.getInferencer();
        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
        System.out.println("0\t" + testProbabilities);
        */
    }

    public static void testCoreNLP() {
        DataSets dataSet = DataSets.ASK_HN_TRAIN;
        for(MessageTree tree : dataSet.read()) {
            System.out.println("---");
            System.out.println(tree.getRoot().getMessage().getNamedEntitiesOfType("PERSON"));
            System.out.println(tree.getRoot().getMessage().getNamedEntitiesOfType("ORGANIZATION"));
            System.out.println(tree.getRoot().getMessage().getNamedEntitiesOfType("LOCATION"));
            System.out.println(tree.getRoot().getMessage().getNamedEntitiesOfType("MISC"));
            //System.out.println(tree.getRoot().getMessage().getBodyWords());
        }
    }

    public static void testActModel() throws Exception {
        DataSets dataSet = DataSets.ASK_HN_TRAIN;
        int numberOfTopics = 10;
        int Z = numberOfTopics;
        double gamma0 = 1.0;
        double gamma1 = 1.0;
        double sigma2 = 10.0;
        double eta = 1.0;

        int rightContext = 0;
        int iters = 1000;
        M4 topicModel = new M4(Z, gamma0, gamma1, sigma2, eta, rightContext);

        File tempFile = new File("disentanglement-m4-input");
        System.out.println("Running in " + tempFile);

        final MutableInt lastInsert = new MutableInt(0);
        final Map<String, Integer> integerIdMap = Maps.newHashMap();

        final PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
        try {
            for(final MessageTree tree : dataSet.read()) {
                tree.getRoot().preorderWalk(new MessageNode.TreeWalker() {
                    @Override
                    public void preorderVisit(MessageNode m, MessageNode parent, int depth) {
                        lastInsert.increment();
                        int docId = lastInsert.intValue();
                        integerIdMap.put(m.getMessage().getId(), docId);

                        int parentId;
                        if (depth == 0) {
                            parentId = -1;
                        } else {
                            parentId = integerIdMap.get(parent.getMessage().getId());
                        }

                        writer.println(docId + " " + parentId
                                + " " + tree.getRoot().getMessage().getId() + "_" + m.getMessage().getId()
                                + " " + Joiner.on(" ").join(m.getMessage().getBodyWords()));

                    }
                });
            }
        } finally {
            IOUtils.closeQuietly(writer);
        }

        topicModel.run(iters, tempFile.getAbsolutePath());
    }

    public static void testLinearThreadBaseline() {
        new DisentanglementPipeline()
                .withLearner(new LinearThreadDisentangler())
                .withTrainData(DataSets.ASK_HN_TRAIN)
                .withTestData(DataSets.ASK_HN_DEV)
                .run();
    }

    public static void testAllReplyBaseline() {
        new DisentanglementPipeline()
                .withLearner(new AllReplyToOpDisentangler())
                .withTrainData(DataSets.ASK_HN_TRAIN)
                .withTestData(DataSets.ASK_HN_DEV)
                .run();
    }

    public static void testSVMSanity() throws Exception {
        new DisentanglementPipeline()
                .withLearner(new SVMDisentangler())
                .withTrainData(DataSets.ASK_HN_TRAIN_SMALL)
                .withTestData(DataSets.ASK_HN_TRAIN_SMALL)
                .run();
    }

    public static void testSVMDev() throws Exception {
        new DisentanglementPipeline()
                .withLearner(new SVMDisentangler())
                .withTrainData(DataSets.ASK_HN_TRAIN_SMALL)
                .withTestData(DataSets.ASK_HN_DEV_SMALL)
                .run();
    }

    public static void testPrintData() {
        for (MessageTree tree : DataSets.ASK_REDDIT_TRAIN_SMALL.read()) {
            System.out.println("****");
            System.out.println(tree);
            for (Message m : tree.linearize()) {
                System.out.println("\t" + m);
            }
        }
    }

    static class CommandLineParser {
        @Parameter(names = {"-exp", "-experiment"}, description = "Name of experiment to run")
        private String experiment = "PrintData";
    }
}
