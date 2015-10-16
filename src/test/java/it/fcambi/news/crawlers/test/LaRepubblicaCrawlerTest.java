package it.fcambi.news.crawlers.test;

import it.fcambi.news.crawlers.Crawler;
import it.fcambi.news.crawlers.LaRepubblicaCrawler;
import it.fcambi.news.model.Article;
import it.fcambi.news.crawlers.CrawlerCannotReadArticleException;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Francesco on 24/09/15.
 */
public class LaRepubblicaCrawlerTest {

    public void testHomePageAndFeedLinks() {
        try {
            Crawler crawler = new LaRepubblicaCrawler();
            Set<String> set = new HashSet<String>();
            set.addAll(crawler.retrieveArticleUrlsFromFeed());
            set.addAll(crawler.retrieveArticleUrlsFromHomePage());

            Iterator<String> i = set.iterator();
            boolean result = true;
            while (i.hasNext()) {
                String s = i.next();
                try {
                    if (crawler.getArticle(s) != null) {
                        System.out.println("  OK  >> \t " + s);
                        result &= true;
                    }
                } catch (CrawlerCannotReadArticleException e) {
                    System.err.println("ERROR >> \t " + s);
                    result &= false;
                    e.printStackTrace();
                }
            }
            assertEquals(true, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testGetArticle() {
        try {
            Article a = (new LaRepubblicaCrawler()).getArticle("http://www.repubblica.it/spettacoli/berliner/2015/09/24/news/zubin_mehta_direttore_ospite-123599839/");
            System.out.println(a.getTitle()+"\n"+a.getDescription()+"\n"+a.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
