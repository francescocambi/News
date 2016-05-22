package it.fcambi.news.model;

/**
 * Created by Francesco on 23/02/16.
 */
public class MatchingArticle extends MatchingCluster {

    public Article getArticle() {
        return this.getCluster();
    }

    public void setArticle(Article article) {
        this.setCluster(article);
    }

    @Override
    public Article getCluster() {
        return (Article) super.getCluster();
    }
}
