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
            if ( a[i] > 0 && b[i] > 0)
                intersection++;

        return intersection/a.length;
    }

    public double compute(double[] a, double[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException();

        double intersection = 0;
        for(int i=0; i < a.length; i++)
            if ( a[i] > 0.0 && b[i] > 0.0)
                intersection++;

        return intersection/a.length;
    }

    public String getName() {
        return "jaccard";
    }

    @Override
    public int compare(double a, double b) {
        if (a<b) return -1;
        if (a>b) return 1;
        else return 0;
    }

    @Override
    public double getMaxValue() {
        return 1;
    }

    @Override
    public double getMinValue() {
        return 0;
    }

    @Override
    public double getThreshold() {
        return 0.7;
    }
}
