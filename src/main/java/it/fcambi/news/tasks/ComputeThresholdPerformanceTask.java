package it.fcambi.news.tasks;

import it.fcambi.news.Application;
import it.fcambi.news.async.Task;
import it.fcambi.news.clustering.HighestMeanOverThresholdMatcher;
import it.fcambi.news.clustering.MatchMapGenerator;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.clustering.Matcher;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.*;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

/**
 * Created by Francesco on 09/11/15.
 */
public class ComputeThresholdPerformanceTask extends Task {

    enum c { OK, ERROR }
    protected double thresholdStart;
    protected double thresholdIncrement;
    protected int thresholdLimit;

    protected ThresholdPerformanceResult results;

    private MatchMapGeneratorConfiguration configuration;
    private Metric metric;
    private MatchMapGenerator matchMapGenerator;

    public ComputeThresholdPerformanceTask(MatchMapGeneratorConfiguration c, Metric m) {
        super();
        this.configuration = c;
        this.metric = m;
        this.thresholdStart = 0.1;
        this.thresholdIncrement = 0.1;
        this.thresholdLimit = 9;
    }

    public ComputeThresholdPerformanceTask(MatchMapGeneratorConfiguration c, Metric m, double thresholdStart,
                                           double thresholdIncrement, int thresholdLimit) {
        super();
        this.configuration = c;
        this.metric = m;
        this.thresholdStart = thresholdStart;
        this.thresholdIncrement = thresholdIncrement;
        this.thresholdLimit = thresholdLimit;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return "Compute performance varying threshold for a specified metric.";
    }

    @Override
    protected void executeTask() throws Exception {
        progress.set(0);

        EntityManager em = Application.createEntityManager();

        Clustering manualClustering = em.find(Clustering.class, "manual");

        List<Article> articles = em.createQuery("select a from Article a where key(a.news) = 'manual'", Article.class)
                .getResultList();

        matchMapGenerator = new MatchMapGenerator(configuration);

        long start = System.currentTimeMillis();
        Map<Article, List<MatchingArticle>> matchMap = matchMapGenerator.generateMap(articles, articles);
        long end = System.currentTimeMillis();

        results = new ThresholdPerformanceResult();
        results.setItemCount(articles.size());
        results.setMatchMapGenerationTime(end - start);

        double progressUnit = 1.0/thresholdLimit;

        DoubleStream.iterate(thresholdStart, a -> a + thresholdIncrement).limit(thresholdLimit).forEach(threshold -> {
            Matcher matcher = new HighestMeanOverThresholdMatcher(metric, threshold, manualClustering);
            Map<Article, MatchingNews> pairs = matcher.findBestMatch(matchMap);

            int okCount = pairs.entrySet().stream().mapToInt(entry -> {
                //Model predicts that article is alone in his cluster and that is true
                if (entry.getValue() == null && entry.getKey().getNews(manualClustering).size() == 1)
                    return 1;
                    //OR Model predicts that article is in a cluster and is the right cluster
                else if (entry.getValue() != null &&
                        entry.getKey().getNews(manualClustering).equals(entry.getValue().getNews()))
                    return 1;
                else
                    return 0;
            }).sum();

            results.addThresholdResult(threshold, okCount);
            progress.add(progressUnit);

        });

        progress.set(1);

        em.close();

    }

    @Override
    public double getProgress() {
        double p = (0.05*progress.get());
        if (matchMapGenerator != null)
            p += (0.95*matchMapGenerator.getProgress());
        if (p > 99.9) return 100;
        else return p;
    }

    @Override
    public ThresholdPerformanceResult getResults() {
        return results;
    }
}
