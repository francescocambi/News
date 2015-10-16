package it.fcambi.news;

import it.fcambi.news.crawlers.*;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.FrontPage;

import javax.persistence.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Francesco on 26/09/15.
 */
public class ArticlesDownloader implements ProgressObservable {

    private static final Logger log = Logger.getLogger(ArticlesDownloader.class.getName());

    Vector<ProgressObserver> observers;

    public ArticlesDownloader() {
        observers = new Vector<ProgressObserver>();
    }

    public void downloadArticles() {
        float statusCompleted = 0F;
        EntityManager em = Application.getEntityManager();

        //Crawl articles
        log.log(Level.INFO, "Article downloader process started.");
        Crawler[] crawlers = new Crawler[6];
        crawlers[0] = new LaRepubblicaCrawler();
        crawlers[1] = new LaStampaCrawler();
        crawlers[2] = new CorriereDellaSeraCrawler();
        crawlers[3] = new AnsaCrawler();
        crawlers[4] = new AdnkronosCrawler();
        crawlers[5] = new IlGiornaleCrawler();

        List<FrontPage> frontPages = new LinkedList<>();
        for (Crawler crawler : crawlers) {
            try {
                // Retrieve articles on the home page
                List<String> urls = crawler.retrieveArticleUrlsFromHomePage();
                List<Article> articles = new LinkedList<>();

                float statusArticleUnit = (75F/crawlers.length)/urls.size();

                // Download each article previously retrieved
                for (String url : urls) {
                    try {
                        articles.add(crawler.getArticle(url));
                    } catch (IOException | CrawlerCannotReadArticleException e) {
                        log.log(Level.WARNING, "Skipped article", e);
                    } finally {
                        statusCompleted += statusArticleUnit;
                        this.updateProgressObservers(statusCompleted);
                    }
                }

                // Create front page and attach articles
                FrontPage p = new FrontPage();
                p.setArticles(articles);
                p.setNewspaper(crawler.getNewspaper());
                frontPages.add(p);

            } catch (Exception e) {
                log.log(Level.SEVERE, "Exception when retrieving articles with "+crawler.getClass().getName(), e);
            }
        }

        log.log(Level.INFO, "Articles download completed. Persisting articles...");

        //Persist articles on db
        float statusPageUnit = 25F/frontPages.size();
        em.getTransaction().begin();

        for (FrontPage page : frontPages) {

            for (Article a : page.getArticles()) {
                List<Article> articles = em.createQuery("select a from Article a " +
                        "where a.title like concat('%', ?1, '%') and a.source=?2", Article.class)
                        .setParameter(1, a.getTitle())
                        .setParameter(2, a.getSource())
                        .getResultList();

                if (articles.size() > 0) {
                    Article article = null;
                    if (articles.size() > 1) {
                        Optional<Article> o = articles.stream().max((x, y) -> x.getCreated().compareTo(y.getCreated()));
                        if (o.isPresent())
                            article = o.get();
                    } else if (articles.size() == 1) {
                        article = articles.get(0);
                    }
                    // Replace detached article with the attached one
                    page.getArticles().set(page.getArticles().indexOf(a), article);
                    log.log(Level.INFO, "Skipped (exists) " + a.getTitle());
                } else {
                    // If article doesn't exists on db
                    // Persist it and attach
                    em.persist(a);
                    log.log(Level.INFO, "Persisted " + a.getTitle() + " from " + a.getSource().name());
                }
            }

            //Persisting front page
            em.persist(page);

            //Update progress
            statusCompleted += statusPageUnit;
            this.updateProgressObservers(statusCompleted);
        }

        em.getTransaction().commit();
        em.close();

        statusCompleted = 100F;
        this.updateProgressObservers(statusCompleted);
        this.flushObservers();

        log.info("Articles download completed");
    }

    private void updateProgressObservers(final float progress) {
        observers.forEach(o -> o.update(progress));
    }

    private void flushObservers() {
        observers.clear();
    }

    public void addProgressObserver(ProgressObserver o) {
        observers.add(o);
    }

    public void removeProgressObserver(ProgressObserver o) {
        observers.remove(o);
    }
}
