package it.fcambi.news;

import it.fcambi.news.model.Article;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Francesco on 16/10/15.
 */
public class MatchingArticle {

    private Article article;
    private List<Double> similarities;

    public MatchingArticle() {
        similarities = new ArrayList<>();
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public void addSimilarity(double d) {
        this.similarities.add(d);
    }

    public List<Double> getSimilarities() {
        return similarities;
    }

    public double getSimilarity(int i) {
        return similarities.get(i);
    }
}
