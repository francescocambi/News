package it.fcambi.news.metrics;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by Francesco on 16/10/15.
 */
public class TanimotoSimilarityTest {

    @Test
    public void testCompute() {
        int[] a = {3,6,18,0};
        int[] b = {3,6,18,0};

        Metric m = new TanimotoSimilarity();
        double result = m.compute(a,b);

        assertEquals(1, result, 0.001);

    }

}
