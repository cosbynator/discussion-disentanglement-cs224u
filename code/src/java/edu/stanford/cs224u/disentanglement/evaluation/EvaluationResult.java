package edu.stanford.cs224u.disentanglement.evaluation;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class EvaluationResult {
    private final String name;
    private final Map<String, Double> metrics;

    /**
     * Builder for evaluation results. Always use this to construct an evaluation result.
     */
    public static class Builder {
        private final String evaluationResultName;
        private final ImmutableMap.Builder<String, Double> metricsBuilder;

        public Builder(String evaluationResultName) {
            this.evaluationResultName = evaluationResultName;
            this.metricsBuilder = ImmutableMap.builder();
        }

        /**
         * Initialize an EvaluationResult.Builder based on an existing result (and possibly extend).
         */
        public Builder(String evaluationResultName, EvaluationResult result) {
            this(evaluationResultName);
            metricsBuilder.putAll(result.metrics);
        }

        public Builder addMetric(String name, double key) {
            metricsBuilder.put(name, key);
            return this;
        }

        public EvaluationResult build() {
            return new EvaluationResult(evaluationResultName, metricsBuilder.build());
        }
    }

    private EvaluationResult(String name, Map<String, Double> metrics) {
        this.name = name;
        this.metrics = ImmutableMap.copyOf(metrics);
    }

    public double getMetric(String name) {
        return this.metrics.get(name);
    }

    @Override
    public final String toString() {
        Objects.ToStringHelper helper = Objects.toStringHelper(name);
        for (String key : metrics.keySet()) {
            helper.add(key, metrics.get(key));
        }

        return helper.toString();
    }
}
