package it.fcambi.news.data;

import java.util.List;

/**
 * Created by Francesco on 30/10/15.
 */
public interface WordVector {

    void setFrom(Text...texts);
    void setWordsFrom(Text...texts);
    void setValuesFrom(Text...texts);


    List<String> getWords();
    void setWords(List<String> words);
    List<Double> getValues();
    void setValues(List<Double> values);

    double[] toArray();

    int size();

}
