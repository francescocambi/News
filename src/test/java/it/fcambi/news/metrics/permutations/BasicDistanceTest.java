package it.fcambi.news.metrics.permutations;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Francesco on 14/10/15.
 */
public class BasicDistanceTest {

    @Test
    public void testCompute() {
        int[] a = { 5,6,3,4,2,9 };
        int[] b = { 18,3,21,4,6,5,2,1 };

        BasicDistance d = new BasicDistance();
        assertEquals(4, d.compute(a,b), 0.1);
    }

}
