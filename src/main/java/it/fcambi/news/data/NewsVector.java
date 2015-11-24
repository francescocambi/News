package it.fcambi.news.data;

import it.fcambi.news.model.Clustering;
import it.fcambi.news.model.FrontPage;

/**
 * Created by Francesco on 14/10/15.
 */
public class NewsVector {

    long[] newsIds;

    public NewsVector(FrontPage page, Clustering c) {

        newsIds = page.getArticles().stream()
//                .filter(a -> a.getNews(c) != null)
                .mapToLong(a -> a.getNews(c).getId())
                .toArray();

    }

    public long[] getNewsIds() {
        return newsIds;
    }

    public void setNewsIds(long[] newsIds) {
        this.newsIds = newsIds;
    }
}
