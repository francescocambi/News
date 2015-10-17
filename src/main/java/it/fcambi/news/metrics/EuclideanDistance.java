package it.fcambi.news.metrics;

/**
 * Created by Francesco on 16/10/15.
 */
public class EuclideanDistance implements Metric {

    @Override
    public double compute(int[] a, int[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("Vectors must have same dimension.");

        double distance = 0;
        for (int i=0; i<a.length; i++) {
            distance += (a[i]-b[i])*(a[i]-b[i]);
        }

        distance = Math.sqrt(distance);

        return distance;
    }

    @Override
    public String getName() {
        return "euclidean";
    }

    @Override
    public int compare(double a, double b) {
        if (a<b) return 1;
        if (a>b) return -1;
        else return 0;
    }

    /**
     * @return value for worse result
     */
    @Override
    public double getMinValue() {
        return Double.MAX_VALUE;
    }

    /**
     * @return Value for optimum
     */
    @Override
    public double getMaxValue() {
        return 0;
    }

    @Override
    public double getThreshold() {
        return 0;
    }
}
