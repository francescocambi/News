package it.fcambi.news.stemmer;

import it.fcambi.news.model.Language;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.StemmerSelector;

/**
 * Created by Francesco on 26/10/15.
 */
public class StemmerWrapper {

    private SnowballStemmer stemmer;

    public StemmerWrapper(Language lang) {
        stemmer = StemmerSelector.getStemmerForLanguage(lang);
    }

    public synchronized String getStemmedWord(String word) {
        stemmer.setCurrent(word);
        if (stemmer.stem()) {
            return stemmer.getCurrent();
        } else {
            return word;
        }
    }

}
