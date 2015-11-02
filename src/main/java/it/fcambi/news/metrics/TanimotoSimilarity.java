package it.fcambi.news.metrics;

/**
 * Created by Francesco on 16/10/15.
 */
public class TanimotoSimilarity implements Metric {

    @Override
    public double compute(int[] a, int[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("Vectors must have same dimension.");

        double scalar = 0;
        double a_norm = 0;
        double b_norm = 0;
        for (int i=0; i < a.length; i++) {
            scalar += a[i]*b[i];
            a_norm += a[i]*a[i];
            b_norm += b[i]*b[i];
        }

        return scalar/(a_norm+b_norm-scalar);

    }

    @Override
    public double compute(double[] a, double[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("Vectors must have same dimension.");

        double scalar = 0;
        double a_norm = 0;
        double b_norm = 0;
        for (int i=0; i < a.length; i++) {
            scalar += a[i]*b[i];
            a_norm += a[i]*a[i];
            b_norm += b[i]*b[i];
        }

        return scalar/(a_norm+b_norm-scalar);

    }

    @Override
    public String getName() {
        return "tanimoto";
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
