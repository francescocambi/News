package it.fcambi.news.metrics;

/**
 * Created by Francesco on 23/10/15.
 */
public class MyMetric implements Metric {

    private static CosineSimilarity cos = new CosineSimilarity();
    private static JaccardSimilarity jac = new JaccardSimilarity();

    @Override
    public double compute(int[] a, int[] b) {
        double cosine = cos.compute(a,b);
        double jaccard = jac.compute(a,b);

        return 0.5*jaccard+0.5*cosine;
    }

    @Override
    public double compute(double[] a, double[] b) {
        double cosine = cos.compute(a,b);
        double jaccard = jac.compute(a,b);

        return 0.5*jaccard+0.5*cosine;
    }

    @Override
    public String getName() {
        return "mymetric";
    }

    @Override
    public int compare(double a, double b) {
        return 0;
    }

    @Override
    public double getMaxValue() {
        return 0;
    }

    @Override
    public double getMinValue() {
        return 0;
    }

    @Override
    public double getThreshold() {
        return 0;
    }
}
