package it.fcambi.news.filters;

import it.fcambi.news.model.Language;
import it.fcambi.news.stemmer.StemmerWrapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Created by Francesco on 26/10/15.
 */
public class StemmerTextFilter extends LanguageDependentTextFilter {

    private static Map<Language, Map<String, String>> caches;
    static {
        // Init caches map
        caches = new ConcurrentHashMap<>();
        for (Language language : Language.values()) {
            caches.put(language, new ConcurrentHashMap<>());
        }
    }

    public StemmerTextFilter(Language lang) {
        super(lang);
    }

    @Override
    public List<String> filter(List<String> words) {
        StemmerWrapper stemmer = new StemmerWrapper(this.language);
        IntStream.range(0, words.size()).parallel().forEach(i -> {
            if (!caches.get(this.language).containsKey(words.get(i))) {
                String stemmed = stemmer.getStemmedWord(words.get(i));
                if (stemmed.length() == 0)
                    stemmed = words.get(i);
                caches.get(this.language).put(words.get(i), stemmed);
            }
            words.set(i, caches.get(this.language).get(words.get(i)));
        });
        return words;
    }
}
