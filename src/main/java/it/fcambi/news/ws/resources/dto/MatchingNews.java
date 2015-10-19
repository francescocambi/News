package it.fcambi.news.ws.resources.dto;

import it.fcambi.news.model.News;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 18/10/15.
 */
public class MatchingNews {

    private News news;
    private List<MatchingArticleDTO> articles;
    private Map<String, Double> meanSimilarities;

    public MatchingNews() {
        articles = new ArrayList<>();
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public void addMatchingArticle(MatchingArticleDTO a) {

        this.articles.add(a);
        this.meanSimilarities = null;
    }

    public List<MatchingArticleDTO> getArticles() {
        return articles;
    }

    public Map<String, Double> getMeanSimilarities() {
        if (this.meanSimilarities == null) {
            //Sum
            meanSimilarities = articles.stream().flatMap(article -> article.getSimilarities().entrySet().stream())
                    .collect(Collectors.groupingBy(e -> e.getKey(), Collectors.summingDouble(e -> e.getValue())));
            //Divide
            meanSimilarities.entrySet().forEach(e -> e.setValue(e.getValue() / articles.size()));
        }

        return meanSimilarities;
    }
}
