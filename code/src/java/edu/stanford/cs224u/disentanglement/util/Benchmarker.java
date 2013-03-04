package edu.stanford.cs224u.disentanglement.util;

import java.util.Stack;

public class Benchmarker {
    private static class Benchmark {
        public final String name;
        public final double start;

        public Benchmark(String name, double start) {
            this.name = name;
            this.start = start;
        }
    }

    private static final ThreadLocal<Stack<Benchmark>> stack = new ThreadLocal<Stack<Benchmark>>() {
        @Override
        protected Stack<Benchmark> initialValue() {
            return new Stack<Benchmark>();
        }
    };

    public static void push(String name) {
        System.err.println("Benchmark[" + name + "] starting...");
        stack.get().push(new Benchmark(name, System.currentTimeMillis()));
    }

    public static void pop() {
        Benchmark benchmark = stack.get().pop();
        System.err.println("Benchmark[" + benchmark.name + "] completed in " + (System.currentTimeMillis() - benchmark.start) / 1000 + "s");
    }

    public static void popError() {
        Benchmark benchmark = stack.get().pop();
        System.err.println("Benchmark[" + benchmark.name + "] errored out in " + (System.currentTimeMillis() - benchmark.start) / 1000 + "s");
    }
}
