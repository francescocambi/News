package it.fcambi.news.data;

/**
 * Created by Francesco on 30/10/15.
 */
public class FrequenciesWordVectorFactory implements WordVectorFactory {

    @Override
    public WordVector createNewVector() {
        return new FrequenciesWordVector();
    }

}
