package edu.stanford.cs224u.disentanglement;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import edu.stanford.cs224u.disentanglement.disentanglers.AllReplyToOpDisentangler;
import edu.stanford.cs224u.disentanglement.disentanglers.LinearThreadDisentangler;
import edu.stanford.cs224u.disentanglement.disentanglers.SVMDisentangler;
import edu.stanford.cs224u.disentanglement.features.TFIDFFeatureFactory;
import edu.stanford.cs224u.disentanglement.structures.DataSets;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessagePair;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;
import org.joda.time.DateTime;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class Test {
    public static void main(String[] args) throws FileNotFoundException, ScriptException {
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

    public static void testLinearThreadBaseline() {
        new DisentanglementPipeline()
                .withLearner(new LinearThreadDisentangler())
                .withTrainData(DataSets.ASK_REDDIT_TRAIN)
                .withTestData(DataSets.ASK_REDDIT_TEST)
                .run();
    }

    public static void testAllReplyBaseline() {
        new DisentanglementPipeline()
                .withLearner(new AllReplyToOpDisentangler())
                .withTrainData(DataSets.ASK_REDDIT_TRAIN)
                .withTestData(DataSets.ASK_REDDIT_TEST)
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
                .withTrainData(DataSets.ASK_REDDIT_SMALL_TRAIN)
                .withTestData(DataSets.ASK_REDDIT_DEV)
                .run();
    }

    public static void testPrintData() {
        for (MessageTree tree : DataSets.ASK_REDDIT_TRAIN.read()) {
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
