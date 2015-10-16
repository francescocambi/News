package it.fcambi.news.data;

import it.fcambi.news.filters.StringFilter;
import it.fcambi.news.filters.VectorFilter;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Francesco on 29/09/15.
 */
public class WordVector {

    private List<String> words;
    private List<Integer> frequencies;

    private Set<StringFilter> stringFilters;

    public WordVector() {
        words = new LinkedList<>();
        frequencies = new LinkedList<>();
        stringFilters = new HashSet<>();
    }

    public void setSourceText(String text, String splitRegex) {
        words.clear();
        frequencies.clear();
        Arrays.asList(
                applyStringFilters(text).split(splitRegex)
        ).forEach(word -> {
                    if (!words.contains(word)) {
                        words.add(word);
                        frequencies.add(1);
                    } else {
                        Integer i = words.indexOf(word);
                        frequencies.set(i, frequencies.get(i) + 1);
                    }
                }
        );
    }

    public int[] getWordsFrequencyIn(String text, String splitRegex) {
        String[] wordsInText = applyStringFilters(text).split(splitRegex);
        int[] freq = new int[words.size()];

        for (int i = 0; i < words.size(); i++)
            for (int j = 0; j < wordsInText.length; j++)
                if (wordsInText[j].equals(words.get(i)))
                    freq[i]++;

        return freq;
    }

    public void addStringFilter(StringFilter f) {
        this.stringFilters.add(f);
    }

    public void removeStringFilter(StringFilter f) {
        this.stringFilters.remove(f);
    }

    private String applyStringFilters(String s) {
        Iterator<StringFilter> i = this.stringFilters.iterator();
        while (i.hasNext())
            s = i.next().filter(s);
        return s;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public List<Integer> getFrequencies() {
        return frequencies;
    }

    public void setFrequencies(List<Integer> frequencies) {
        this.frequencies = frequencies;
    }
}
