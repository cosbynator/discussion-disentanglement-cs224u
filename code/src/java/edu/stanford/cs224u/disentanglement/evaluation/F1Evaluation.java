package edu.stanford.cs224u.disentanglement.evaluation;

import com.google.common.base.Objects;

/**
* Created with IntelliJ IDEA.
* User: tdimson
* Date: 3/8/13
* Time: 5:58 PM
* To change this template use File | Settings | File Templates.
*/
public class F1Evaluation implements Evaluation {
    public String name;
    public final double precision;
    public final double recall;
    public final double f1;

    public F1Evaluation(double precision, double recall) {
        this.name = this.getClass().getSimpleName();
        this.precision = precision;
        this.recall = recall;
        this.f1 = 2 * precision * recall / (precision + recall);
    }

    public void setName(String name) {
        this.name = name;
    }


    public String toString() {
        return Objects.toStringHelper(name)
                .add("f1", f1)
                .add("precision", precision)
                .add("recall", recall)
                .toString();
    }
}
