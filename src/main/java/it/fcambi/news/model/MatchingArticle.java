package it.fcambi.news.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Francesco on 16/10/15.
 */
public class MatchingArticle {

    private Article article;
    private Map<String, Double> similarities;

    public MatchingArticle() {
        similarities = new HashMap<>();
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public void addSimilarity(String k, double d) {
        this.similarities.put(k, d);
    }

    public Map<String, Double> getSimilarities() {
        return similarities;
    }

    public double getSimilarity(String k) {
        return similarities.get(k);
    }
}
