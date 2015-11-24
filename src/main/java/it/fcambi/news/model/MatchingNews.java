package it.fcambi.news.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Francesco on 18/10/15.
 */
public class MatchingNews {

    private News news;
    private Map<String, Double> similarities = new HashMap<>();

    private List<MatchingArticle> matchingArticles;

    public MatchingNews() {
        matchingArticles = new ArrayList<>();
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public double getSimilarity(String name) {
        return similarities.get(name);
    }

    public void addSimilarity(String name, double value) {
        this.similarities.put(name, value);
    }

    public Map<String, Double> getSimilarities() {
        return similarities;
    }

    public List<MatchingArticle> getMatchingArticles() {
        return matchingArticles;
    }

    public void setMatchingArticles(List<MatchingArticle> matchingArticles) {
        this.matchingArticles = matchingArticles;
    }
}
