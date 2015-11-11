package it.fcambi.news.tasks;

import it.fcambi.news.Application;
import it.fcambi.news.Logging;
import it.fcambi.news.async.Task;
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
public class ArticlesDownloaderTask extends Task {

    private static final Logger log = Logging.registerLogger(ArticlesDownloaderTask.class.getName());

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return "Collects articles from each newspaper website";
    }

    @Override
    public void executeTask() {
        progress.set(0);
        EntityManager em = Application.getEntityManager();

        //Crawl articles
        log.info("Article downloader process started.");
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
                        progress.add(statusArticleUnit);
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

        log.info("Articles download completed. Persisting articles...");

        //Persist articles on db
        float statusPageUnit = 25F/frontPages.size();
        em.getTransaction().begin();

        for (FrontPage page : frontPages) {

            for (Article a : page.getArticles()) {
                List<Article> articles = em.createQuery("select a from Article a " +
                        "where (a.title like concat('%', ?1,'%') or a.sourceUrl=?2) and a.source=?3 order by a.created desc", Article.class)
                        .setParameter(1, a.getTitle())
                        .setParameter(2, a.getSourceUrl())
                        .setParameter(3, a.getSource())
                        .getResultList();

                if (articles.size() > 0) {
                    Article article = articles.get(0);
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
            progress.add(statusPageUnit);
        }

        em.getTransaction().commit();
        em.close();

        progress.set(100);

        log.info("Articles download completed");
    }

}
