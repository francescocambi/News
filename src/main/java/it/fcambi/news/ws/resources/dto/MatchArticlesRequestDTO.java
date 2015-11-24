package it.fcambi.news.ws.resources.dto;

/**
 * Created by Francesco on 03/10/15.
 */
public class MatchArticlesRequestDTO {

    private long articleId;
    private long newsId;

    public long getArticleId() {
        return articleId;
    }

    public void setArticleId(long articleId) {
        this.articleId = articleId;
    }

    public long getNewsId() {
        return newsId;
    }

    public void setNewsId(long newsId) {
        this.newsId = newsId;
    }
}
