package it.fcambi.news.crawlers;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.Newspaper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Francesco on 12/10/15.
 */
public class IlGiornaleCrawler implements Crawler {

    @Override
    public Newspaper getNewspaper() {
        return Newspaper.IL_GIORNALE;
    }

    @Override
    public Article getArticle(String url) throws IOException, CrawlerCannotReadArticleException {
        Article a = new Article();

        Document d = Jsoup.connect(url).get();

        a.setSourceHtml(d.html());
        a.setSourceUrl(url);
        a.setSource(Newspaper.IL_GIORNALE);

        //Retrieve Title
        try {
            Element title = d.select("h1.entry-title").get(0);
            a.setTitle(title.text());
        } catch (Exception e) {
            throw new CrawlerCannotReadArticleException("Can't find headline on "+url);
        }

        //Retrieve Description
        try {
            Element desc = d.select("h2.entry-summary").get(0);
            a.setDescription(desc.text());
        } catch (Exception e) {
            throw new CrawlerCannotReadArticleException("Can't find description on "+url);
        }

        //Retrieve body
        Elements bodyPs = d.select("#insertbox_text p");
        if (bodyPs.size() < 1)
            throw new CrawlerCannotReadArticleException("Can't find article body on "+url);
        a.setBody("");
        bodyPs.forEach(p -> a.setBody(a.getBody()+" "+p.text()));

        return a;
    }

    @Override
    public List<String> retrieveArticleUrlsFromHomePage() throws IOException {
        List<String> urls = new LinkedList<>();

        Document d = Jsoup.connect("http://www.ilgiornale.it").get();

        //Retrieve article from apertura
        Elements links = d.select("div.hp_apertura .entry-title a:first-of-type");
        //Retrieve articles from main content column
        links.addAll(d.select("#content .sez_col1:lt(2) .entry-title a:first-of-type"));
        links.addAll(d.select("#block-ilg-elenco-item-home-home-bottom-1 .content .entry-title a:first-of-type"));
        links.forEach(a -> urls.add("http://www.ilgiornale.it"+a.attr("href")));

        return urls;
    }

    @Override
    public Collection<String> retrieveArticleUrlsFromFeed() throws IOException {
        return null;
    }

//    public static void main(String[] args) throws IOException {
//        Crawler c = new IlGiornaleCrawler();
//
//        Collection<String> urls = c.retrieveArticleUrlsFromHomePage();
////        for (String url : urls) {
////            try {
////                System.out.println(url);
////                System.out.println("-- " + c.getArticle(url).getTitle());
////            } catch (CrawlerCannotReadArticleException e) {
////                System.err.println("-- "+e.getMessage());
////            }
////        }
//
//        try {
//            Article a = c.getArticle((String)urls.toArray()[0]);
//            System.out.println(a.getTitle());
//            System.out.println(a.getDescription());
//            System.out.println(a.getBody());
//        } catch (CrawlerCannotReadArticleException e) {
//            System.err.println(e.getMessage());
//        }
//    }
}
