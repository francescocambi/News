package it.fcambi.news.metrics;

/**
 * Created by Francesco on 05/10/15.
 */
public interface Metric {

    double compute(int[] a, int[] b);
    String getName();

}
