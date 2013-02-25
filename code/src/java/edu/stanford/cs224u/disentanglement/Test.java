package edu.stanford.cs224u.disentanglement;

import com.google.common.base.Joiner;
import edu.stanford.cs224u.disentanglement.structures.DataSets;
import edu.stanford.cs224u.disentanglement.structures.Message;
import edu.stanford.cs224u.disentanglement.structures.MessageTree;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Test {
    public static void main(String[] args) throws FileNotFoundException, ScriptException {
        for(MessageTree tree : DataSets.ASK_REDDIT_TRAIN.read()) {
            System.out.println("****");
            System.out.println(tree);
            for(Message m : tree.linearize()) {
                System.out.println("\t" + m);
            }
        }
    }
}
