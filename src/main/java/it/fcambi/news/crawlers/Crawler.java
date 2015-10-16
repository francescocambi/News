package it.fcambi.news.crawlers;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.Newspaper;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created by Francesco on 24/09/15.
 */
public interface Crawler {

    /**
     * Download article info and content from the url passed as argument
     * @param url Url where the article can be found
     * @return the article
     */
    public Article getArticle(String url) throws IOException, CrawlerCannotReadArticleException;

    public List<String> retrieveArticleUrlsFromHomePage() throws IOException;

    public Collection<String> retrieveArticleUrlsFromFeed() throws IOException;

    public Newspaper getNewspaper();

}
