package it.fcambi.news.filters;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Francesco on 26/10/15.
 */
public class StemmerTextFilterTest {

    @Test
    public void testFilter() {

        String[] words = { "abbandonata", "abbandonerò", "pronto", "pronuncerà", "propaga", "propaghino", "propensione", "proponenti" };
        String[] stemmedWords = { "abbandon", "abbandon", "pront", "pronunc", "propag", "propaghin", "propension", "proponent"};

        List<String> text = Arrays.asList(words);
        new StemmerTextFilter().filter(text);

        assertArrayEquals(stemmedWords, text.toArray(new String[text.size()]));

    }

}
