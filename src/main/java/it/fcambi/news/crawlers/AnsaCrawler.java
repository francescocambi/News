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
 * Created by Francesco on 10/10/15.
 */
public class AnsaCrawler implements Crawler {

    @Override
    public Newspaper getNewspaper() {
        return Newspaper.ANSA;
    }

    @Override
    public Article getArticle(String url) throws IOException, CrawlerCannotReadArticleException {

        Document d = Jsoup.connect(url).get();

        Article a = new Article();
//        a.setSourceHtml(d.html());
        a.setSource(Newspaper.ANSA);

        //Retrieve title
        try {
            Element title = d.select("[itemprop=headline]").get(0);
            a.setTitle(title.text());
        } catch (Exception e) {
            throw new CrawlerCannotReadArticleException("Can't find article headline on "+url);
        }

        //Retrieve Description
        try {
            Element desc = d.select(".header-news h2").get(0);
            a.setDescription(desc.text());
        } catch (Exception e) {
            throw new CrawlerCannotReadArticleException("Can't find article description on "+url);
        }

        //Retrieve Body
        try {
            Element body = d.select("[itemprop=articlebody]").get(0);
            a.setBody(body.text());
        } catch (Exception e) {
            throw new CrawlerCannotReadArticleException("Can't find article body on "+url);
        }

        return a;
    }

    @Override
    public List<String> retrieveArticleUrlsFromHomePage() throws IOException {
        List<String> urls = new LinkedList<>();

        Document d = Jsoup.connect("http://www.ansa.it").get();

        //Retrieve main article
        Elements mainlink = d.select("section:lt(1) h3.pp-title a");
        if (mainlink.size() > 0)
            urls.add("http://www.ansa.it/"+mainlink.get(0).attr("href"));

        //Retrive articles in main column
        Elements articles = d.select("div.pp-inner article .news-title a");

        articles.forEach(a -> urls.add("http://www.ansa.it/"+a.attr("href")));

        return urls;
    }

    @Override
    public Collection<String> retrieveArticleUrlsFromFeed() throws IOException {
        Collection<String> urls = new LinkedList<String>();

        Document d = Jsoup.connect("https://www.ansa.it/main/notizie/awnplus/italia/synd/ansait_awnplus_italia_medsynd_Today_Idx.xml").get();

        Elements links = d.select("item > link");
        links.forEach(link -> urls.add(link.text()));

        return urls;
    }

//    public static void main(String[] args) throws IOException {
//        AnsaCrawler c = new AnsaCrawler();
//        Collection<String> links = c.retrieveArticleUrlsFromHomePage();
//        Iterator<String> i = links.iterator();
//        while (i.hasNext()) {
//            try {
//                String s = i.next();
//                System.out.println("-"+s);
//                System.out.println("-- " + c.getArticle(s).getTitle());
//            } catch (CrawlerCannotReadArticleException e) {
//                System.err.println("-- "+e.getMessage());
//            }
//        }
//    }
}
