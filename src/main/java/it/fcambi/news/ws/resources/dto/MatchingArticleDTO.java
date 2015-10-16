package it.fcambi.news.ws.resources.dto;

import it.fcambi.news.model.Article;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Francesco on 30/09/15.
 */
public class MatchingArticleDTO {

    private Article article;
    private Map<String, Double> similarities;

    public MatchingArticleDTO() {
        similarities = new HashMap<String, Double>();
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public double getSimilarity(String key) {
        return similarities.get(key);
    }

    public Map<String, Double> getSimilarities() { return similarities; }

    public void putSimilarity(String key, double value) {
        similarities.put(key, value);
    }
}
