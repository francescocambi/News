package it.fcambi.news.filters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Francesco on 26/10/15.
 */
public class NoiseWordsTextFilter implements TextFilter  {

    private static final String DICTIONARY_PATH = "noise_word_FULL";
    private static List<String> stopWords = new ArrayList<>();

    public NoiseWordsTextFilter() {
        if (stopWords.isEmpty()) {
            try {
                stopWords = Files.lines(Paths.get(DICTIONARY_PATH)).collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public List<String> filter(List<String> words) {
        return words.parallelStream().filter(s -> !stopWords.contains(s.toLowerCase()))
                .collect(Collectors.toList());
    }
}
