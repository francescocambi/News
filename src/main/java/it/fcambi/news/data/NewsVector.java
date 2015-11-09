package it.fcambi.news.data;

import it.fcambi.news.model.FrontPage;

/**
 * Created by Francesco on 14/10/15.
 */
public class NewsVector {

    long[] newsIds;

    public NewsVector(FrontPage page) {

//        newsIds = page.getArticles().stream()
//                .filter(a -> a.getNews() != null)
//                .mapToLong(a -> a.getNews().getId())
//                .toArray();

    }

    public long[] getNewsIds() {
        return newsIds;
    }

    public void setNewsIds(long[] newsIds) {
        this.newsIds = newsIds;
    }
}
