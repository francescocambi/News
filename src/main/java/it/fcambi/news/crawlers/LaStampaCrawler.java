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
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Created by Francesco on 24/09/15.
 */
public class LaStampaCrawler implements Crawler {

    private static final String HOMEPAGE_URL = "http://www.lastampa.it";
    private static final String RSS_URL = "http://www.lastampa.it/rss.xml";
    private static final String MULTIMEDIA_REGEX = "http://[a-z.]*lastampa.it/[a-z0-9A-Z-/+&@#%?=~_|!:,.;]*/multimedia/[a-z0-9A-Z-/+&@#%?=~_|!:,.;]*";
    private static final String URL_REGEX = "http://[a-z.]*lastampa.it[a-z0-9A-Z-/+&@#%?=~_|!:,.;]*/pagina.html";
    private static Pattern MULTIMEDIA_PATTERN;
    private static Pattern URL_PATTERN;

    @Override
    public Newspaper getNewspaper() {
        return Newspaper.LA_STAMPA;
    }

    private static Pattern getMultimediaPattern() {
        if (MULTIMEDIA_PATTERN == null)
            MULTIMEDIA_PATTERN = Pattern.compile(MULTIMEDIA_REGEX);
        return MULTIMEDIA_PATTERN;
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
        a.setSourceHtml(d.html());
        a.setSourceUrl(url);

        Elements bodyParagraphs;
        try {
            // Retrieve headline
            Elements headline = d.select("meta[property=og:title]");
            if (headline.size() > 0)
                a.setTitle(headline.get(0).attr("content"));
            else {
                headline = d.select("#vatican_dettaglio > h1");
                a.setTitle(headline.get(0).text());
            }
        } catch (IndexOutOfBoundsException e) {
            throw new CrawlerCannotReadArticleException("Can't find headline on "+url);
        }

        try {
            //Retrieve description
            Elements description = d.select("meta[property=og:description]");
            if (description.size() > 0)
                a.setDescription(description.get(0).attr("content"));
            else {
                description = d.select("#vatican_dettaglio > h2");
                a.setDescription(description.get(0).text());
            }

        } catch (IndexOutOfBoundsException e) {
            throw new CrawlerCannotReadArticleException("Can't find article description on "+url);
        }

        String body = "";
        try {
            //Retrieve body
            bodyParagraphs = d.select("[itemprop=articleBody] > p");
            for (int i = 0; i < bodyParagraphs.size(); i++)
                body += bodyParagraphs.get(i).ownText()+" ";
        } catch (IndexOutOfBoundsException e) {
            throw new CrawlerCannotReadArticleException("Can't find article body on "+url);
        }

        // Creates new article object and return
        a.setSource(Newspaper.LA_STAMPA);
        a.setBody(body);

        return a;

    }

    public List<String> retrieveArticleUrlsFromHomePage() throws IOException {
        List<String> urls = new LinkedList<>();

        Document d = Jsoup.connect(HOMEPAGE_URL).get();

        // Top articles from apertura
        Elements links = d.select("#apertura .ls-box-object .ls-box-titolo > a:lt(1)");

        //Articles from main column
        links.addAll(d.select(".ls-colonna1 .ls-colonna1A .ls-box-object .ls-box-titolo > a:lt(1)"));

        Pattern p = Pattern.compile("http://[a-z0-9A-Z-/+&@#%?=~_|!:,.;]*");
        String url;
        for (Element a : links) {
            url = a.attr("href");
            if (!p.matcher(url).matches())
                url = HOMEPAGE_URL+url;
            if (isArticleParsable(url))
                urls.add(url);
        }

        return urls;
    }

    public Collection<String> retrieveArticleUrlsFromFeed() throws IOException {

        Document d = Jsoup.connect(RSS_URL).get();
        Elements articles = d.select("item > link");

        Vector<String> urls = new Vector<String>();

        String url;
        for (int i=0; i<articles.size(); i++) {
            url = articles.get(i).text();
            if (isArticleParsable(url));
                urls.add(url);
        }

        return urls;
    }

    private boolean isArticleParsable(String url) {
        return (getUrlPattern().matcher(url).matches() && !getMultimediaPattern().matcher(url).matches());
    }



//    public static void main(String[] args) throws IOException{
//        Crawler c = new LaStampaCrawler();
//        for (String url : c.retrieveArticleUrlsFromHomePage()) {
//            try {
//                System.out.println(url);
//                System.out.println("-- "+c.getArticle(url).getTitle());
//            } catch (CrawlerCannotReadArticleException e) {
//                System.err.println("-- "+e.getMessage());
//            }
//        }
//    }

}
