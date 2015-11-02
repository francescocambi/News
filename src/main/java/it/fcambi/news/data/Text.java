package it.fcambi.news.data;

import it.fcambi.news.filters.TextFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 26/10/15.
 */
public class Text {

    private List<String> words;

    public Text(String s, String splitRegex) {
        words = Arrays.asList(s.split(splitRegex));
    }

    public Text(Text...textsToMerge) {
        words = new ArrayList<>();
        for (Text text : textsToMerge) {
            words.addAll(text.words());
        }
    }

    public Text applyFilter(TextFilter f) {
        words = f.filter(words);
        return this;
    }

    public List<String> wordsSubset(Predicate<String> condition) {
        return words.stream().filter(condition).collect(Collectors.toList());
    }

    public List<String> words() {
        return words;
    }

    @Override
    public String toString() {
        return words.stream().collect(Collectors.joining(" "));
    }
}