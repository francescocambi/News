package it.fcambi.news.filters;

import it.fcambi.news.data.WordVector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 29/09/15.
 */
public class NoiseWordsVectorFilter implements VectorFilter {

    private static final String DICTIONARY_PATH = "noise_word_FULL";

    @Override
    public void filter(WordVector w) {
        List<String> generalWords;
        try {
            generalWords = Files.lines(Paths.get(DICTIONARY_PATH)).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (int i=0; i < w.getWords().size(); i++) {
            if (generalWords.contains(w.getWords().get(i))) {
                w.getWords().remove(i);
                w.getFrequencies().remove(i);
                i--;
            }
        }
    }

}
