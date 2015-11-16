package it.fcambi.news.metrics.permutations;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Francesco on 16/11/15.
 */
public class KendallTauTest {

    private KendallTau ktau;

    @Before
    public void setUp() {
        ktau = new KendallTau();
    }

    @Test
    public void testZeroDistance() {
        long[] a = { 1L, 2L, 1L, 3L, 4L, 5L };
        long[] b = { 1L, 2L, 3L, 4L, 5L, 4L };

        assertEquals(0.0, ktau.compute(a, b), 0.01);
        assertEquals(0.0, ktau.compute(b, a), 0.01);

        long[] c = { 2L, 1L, 1L, 3L, 4L, 5L };
        long[] d = { 1L, 2L, 3L, 4L, 5L, 4L };

        assertEquals(1.0, ktau.compute(c, d), 0.01);
        assertEquals(1.0, ktau.compute(d, c), 0.01);
    }

    @Test
    public void testIJInAListAndOnlyOneInTheOther() {

        // Only one, with same order
        long[] a = { 1L, 2L, 3L, 4L, 5L, 6L };
        long[] b = { 1L, 2L, 3L, 4L, 5L, 5L };

        assertEquals(0.0, ktau.compute(a,b), 0.01);
        assertEquals(0.0, ktau.compute(b,a), 0.01);

        //Only one, inversed order
        long[] c = { 1L, 2L, 3L, 4L, 5L, 6L };
        long[] d = { 1L, 2L, 3L, 4L, 6L, 6L };

        assertEquals(1.0, ktau.compute(c,d), 0.01);
        assertEquals(1.0, ktau.compute(d,c), 0.01);

    }

    @Test
    public void testIInAListAndJInTheOther() {

        long[] a = { 1L, 2L, 3L, 4L, 5L, 6L };
        long[] b = { 1L, 2L, 3L, 4L, 5L, 60L };

        assertEquals(1.0, ktau.compute(a,b), 0.01);
        assertEquals(1.0, ktau.compute(b,a), 0.01);

    }

    @Test
    public void testIJOnlyInOneList() {

        long[] a = { 1L, 2L, 3L, 4L, 5L, 6L };
        long[] b = { 3L, 4L, 5L, 6L, 6L, 6L };

        assertEquals(8.5, ktau.compute(a,b), 0.01);
        assertEquals(8.5, ktau.compute(b,a), 0.01);

    }

}
