package it.fcambi.news.tasks;

import it.fcambi.news.Application;
import it.fcambi.news.async.Task;
import it.fcambi.news.clustering.MatchMapGenerator;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.clustering.Matcher;
import it.fcambi.news.model.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 06/11/15.
 */
public class IncrementalClusteringTask extends Task {

    private MatchMapGeneratorConfiguration matchMapConfiguration;
    private Matcher matcher;
    private List<Article> articlesToBeClustered;
    private Clustering clustering;

    public IncrementalClusteringTask(MatchMapGeneratorConfiguration matchMapConfiguration, Matcher matcher,
                                     List<Article> articlesToBeClustered, Clustering clustering) {
        super();
        this.matchMapConfiguration = matchMapConfiguration;
        this.matcher = matcher;
        this.articlesToBeClustered = articlesToBeClustered;
        this.clustering = clustering;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return "Groups articles about the same event.";
    }

    @Override
    protected void executeTask() throws Exception {
        progress.set(0);

        if (Thread.interrupted()) return;

        EntityManager em = Application.createEntityManager();
        em.getTransaction().begin();
        em.merge(clustering);

        articlesToBeClustered = articlesToBeClustered.stream().map(em::merge).collect(Collectors.toList());

        //Configure match map generator
        MatchMapGenerator matchMapGenerator = new MatchMapGenerator(matchMapConfiguration);

        //Prepare set with all articles from existing clusters
        String select = "select a from Article a where key(a.news)=:clusteringName";
        List<Article> classifiedArticles;
        try {
            classifiedArticles = em.createQuery(select, Article.class)
                    .setParameter("clusteringName", clustering.getName())
                    .getResultList();
        } catch (NoResultException e) {
            classifiedArticles = new Vector<>();
        }

        final double progressIncrementA = 0.85/articlesToBeClustered.size();

        //TODO Remove
        classifiedArticles.forEach(a -> {
            assert a.getNews(clustering) != null;
        });

        Set<News> newsToMerge = new HashSet<>();

        //Find a fitting cluster for each article one by one
        // updating classifiedArticles each iteration
        for (int i=0; i<articlesToBeClustered.size() && !Thread.currentThread().isInterrupted(); i++) {
            Article article = articlesToBeClustered.get(i);

            //Match map generation
            List<Article> articleToCluster = new LinkedList<>();
            articleToCluster.add(article);
            Map<Article, List<MatchingArticle>> matchMap = matchMapGenerator.generateMap(
                    articleToCluster, classifiedArticles);

            //Find best match
            Map<Article, MatchingNews> bestMatchMap = matcher.findBestMatch(matchMap);
            MatchingNews bestMatchingNews = bestMatchMap.get(article);
            if (bestMatchingNews != null) {
                // Cluster found, add article to cluster
                article.setNews(clustering, bestMatchingNews.getNews());
                article.getNews(clustering).addArticle(article);
            } else {
                // New cluster for this article
                News newCluster = News.createForArticle(article, clustering);
                newCluster = em.merge(newCluster);
                article.setNews(clustering, newCluster);
            }
            newsToMerge.add(article.getNews(clustering));

            //Updates classifiedArticle
            classifiedArticles.add(article);

            progress.add(progressIncrementA);

        }

        final double progressIncrementB = 0.05/articlesToBeClustered.size();


//        System.out.println("# of clusters obtained "+newsToMerge.size());


        if (!Thread.currentThread().isInterrupted()) {
            em.getTransaction().commit();
            progress.set(1);
        } else {
            em.getTransaction().rollback();
        }

        em.close();
    }
}
