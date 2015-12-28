package it.fcambi.news.relevance;

import it.fcambi.news.fpclustering.FrontPagesTimestampGroup;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.Clustering;
import it.fcambi.news.model.FrontPage;
import it.fcambi.news.model.News;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by Francesco on 26/12/15.
 */
public class NewsRelevanceCalculatorTest {

    private static final Clustering clustering = new Clustering("TEST");

    @Test
    public void testCalculator() {

        Article a = new Article();
        Article b = new Article();
        Article c = new Article();
        Article d = new Article();
        Article e = new Article();
        Article f = new Article();

        News one = new News(clustering);
        one.setId(1L);
        one.addArticle(a);
        one.addArticle(b);
        a.setNews(clustering, one);
        b.setNews(clustering, one);

        News two = new News(clustering);
        two.setId(2L);
        two.addArticle(c);
        two.addArticle(d);
        two.addArticle(e);
        c.setNews(clustering, two);
        d.setNews(clustering, two);
        e.setNews(clustering, two);

        News three = new News(clustering);
        three.setId(3L);
        three.addArticle(f);
        f.setNews(clustering, three);

        FrontPage first = new FrontPage();
        FrontPage second = new FrontPage();
        FrontPage third = new FrontPage();
        FrontPage fourth = new FrontPage();
        FrontPage fifth = new FrontPage();

        first.setArticles(Stream.of(a, c).collect(Collectors.toList()));

        second.setArticles(Stream.of(f).collect(Collectors.toList()));

        third.setArticles(Stream.of(d, f).collect(Collectors.toList()));

        fourth.setArticles(Stream.of(b).collect(Collectors.toList()));

        fifth.setArticles(Stream.of(e).collect(Collectors.toList()));

        FrontPagesTimestampGroup groupOne = new FrontPagesTimestampGroup();
        Calendar oneHourAgo = Calendar.getInstance();
        oneHourAgo.add(Calendar.HOUR_OF_DAY, -1);
        groupOne.setTimestamp(oneHourAgo);
        groupOne.setFrontPages(Stream.of(first, second, third).collect(Collectors.toList()));

        FrontPagesTimestampGroup groupTwo = new FrontPagesTimestampGroup();
        Calendar now = Calendar.getInstance();
        groupTwo.setTimestamp(now);
        groupTwo.setFrontPages(Stream.of(fourth, fifth).collect(Collectors.toList()));

        List<FrontPagesTimestampGroup> groups = Stream.of(groupOne, groupTwo).collect(Collectors.toList());

        OrderBasedRelevance calculator = new OrderBasedRelevance(clustering);
        calculator.setFrontPagesGroups(groups);
        calculator.computeRelevances();

        List<NewsRelevance> relevances = calculator.getRelevances();
        relevances.forEach(relevance -> {

            if (relevance.getNews().equals(one)) {
                assertEquals(1, relevance.getRelevances().get(oneHourAgo.getTimeInMillis()), 0.01);
                assertEquals(1, relevance.getRelevances().get(now.getTimeInMillis()), 0.01);
            } else if (relevance.getNews().equals(two)) {
                assertEquals(1.5, relevance.getRelevances().get(oneHourAgo.getTimeInMillis()), 0.01);
                assertEquals(1, relevance.getRelevances().get(now.getTimeInMillis()), 0.01);
            } else if (relevance.getNews().equals(three)) {
                assertEquals(1.5, relevance.getRelevances().get(oneHourAgo.getTimeInMillis()), 0.01);
                assertNull(relevance.getRelevances().get(now.getTimeInMillis()));
            } else {
                assertFalse(true);
            }

        });

    }

}
