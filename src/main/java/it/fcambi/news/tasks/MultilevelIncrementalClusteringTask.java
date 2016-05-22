package it.fcambi.news.tasks;

import it.fcambi.news.Application;
import it.fcambi.news.async.Task;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.clustering.MetaMatchMapGenerator;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.model.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 22/02/16.
 */
public class MultilevelIncrementalClusteringTask extends Task {
    
    private List<Article> articlesToBeClustered;
    private Clustering clustering;

    private double metaNewsThreshold;
    private double newsThreshold;

    private MatchMapGeneratorConfiguration conf;

    public MultilevelIncrementalClusteringTask(List<Article> articlesToBeClustered, Clustering clustering,
                                               double metaNewsThreshold, double newsThreshold,
                                               MatchMapGeneratorConfiguration conf) {
        this.articlesToBeClustered = articlesToBeClustered;
        this.clustering = clustering;
        this.metaNewsThreshold = metaNewsThreshold;
        this.newsThreshold = newsThreshold;
        this.conf = conf;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return "Multilevel incremental clustering.";
    }

    @Override
    protected void executeTask() throws Exception {
        progress.set(0);

        EntityManager em = Application.createEntityManager();
        em.getTransaction().begin();

        this.clustering = em.merge(clustering);

        articlesToBeClustered = articlesToBeClustered.stream().map(em::merge).collect(Collectors.toList());

        List<Cluster> existingClusters;
        try {
            existingClusters = em.createQuery("select m from MetaNews m where m.clustering = :clustering", Cluster.class)
                    .setParameter("clustering", this.clustering).getResultList();
        } catch (NoResultException e) {
            existingClusters = new Vector<>();
        }

        long startTime = System.currentTimeMillis();

        // Generates centroid for articles
        this.articlesToBeClustered.stream().parallel()
                .filter(article -> article.getCentroid(this.clustering) == null)
                .forEach(article -> {
                    Centroid centroid = Centroid.builder().setConfiguration(conf).setArticle(article).build();
                    article.setCentroid(this.clustering, centroid);
                });

        MetaMatchMapGenerator matchMapGenerator = new MetaMatchMapGenerator(this.clustering, this.conf);

        final double progressIncrementA = 0.99/articlesToBeClustered.size();

//        Set<MetaNews> metanewsToMerge = new HashSet<>();

        for (int i = 0; i<articlesToBeClustered.size() && !Thread.currentThread().isInterrupted(); i++) {
            Article article = articlesToBeClustered.get(i);

            // Find the best metanews
            MatchingCluster bestMetaNews = findBestMatch(
                    matchMapGenerator.generateClustersMatchMap(article, existingClusters), this.metaNewsThreshold);

            // if article does not fit in any metanews, a new metanews with a new news will be created
            if (bestMetaNews == null) {
                News newNews = News.createForArticle(article, this.clustering);
                newNews = em.merge(newNews);
                article.setNews(clustering, newNews);

                MetaNews newMetaNews = new MetaNews(clustering);
                newMetaNews = em.merge(newMetaNews);
                newMetaNews.addNews(newNews);
                newNews.setMetanews(newMetaNews);

//                metanewsToMerge.add(newMetaNews);

                existingClusters.add(newMetaNews);
            } else {
                // Compute similarity between article and each news inside metanews
                List<MatchingCluster> newsMatchMap = matchMapGenerator.generateClustersMatchMap(article, bestMetaNews.getCluster().getChildren());

                MatchingCluster bestNews = findBestMatch(newsMatchMap, this.newsThreshold);

                if (bestNews == null) {
                    News newNews = News.createForArticle(article, this.clustering);
                    newNews.setMetanews((MetaNews) bestMetaNews.getCluster());
                    newNews = em.merge(newNews);
                    article.setNews(clustering, newNews);
                    ((MetaNews) bestMetaNews.getCluster()).addNews(newNews);
                } else {
                    ((News) bestNews.getCluster()).addArticle(article);
                    article.setNews(this.clustering, (News) bestNews.getCluster());
                }
            }

            progress.add(progressIncrementA);

        }

        long endTime = System.currentTimeMillis();
        System.err.println("Time Elapsed (ms) >> " + (endTime-startTime));

        if (!Thread.currentThread().isInterrupted()) {
            em.getTransaction().commit();
            progress.set(1);
        } else {
            em.getTransaction().rollback();
        }

        em.close();
        
    }

    private MatchingCluster findBestMatch(List<MatchingCluster> matchMap, double threshold) {

        return matchMap.stream().filter(m -> m.getSimilarity("cosine") > threshold)
                .max((a,b) -> Double.compare(a.getSimilarity("cosine"), b.getSimilarity("cosine")))
                .orElse(null);

    }

    public static void execTask(String clusteringName, double metaNewsThreshold, double newsThreshold,
                                String tfDictionary, String stemmerLanguageString) throws Exception {

        EntityManager em = Application.createEntityManager();

        TFDictionary dict = em.find(TFDictionary.class, tfDictionary);
        if (dict == null)
            throw new IllegalArgumentException("TF Dictionary does not exists.");

        Language language = Language.valueOf(stemmerLanguageString.toUpperCase());


        List<Article> articlesToCluster = em.createQuery("select a from Article a", Article.class).getResultList();

        Clustering clustering = new Clustering();
        clustering.setName(clusteringName);
        clustering.setDescription("MULTILEVEL - Metric cosine@"+metaNewsThreshold+"_"+newsThreshold+"; " +
                "Stemming true ("+ language +"); TFIDF true ("+tfDictionary+"); " +
                "Keyword Extraction headline_and_capitals;");

        MatchMapGeneratorConfiguration conf = new MatchMapGeneratorConfiguration()
                .setWordVectorFactory(new TFIDFWordVectorFactory(dict))
                .addMetric(new CosineSimilarity())
                .addTextFilter(new NoiseWordsTextFilter(language))
                .addTextFilter(new StemmerTextFilter(language))
                .setStringToTextFunction(MatchMapGeneratorConfiguration.onlyAlphaSpaceSeparated)
                .setIgnorePairPredicate(MatchMapGeneratorConfiguration.ignoreReflectiveMatch)
                .setKeywordSelectionFunction(MatchMapGeneratorConfiguration.headlineAndCapitalsKeywords);

        MultilevelIncrementalClusteringTask ict = new MultilevelIncrementalClusteringTask(articlesToCluster,
                clustering, metaNewsThreshold, newsThreshold, conf);

        Runnable statusRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        System.err.println(ict.progress.get()*100 + " %");
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        };

        Thread statusThread = new Thread(statusRunnable);
        statusThread.start();

        ict.executeTask();

        statusThread.interrupt();
        em.close();
    }


}
