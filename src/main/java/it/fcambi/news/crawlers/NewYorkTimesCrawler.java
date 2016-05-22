package it.fcambi.news.crawlers;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.Newspaper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 12/05/16.
 */
public class NewYorkTimesCrawler implements Crawler {

    @Override
    public Article getArticle(String url) throws IOException, CrawlerCannotReadArticleException {
        return null;
    }

    @Override
    public List<String> retrieveArticleUrlsFromHomePage() throws IOException {
        List<String> urls = new LinkedList<>();

        Document d = Jsoup.connect("http://www.nytimes.com").get();

        //Retrieve top article
        Elements topArticle = d.select("#top-news .a-lede-package-region article .story-heading a");

        urls.addAll(topArticle.stream().map(a -> a.attr("href")).collect(Collectors.toList()));

//        for (Element element : topArticle) {
//            System.out.println(element.attr("href"));
//        }

//        System.out.println("----------------------------------------");

        //Retrieve main column
        Elements mainCol = d.select("#top-news .second-column-region article .story-heading a");

        urls.addAll(mainCol.stream().map(a -> a.attr("href")).collect(Collectors.toList()));

//        for (Element element : mainCol) {
//            System.out.println(element.attr("href"));
//        }

        return urls;
    }

    @Override
    public Collection<String> retrieveArticleUrlsFromFeed() throws IOException {
        return null;
    }

    @Override
    public Newspaper getNewspaper() {
        return Newspaper.NEW_YORK_TIMES;
    }

    public static void main(String[] args) throws Exception {
        NewYorkTimesCrawler crawler = new NewYorkTimesCrawler();

        System.out.println(crawler.retrieveArticleUrlsFromHomePage().toString());
    }
}
