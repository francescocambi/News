package it.fcambi.news.crawlers.test;

import it.fcambi.news.crawlers.CorriereDellaSeraCrawler;
import it.fcambi.news.crawlers.Crawler;
import it.fcambi.news.crawlers.CrawlerCannotReadArticleException;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Created by Francesco on 25/09/15.
 */
public class CorriereDellaSeraCrawlerTest {

    public void testHomePage() {
        Crawler crawler = new CorriereDellaSeraCrawler();

        try {
            Collection<String> urls = crawler.retrieveArticleUrlsFromFeed();
            urls.addAll(crawler.retrieveArticleUrlsFromHomePage());

            Iterator<String> i = urls.iterator();
            boolean result = true;
            while (i.hasNext()) {
                String s = i.next();
                try {
                    if (crawler.getArticle(s) != null) {
                        System.out.println("  OK  >> \t " + s);
                        result &= true;
                    }
                }
                catch (CrawlerCannotReadArticleException e) {
                    System.err.println("ERROR >> \t " + s);
                    result &= false;
                    e.printStackTrace();
                }
                catch (Exception e) {
                    System.err.println("CRITICAL >> \t " + s);
                    e.printStackTrace();
                }
            }
            assertEquals(true, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
