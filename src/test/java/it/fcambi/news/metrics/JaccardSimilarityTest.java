package it.fcambi.news.metrics;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Francesco on 16/10/15.
 */
public class JaccardSimilarityTest {

    @Test
    public void testCompute() {

        int[] a = {3,6,18};
        int[] b = {3,6,18};

        Metric m = new JaccardSimilarity();
        double sim = m.compute(a, b);

        assertEquals(1, sim, 0.0001);
    }

}
