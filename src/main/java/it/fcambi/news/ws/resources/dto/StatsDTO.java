package it.fcambi.news.ws.resources.dto;

import java.util.Date;

/**
 * Created by Francesco on 05/10/15.
 */
public class StatsDTO {

    private long articlesCount;
    private long matchedArticlesCount;
    private long notMatchedArticlesCount;
    private Date mostRecentArticleDate;

    public long getArticlesCount() {
        return articlesCount;
    }

    public void setArticlesCount(long articlesCount) {
        this.articlesCount = articlesCount;
    }

    public long getMatchedArticlesCount() {
        return matchedArticlesCount;
    }

    public void setMatchedArticlesCount(long matchedArticlesCount) {
        this.matchedArticlesCount = matchedArticlesCount;
    }

    public long getNotMatchedArticlesCount() {
        return notMatchedArticlesCount;
    }

    public void setNotMatchedArticlesCount(long notMatchedArticlesCount) {
        this.notMatchedArticlesCount = notMatchedArticlesCount;
    }

    public Date getMostRecentArticleDate() {
        return mostRecentArticleDate;
    }

    public void setMostRecentArticleDate(Date mostRecentArticleDate) {
        this.mostRecentArticleDate = mostRecentArticleDate;
    }
}
