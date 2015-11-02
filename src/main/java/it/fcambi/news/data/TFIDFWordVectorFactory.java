package it.fcambi.news.data;

import it.fcambi.news.model.TFDictionary;

/**
 * Created by Francesco on 30/10/15.
 */
public class TFIDFWordVectorFactory implements WordVectorFactory {

    private TFDictionary dictionary;

    public TFIDFWordVectorFactory(TFDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public WordVector createNewVector() {
        return new TFIDFWordVector(dictionary);
    }
}
