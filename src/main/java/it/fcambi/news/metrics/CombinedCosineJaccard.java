package it.fcambi.news.metrics;

/**
 * Created by Francesco on 16/10/15.
 */
public class CombinedCosineJaccard implements Metric {

    Metric cosine = new CosineSimilarity();
    Metric jaccard = new JaccardSimilarity();

    @Override
    public double compute(int[] a, int[] b) {
        return cosine.compute(a, b)+jaccard.compute(a, b);
    }

    @Override
    public String getName() {
        return "cosine_jaccard";
    }

    @Override
    public int compare(double a, double b) {
        if (a<b) return -1;
        if (a>b) return 1;
        else return 0;
    }

    @Override
    public double getMaxValue() {
        return 2;
    }

    @Override
    public double getMinValue() {
        return 0;
    }

    @Override
    public double getThreshold() {
        return 1.1;
    }
}
