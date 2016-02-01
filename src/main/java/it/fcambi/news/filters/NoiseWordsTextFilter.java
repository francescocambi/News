package it.fcambi.news.filters;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 26/10/15.
 */
public class NoiseWordsTextFilter implements TextFilter  {

    private static final String DICTIONARY_PATH = "/noise_word_FULL";
    private static List<String> stopWords = new ArrayList<>();

    public NoiseWordsTextFilter() {
        if (stopWords.isEmpty()) {
            BufferedReader txtReader = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream(DICTIONARY_PATH)
            ));
            stopWords = txtReader.lines().collect(Collectors.toList());
            if (stopWords.isEmpty())
                throw new IllegalStateException("Empty NoiseWordsTextFilter words list.");
        }
    }

    public List<String> filter(List<String> words) {
        return words.parallelStream().filter(s -> !stopWords.contains(s.toLowerCase()))
                .collect(Collectors.toList());
    }
}
