package it.fcambi.news.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Francesco on 26/10/15.
 */
public class FrequenciesWordVector implements WordVector {

    protected List<String> words = new ArrayList<>();
    protected List<Double> frequencies = new ArrayList<>();

    @Override
    public void setFrom(Text... texts) {
        words.clear();
        frequencies.clear();
        for (Text t : texts)
            for (String word : t.words()) {
                int i = words.indexOf(word.toLowerCase());
                if (i < 0) {
                    words.add(word.toLowerCase());
                    frequencies.add(1.0);
                } else
                    frequencies.set(i, frequencies.get(i)+1);
            }
    }

    @Override
    public void setWordsFrom(Text... texts) {
        words.clear();
        for (Text t : texts)
            for (String word : t.words()) {
                if (!words.contains(word.toLowerCase()))
                    words.add(word.toLowerCase());
            }
    }

    @Override
    public void setValuesFrom(Text... texts) {
        frequencies.clear();
        for (int i=0;i<words.size();i++) frequencies.add(0.0);
        for (Text t : texts)
            for (String word : t.words()) {
                int i = words.indexOf(word.toLowerCase());
                if (i >= 0)
                    frequencies.set(i, frequencies.get(i)+1);
            }
    }

    @Override
    public List<String> getWords() {
        return words;
    }

    @Override
    public void setWords(List<String> words) {
        this.words = words;
    }

    @Override
    public List<Double> getValues() {
        return frequencies;
    }

    @Override
    public void setValues(List<Double> values) {
        this.frequencies = values;
    }

    @Override
    public int size() {
        return words.size();
    }

    @Override
    public double[] toArray() {
        return frequencies.stream().mapToDouble(d -> d).toArray();
    }
}
