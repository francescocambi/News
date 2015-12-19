package it.fcambi.news.filters;

import it.fcambi.news.stemmer.StemmerWrapper;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Created by Francesco on 26/10/15.
 */
public class StemmerTextFilter implements TextFilter {

    private static Map<String, String> cache = new ConcurrentHashMap<>();

    @Override
    public List<String> filter(List<String> words) {
        StemmerWrapper stemmer = new StemmerWrapper();
        IntStream.range(0, words.size()).parallel().forEach(i -> {
            if (!cache.containsKey(words.get(i))) {
                String stemmed = stemmer.getStemmedWord(words.get(i));
                if (stemmed.length() == 0)
                    stemmed = words.get(i);
                cache.put(words.get(i), stemmed);
            }
            words.set(i, cache.get(words.get(i)));
        });
        return words;
    }
}
