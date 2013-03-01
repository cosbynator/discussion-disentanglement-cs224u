package edu.stanford.cs224u.disentanglement;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.stanford.cs224u.disentanglement.baselines.AdvancedBaseline;
import edu.stanford.cs224u.disentanglement.baselines.BasicBaseline;
import edu.stanford.cs224u.disentanglement.structures.DataSets;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;
import org.apache.commons.lang.WordUtils;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Test {
    static class CommandLineParser {
        @Parameter(names = {"-exp", "-experiment"}, description = "Name of experiment to run")
        private String experiment = "printData";
    }

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
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            System.err.println("Could not execute experiment named '" + experimentName + "'!");
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalAccessException e) {
            System.err.println("Could not execute experiment named '" + experimentName + "'!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void testBaseline() {
        BasicBaseline.runBaseline();
    }

    public static void testAdvancedBaseline() throws Exception {
        AdvancedBaseline.runBaseline();
    }

    public static void testPrintData() {
        for(MessageTree tree : DataSets.ASK_REDDIT_TRAIN.read()) {
            System.out.println("****");
            System.out.println(tree);
            for(Message m : tree.linearize()) {
                System.out.println("\t" + m);
            }
        }
    }
}
