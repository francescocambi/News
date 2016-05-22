package org.tartarus.snowball;

import it.fcambi.news.model.Language;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.italianStemmer;

/**
 * Created by Francesco on 15/05/16.
 */
public class StemmerSelector {

    public static SnowballStemmer getStemmerForLanguage(Language language) {
        switch (language) {
            case IT:
                return new italianStemmer();
            case EN:
                return new englishStemmer();
            default:
                return null;
        }
    }

}
