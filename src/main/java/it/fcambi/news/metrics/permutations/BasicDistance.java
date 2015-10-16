package it.fcambi.news.metrics.permutations;

/**
 * Created by Francesco on 14/10/15.
 */
public class BasicDistance implements PermutationsMetric {

    @Override
    public double compute(int[] a, int[] b) {
        int min = (a.length < b.length) ? a.length : b.length;
        int distance = 0;

        // Count items in a but not in b
        for (int i=0; i<a.length; i++) {
            boolean found = false;
            for (int j = 0; j < b.length && !found; j++)
                found = a[i] == b[j];
            if (!found) distance++;
        }

        // Count items in b but not in a
        for (int i=0; i<b.length; i++) {
            boolean found = false;
            for (int j=0; j<a.length && !found; j++)
                found = b[i] == a[j];
            if (!found) distance++;
        }

        return distance;
    }

    @Override
    public String getName() {
        return "BASIC_DISTANCE";
    }
}
