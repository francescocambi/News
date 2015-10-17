package it.fcambi.news.metrics;

/**
 * Created by Francesco on 05/10/15.
 */
public interface Metric {

    double compute(int[] a, int[] b);
    String getName();
    int compare(double a, double b);
    double getMaxValue();
    double getMinValue();
    double getThreshold();

}
