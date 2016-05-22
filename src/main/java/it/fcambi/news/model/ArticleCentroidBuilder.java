package it.fcambi.news.model;

import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.data.Text;
import it.fcambi.news.data.WordVector;

/**
 * Created by Francesco on 23/02/16.
 */
public class ArticleCentroidBuilder {

    private MatchMapGeneratorConfiguration conf;
    private Article article;

    public ArticleCentroidBuilder setConfiguration(MatchMapGeneratorConfiguration conf) {
        this.conf = conf;
        return this;
    }

    public ArticleCentroidBuilder setArticle(Article a) {
        this.article = a;
        return this;
    }

    public Centroid build() {
        Text body = getTextAndApplyFilters(article.getBody());
        Text title = getTextAndApplyFilters(article.getTitle());
        Text description = getTextAndApplyFilters(article.getDescription());
        Text keywords = conf.getKeywordSelectionFn().apply(title, description, body);

        WordVector w = conf.getWordVectorFactory().createNewVector();
        w.setWordsFrom(keywords);
        w.setValuesFrom(body);

        Centroid centroid = new Centroid();
        for (int i = 0; i < w.getWords().size(); i++) {
            centroid.addValue(w.getWords().get(i), w.getValues().get(i));
        }

        return centroid;
    }

    private Text getTextAndApplyFilters(String s) {
        if (s == null) return new Text();
        Text t = conf.getStringToTextFn().apply(s);
        conf.getTextFilters().forEach(t::applyFilter);
        return t;
    }
}
