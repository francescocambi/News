package it.fcambi.news.data;

import it.fcambi.news.model.FrontPage;

/**
 * Created by Francesco on 14/10/15.
 */
public class NewsVector {

    int[] newsIds;

    public NewsVector(FrontPage page) {

        newsIds = page.getArticles().stream()
                .filter(a -> a.getNews() != null)
                .mapToInt(a -> a.getNews().getId())
                .toArray();

    }

    public int[] getNewsIds() {
        return newsIds;
    }

    public void setNewsIds(int[] newsIds) {
        this.newsIds = newsIds;
    }
}
