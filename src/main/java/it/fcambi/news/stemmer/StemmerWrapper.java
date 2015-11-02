package it.fcambi.news.stemmer;

import org.tartarus.snowball.ext.italianStemmer;

/**
 * Created by Francesco on 26/10/15.
 */
public class StemmerWrapper {

    private italianStemmer stemmer = new italianStemmer();

    public synchronized String getStemmedWord(String word) {
        stemmer.setCurrent(word);
        if (stemmer.stem()) {
            return stemmer.getCurrent();
        } else {
            return word;
        }
    }

}
