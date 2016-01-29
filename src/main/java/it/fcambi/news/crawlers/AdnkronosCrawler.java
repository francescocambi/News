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
 * Created by Francesco on 11/10/15.
 */
public class AdnkronosCrawler implements Crawler {

    @Override
    public Newspaper getNewspaper() {
        return Newspaper.ADNKRONOS;
    }

    @Override
    public Article getArticle(String url) throws IOException, CrawlerCannotReadArticleException {

        Document d = Jsoup.connect(url).get();
        Article a = new Article();
        a.setSourceUrl(url);
//        a.setSourceHtml(d.html());
        a.setSource(Newspaper.ADNKRONOS);

        //Retrieve Title
        try {
            Element title = d.select("h1.title").get(0);
            a.setTitle(title.text());
        } catch (Exception e) {
            throw new CrawlerCannotReadArticleException("Can't find article headline on "+url);
        }

        //No description!!

        //Retrieve Body
        a.setBody("");
        try {
            Elements bodyPs = d.select("article .innerFull > p:not(.articleDate)");
            bodyPs.forEach(p -> a.setBody(a.getBody()+" "+p.text()));
        } catch (Exception e) {
            throw new CrawlerCannotReadArticleException("Can't find article body on "+url);
        }

        return a;
    }

    @Override
    public List<String> retrieveArticleUrlsFromHomePage() throws IOException {
        LinkedList<String> urls = new LinkedList<>();

        Document d = Jsoup.connect("http://www.adnkronos.com").get();

        //Top news
        Elements articles = d.select("#bigEvent .title > a:first-of-type");
        articles.addAll(d.select("#leftCol .inner > .previewGroup article .title a:first-of-type"));
        articles.addAll(d.select("#innerLeft article .title a:first-of-type"));

        articles.forEach(a -> urls.add(a.attr("href")));

        return urls;

    }

    @Override
    public Collection<String> retrieveArticleUrlsFromFeed() throws IOException {
        return null;
    }

//    public static void main(String[] args) throws IOException {
//
//        Crawler c = new AdnkronosCrawler();
//
//        Collection<String> articles = c.retrieveArticleUrlsFromHomePage();
//
//        System.out.println("\n\n");
//
//        articles.forEach(url -> {
//            try {
//                System.out.println("-- "+c.getArticle(url).getTitle());
//            } catch (Exception e) {
//                System.err.println(e.getMessage());
//                e.printStackTrace();
//            }
//        });
//    }
}
