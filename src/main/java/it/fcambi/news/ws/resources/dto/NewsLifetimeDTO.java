package it.fcambi.news.ws.resources.dto;

import it.fcambi.news.model.News;

/**
 * Created by Francesco on 20/02/16.
 */
public class NewsLifetimeDTO {

    protected News news;
    protected long relevanceLifetime;
    protected long timeRange;

    public NewsLifetimeDTO() {
    }

    public NewsLifetimeDTO(News news, long relevanceLifetime, long timeRange) {
        this.news = news;
        this.relevanceLifetime = relevanceLifetime;
        this.timeRange = timeRange;
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public long getRelevanceLifetime() {
        return relevanceLifetime;
    }

    public void setRelevanceLifetime(long relevanceLifetime) {
        this.relevanceLifetime = relevanceLifetime;
    }

    public long getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(long timeRange) {
        this.timeRange = timeRange;
    }
}
