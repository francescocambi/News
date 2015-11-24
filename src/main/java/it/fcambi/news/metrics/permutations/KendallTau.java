package it.fcambi.news.metrics.permutations;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Francesco on 16/11/15.
 */
public class KendallTau implements PermutationsMetric {

    private int k = -1;

    public KendallTau() {
    }

    public KendallTau(int k) {
        this.k = k;
    }

    @Override
    public double compute(long[] a, long[] b) {
        double kendall = 0;

        List<Long> x = Arrays.stream(a).distinct().boxed().collect(Collectors.toList());
        List<Long> y = Arrays.stream(b).distinct().boxed().collect(Collectors.toList());

//        if (k == -1)
//            k = (x.size() <= y.size()) ? x.size() : y.size();
        if (k < -1) {
            if (x.size() < k) k = x.size();
            if (y.size() < k) k = y.size();
            x = x.subList(0, k);
            y = y.subList(0, k);
        }

        // Create domain vector
        Vector<Long> items = new Vector<>();
        items.addAll(Stream.concat(x.stream(), y.stream()).distinct().collect(Collectors.toList()));

        // Iterates over all possible pairs of items in domain
        for (int i = 0; i < items.size(); i++) {
            for (int j = i+1; j < items.size(); j++) {

                // Sum 1 to remove negatives for not found
                // When one item is not found, product result will be 0
                int ix = x.indexOf(items.get(i))+1;
                int jx = x.indexOf(items.get(j))+1;
                int iy = y.indexOf(items.get(i))+1;
                int jy = y.indexOf(items.get(j))+1;

                if ( ix*jx*iy*jy > 0 ) {
                    // i j in both lists
                    // if order in a list is reversed in the other list
                    if ( (ix < jx && iy > jy) || (ix > jx && iy < jy) )
                        kendall++;
                } else if (ix*jx > 0 && iy == 0 && jy > 0) {
                    // i j both in X but only j in y
                    if ( jx > ix ) kendall++;
                } else if (ix*jx > 0 && jy == 0 && iy > 0) {
                    // i j both in X but only i in y
                    if ( ix > jx ) kendall++;
                } else if (iy*jy > 0 && ix == 0 && jx > 0) {
                    //i j both in Y but only j in x
                    if ( jy > iy ) kendall++;
                } else if (iy*jy > 0 && jx == 0 && ix > 0) {
                    //i j both in Y but only i in x
                    if ( iy > jy ) kendall++;
                } else if ( (ix*jx > 0 && iy*jy == 0) || (ix*jx == 0 && iy*jy > 0) ) {
                    // i j in only one list
                    kendall += 0.5;
                } else if ( (ix > 0 && iy == 0) || (ix == 0 && iy > 0)
                        || (jx > 0 && jy == 0) || (jx == 0 && jy > 0) ) {
                    // i in a list, j in the other list
                    kendall++;
                }

            }
        }

        return kendall;


    }

    @Override
    public String getName() {
        return "KENDALL";
    }
}
