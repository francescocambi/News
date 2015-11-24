package it.fcambi.news.crawlers;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.Newspaper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 24/09/15.
 */
public class CorriereDellaSeraCrawler implements Crawler {

    @Override
    public Newspaper getNewspaper() {
        return Newspaper.CORRIERE_DELLA_SERA;
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
        String body = "";
        try {
            // Retrieve headline
            Elements headline = d.select("[itemprop=name]");
            if (headline.size() > 0) {
                a.setTitle(headline.get(0).text());
                //Removes corriere.it suffix
                int i = a.getTitle().lastIndexOf("- Corriere");
                if (i > 0) a.setTitle(a.getTitle().substring(0, i).trim());
            }
            else {
                headline = d.select("[itemprop=headline]");
                if (headline.size() > 0)
                    a.setTitle(headline.get(0).text());
                else {
                    headline = d.select("[property=og:title]");
                    a.setTitle(headline.get(0).attr("content"));
                }
            }

        } catch (IndexOutOfBoundsException e) {
            throw new CrawlerCannotReadArticleException("Can't find headline on "+url);
        }

        try {
            //Retrieve description
            Elements description = d.getElementsByClass("article-subtitle");
            if (description.size() > 0)
                a.setDescription(description.get(0).text());
            else {
                description = d.select("[itemprop=description]");
                if (description.size() > 0)
                    a.setDescription(description.get(0).text());
                else
                    System.err.println("WARN: Can't find article description on "+url);
            }

        } catch (IndexOutOfBoundsException e) {
            throw new CrawlerCannotReadArticleException("Can't find article description on "+url);
        }

        try {
            //Retrieve body
            bodyParagraphs = d.select(".chapter p");
            if (bodyParagraphs.size() > 0) {
                while (!bodyParagraphs.isEmpty()) {
                    body += bodyParagraphs.get(0).text() + " ";
                    bodyParagraphs.remove(0);
                }
            } else {
                bodyParagraphs = d.select("[itemprop=articleBody] > p");
                if (bodyParagraphs.size() > 0)
                    for (int i = 0; i < bodyParagraphs.size(); i++)
                        body += bodyParagraphs.get(i).text() + " ";
                else
                    bodyParagraphs.get(0); //Causes exception
            }
            if (body.replace("\\p{Space}", "").length() == 0)
                throw new IllegalStateException();
        } catch (IndexOutOfBoundsException | IllegalStateException e) {
            throw new CrawlerCannotReadArticleException("Can't find article body on "+url);
        }

        // Creates new article object and return
        a.setSource(Newspaper.CORRIERE_DELLA_SERA);
        a.setBody(body);

        return a;


    }

    public List<String> retrieveArticleUrlsFromHomePage() throws IOException{
        Collection<String> urls = new LinkedList<>();

        Document d = Jsoup.connect("http://corriere.it").get();

        // Apertura Straordinaria
        Elements links = d.select("[data-vr-zone=Apertura Straordinaria] .title_art > a:lt(1)");
        // Top articles
        links.addAll(d.select(".main-content .title_art > a:first-of-type"));
        links.addAll(d.select("#colonnaNotizie .title_art > a:first-of-type"));

        //Loads package1 div articles
        Document package1 = Jsoup.connect("http://www.corriere.it/includes_methode/cache/mixedZoneMethode2014_bottom.shtml").get();

        // Main column articles
        links.addAll(package1.select("article .title_art > a:first-of-type"));

        links.forEach(a -> urls.add(a.attr("href")));

//        Elements links = d.select("article a");
//        Iterator<Element> i = links.iterator();
//        Collection<String> urls = new HashSet<String>();
//        while (i.hasNext()) {
//            urls.add(i.next().attr("href"));
//        }
//
//        urls = this.filterUrls(urls);

        return this.filterUrls(urls);
    }

    private List<String> filterUrls(Collection<String> sourceUrls) {
        //Pattern for videos
        Pattern videoPattern = Pattern.compile("http://video.corriere.it/.*");
        //Pattern for foto-gallery and cards
        Pattern fotoGalleryPattern = Pattern.compile("(.*)/(foto-gallery|cards)/(.*)");

        List<String> urls = sourceUrls.stream().filter(url ->  url.startsWith("http")
                                        && !videoPattern.matcher(url).matches()
                                        && !fotoGalleryPattern.matcher(url).matches())
                .collect(Collectors.toList());

        return urls;
    }

    public Collection<String> retrieveArticleUrlsFromFeed() throws IOException {

        Document d = Jsoup.connect("http://xml.corriereobjects.it/rss/homepage.xml").get();
        Elements links = d.select("item > link");

        Iterator<Element> i = links.iterator();
        Collection<String> urls = new HashSet<String>();
        while (i.hasNext())
            urls.add(i.next().text());

        urls = this.filterUrls(urls);

        return urls;
    }

//    public static void main(String[] args) throws IOException {
//
//        Crawler c = new CorriereDellaSeraCrawler();
//        Collection<String> urls = c.retrieveArticleUrlsFromHomePage();
//
//        for (String url : urls) {
//            try {
//                System.out.println(url);
//                System.out.println("-- "+c.getArticle(url).getTitle());
//            } catch (CrawlerCannotReadArticleException e) {
//                System.err.println(e.getMessage());
//            }
//        }
//
//    }
}
