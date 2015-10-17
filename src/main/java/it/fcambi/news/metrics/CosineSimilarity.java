package it.fcambi.news.metrics;

/**
 * Created by Francesco on 29/09/15.
 */
public class CosineSimilarity implements Metric {

    public double compute(int[] a, int[] b) {

        if (a.length != b.length)
            throw new IllegalArgumentException();

        int den = 0;
        int suma = 0;
        int sumb = 0;
        for (int i=0; i<a.length; i++) {
            den += a[i]*b[i];
            suma += a[i]*a[i];
            sumb += b[i]*b[i];
        }

        if (suma*sumb == 0) return 0.0;

        return den/(Math.sqrt(suma)*Math.sqrt(sumb));

    }

    public String getName() {
        return "cosine";
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
        return 0.8;
    }
}
