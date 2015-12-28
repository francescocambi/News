package it.fcambi.news.ws.resources.dto;

import it.fcambi.news.model.News;
import it.fcambi.news.relevance.NewsRelevance;

import java.util.Date;

/**
 * Created by Francesco on 27/12/15.
 */
public class NewsWithRelevanceDto {

    protected News news;
    protected double relevanceSum;
    protected Date firstAppearance;
    protected Date lastAppearance;

    public NewsWithRelevanceDto(NewsRelevance n) {
        news = n.getNews();
        relevanceSum = n.getRelevanceSum();
        firstAppearance = new Date(n.getRelevances().entrySet().stream().mapToLong(e -> e.getKey()).min().orElse(0));
        lastAppearance = new Date(n.getRelevances().entrySet().stream().mapToLong(e -> e.getKey()).max().orElse(0));
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public double getRelevanceSum() {
        return relevanceSum;
    }

    public void setRelevanceSum(double relevanceSum) {
        this.relevanceSum = relevanceSum;
    }

    public Date getFirstAppearance() {
        return firstAppearance;
    }

    public void setFirstAppearance(Date firstAppearance) {
        this.firstAppearance = firstAppearance;
    }

    public Date getLastAppearance() {
        return lastAppearance;
    }

    public void setLastAppearance(Date lastAppearance) {
        this.lastAppearance = lastAppearance;
    }
}
