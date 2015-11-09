package it.fcambi.news.clustering;

import it.fcambi.news.Application;
import it.fcambi.news.async.Task;
import it.fcambi.news.model.*;

import javax.persistence.EntityManager;
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
        return "Groups article that talks about the same event.";
    }

    @Override
    protected void executeTask() throws Exception {
        progress = 0;

        EntityManager em = Application.getEntityManager();

        MatchMapGenerator matchMapGenerator = new MatchMapGenerator(matchMapConfiguration);

        Set<Article> classifiedArticles = clustering.getClusters().parallelStream()
                .map(News::getArticles).flatMap(List::stream).collect(Collectors.toSet());

        final double progressIncrementA = 85/articlesToBeClustered.size();

        for (int i=1; i<articlesToBeClustered.size(); i++) {

            Map<Article, List<MatchingArticle>> matchMap = matchMapGenerator.generateMap(
                    articlesToBeClustered.subList(i-1, i), classifiedArticles);

            Map<Article, MatchingNews> bestMatchMap = matcher.findBestMatch(matchMap);

            Article article = articlesToBeClustered.get(i-1);
            MatchingNews bestMatchingNews = bestMatchMap.get(article);
            if (bestMatchingNews != null) {
                article.setNews(clustering, bestMatchingNews.getNews());
                article.getNews(clustering).addArticle(article);
            } else {
                News newCluster = News.createForArticle(article, clustering);
                article.setNews(clustering, newCluster);
            }
            classifiedArticles.add(article);

            progress += progressIncrementA;

        }

        //TODO Merge all articles
        final double progressIncrementB = 15/articlesToBeClustered.size();

        em.getTransaction().begin();
        articlesToBeClustered.forEach(article -> {
            em.merge(article);
            progress += progressIncrementB;
        });
        em.getTransaction().commit();

        progress = 100;

        em.close();
    }
}
