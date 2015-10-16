package it.fcambi.news.metrics.permutations;

/**
 * Created by Francesco on 14/10/15.
 */
public interface PermutationsMetric {

    double compute(int[] a, int[] b);
    String getName();

}
