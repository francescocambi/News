package it.fcambi.news.crawlers.test;

import it.fcambi.news.crawlers.Crawler;
import it.fcambi.news.crawlers.LaStampaCrawler;
import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Francesco on 24/09/15.
 */
public class LaStampaCrawlerTest {

    public void testHomePageAndFeedLinks() {
        try {
            Crawler crawler = new LaStampaCrawler();
            Set<String> set = new HashSet<String>();
            set.addAll(crawler.retrieveArticleUrlsFromFeed());
            set.addAll(crawler.retrieveArticleUrlsFromHomePage());


            Iterator<String> i = set.iterator();
            boolean result = true;
            while (i.hasNext()) {
                String s = i.next();
//                System.out.println(s);
                try {
                    if (crawler.getArticle(s) != null) {
                        System.out.println("  OK  >> \t " + s);
                        result &= true;
                    }
                } catch (Exception e) {
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

}
