package it.fcambi.news.metrics;

/**
 * Created by Francesco on 30/09/15.
 */
public class JaccardSimilarity implements Metric {

    public double compute(int[] a, int[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException();

        double intersection = 0;
        for(int i=0; i < a.length; i++)
            if (a[i] > 0 && b[i] > 0)
                intersection++;

        return intersection/a.length;
    }

    public String getName() {
        return "jaccard";
    }


}
