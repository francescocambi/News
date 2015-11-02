package it.fcambi.news.filters;

import it.fcambi.news.stemmer.StemmerWrapper;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Created by Francesco on 26/10/15.
 */
public class StemmerTextFilter implements TextFilter {

    private static Map<String, String> cache = new Hashtable<>();

    @Override
    public List<String> filter(List<String> words) {
        StemmerWrapper stemmer = new StemmerWrapper();
        IntStream.range(0, words.size()).parallel().forEach(i -> {
            if (!cache.containsKey(words.get(i))) {
                cache.put(words.get(i), stemmer.getStemmedWord(words.get(i)));
            }
            words.set(i, cache.get(words.get(i)));
        });
        return words;
    }
}
