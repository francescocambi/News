package it.fcambi.news.crawlers;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.Newspaper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Created by Francesco on 24/09/15.
 */
public class LaRepubblicaCrawler implements Crawler {

    private static final String URL_REGEX = "http://[a-z.]*repubblica.it/[a-z0-9A-Z-/+&@#%?=~_|!:,.;]*/news/[a-z0-9A-Z-/+&@#%?=~_|!:,.;]*";
    private static Pattern URL_PATTERN = null;

    @Override
    public Newspaper getNewspaper() {
        return Newspaper.LA_REPUBBLICA;
    }

    private static Pattern getUrlPattern() {
        if (URL_PATTERN == null)
            URL_PATTERN = Pattern.compile(URL_REGEX);
        return URL_PATTERN;
    }

    public Article getArticle(String url) throws IOException, CrawlerCannotReadArticleException {
        Document d;
        try {
            d = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw e;
        }

        Article a = new Article();
        a.setSource(Newspaper.LA_REPUBBLICA);
//        a.setSourceHtml(d.html());
        a.setSourceUrl(url);

        try {
            // Retrieve headline
            Elements headline = d.select("[itemprop=headline name]");
            if (headline.size() == 0) {
                headline = d.select("meta[property=og:title]");
                a.setTitle(headline.get(0).attr("content"));
            } else
                a.setTitle(headline.get(0).text());

        } catch (IndexOutOfBoundsException e) {
            throw new CrawlerCannotReadArticleException("Can't find headline on "+url);
        }

        //Retrieve description
        Elements description = d.select(".summary");
        if (description.size() > 0)
            a.setDescription(description.get(0).text());
        else {
            description = d.select(".eleven > p");
            if (description.size() > 0)
                a.setDescription(description.get(0).text());
        }

        try {
            //Retrieve body
            Elements body = d.select("[itemprop=articleBody]");
            if (body.size() == 0) {
                body = d.select(".post-content");
                if (body.size() == 0) {
                    body = d.select(".content > p");
                    a.setBody(body.get(0).text());
                } else {
                    a.setBody("");
                    body.forEach(p -> a.setBody(a.getBody()+" "+p.text()));
                }
            } else
                a.setBody(d.select("[itemprop=articleBody]").get(0).ownText());
        } catch (IndexOutOfBoundsException e) {
            throw new CrawlerCannotReadArticleException("Can't find article body on "+url);
        }

        /*
        TODO body.text() recupera tutto il testo (corpo, didascalie, ecc...); ownText recupera solo il corpo dell'articolo
         */

        return a;

    }

    public List<String> retrieveArticleUrlsFromHomePage() throws IOException {

        Document d = Jsoup.connect("http://repubblica.it").get();

        //Select only main column
        Elements articles = d.select("#container > .article h1 a:first-of-type");
        articles.addAll(d.select(".main-content[role=main] > .article h1 a:first-of-type"));
        articles.addAll(d.select(".sub-content-1 .article h1 a:first-of-type"));

        Vector<String> urls = new Vector<String>();

        String url;
        for (int i = 0; i<articles.size(); i++) {
            url = articles.get(i).attr("href");
            int queryStringIndex = url.lastIndexOf('?');
            if (queryStringIndex > 0)
                url = url.substring(0, queryStringIndex);
            if (isArticleAtUrlParsable(url))
                urls.add(url);
        }

        return urls;

    }

    public Collection<String> retrieveArticleUrlsFromFeed() throws IOException {

        Document feed = Jsoup.connect("http://www.repubblica.it/rss/homepage/rss2.0.xml").get();

        Elements articles = feed.select("item > link");

        Vector<String> urls = new Vector<String>();

        String url;
        for (int i=0; i<articles.size(); i++) {
            url = articles.get(i).text();
            int queryStringIndex = url.lastIndexOf('?');
            if (queryStringIndex > 0)
                url = url.substring(0, queryStringIndex);
            if (isArticleAtUrlParsable(url))
                urls.add(url);
        }

        return urls;
    }

    private boolean isArticleAtUrlParsable(String url) {
        return getUrlPattern().matcher(url).matches();
    }

//    public static void main(String[] args) {
//        try {
//            Article a = (new LaRepubblicaCrawler()).getArticle("http://www.repubblica.it/motori/sezioni/prodotto/2015/10/12/news/fiat_tipo_2016_nuova-124890865/");
//            System.out.println(a.getBody());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public static void main(String[] args) {
//        try {
//            LaRepubblicaCrawler crawler = new LaRepubblicaCrawler();
//            Collection<String> links = crawler.retrieveArticleUrlsFromHomePage();
//            for (String link:links) {
//                try {
//                    Article a = crawler.getArticle(link);
//                    System.out.println("-- " + a.getTitle());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}