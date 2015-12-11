package it.fcambi.news.tasks;

import it.fcambi.news.Application;
import it.fcambi.news.async.Task;
import it.fcambi.news.clustering.MatchMapGenerator;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.clustering.Matcher;
import it.fcambi.news.clustering.MatcherFactory;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.*;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Created by Francesco on 11/12/15.
 */
public class ComputeClusteringPerformanceTask extends Task {

    protected MatchMapGeneratorConfiguration conf;
    protected Metric metric;
    protected double thresholdStart;
    protected double thresholdIncrement;
    protected int thresholdLimit;
    protected MatcherFactory matcherFactory;

    protected Map<Double, ClusteringPerformanceResults> results;

    public ComputeClusteringPerformanceTask(MatchMapGeneratorConfiguration conf, Metric metric,
                                            MatcherFactory mf, double thresholdStart,
                                            double thresholdIncrement, int thresholdLimit) {
        this.conf = conf;
        this.metric = metric;
        this.thresholdStart = thresholdStart;
        this.thresholdIncrement = thresholdIncrement;
        this.thresholdLimit = thresholdLimit;
        this.matcherFactory = mf;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    protected void executeTask() throws Exception {

        /*
        TODO Handle thread interruption
         */

        progress.set(0);
        EntityManager em = Application.createEntityManager();

        results = new ConcurrentHashMap<>();

        List<Article> articles = em.createQuery("select a from Article a where key(a.news) = 'manual'", Article.class)
                .getResultList();

        double progressIncrement = 1.0/(articles.size()*thresholdLimit);

        MatchMapGenerator generator = new MatchMapGenerator(conf);

        DoubleStream.iterate(thresholdStart, a -> a+thresholdIncrement).limit(thresholdLimit).parallel()
                .forEach(threshold -> {

                    List<Article> classifiedArticles = new ArrayList<>();

                    Clustering clustering = new Clustering();
                    Matcher matcher = matcherFactory.createMatcher(metric, threshold, clustering);

                    for (int i=0; i < articles.size(); i++) {
                        List<Article> articleToCluster = new LinkedList<>();
                        articleToCluster.add(articles.get(i));
                        Map<Article, List<MatchingArticle>> matchmap =
                                generator.generateMap(articleToCluster, classifiedArticles);
                        Map<Article, MatchingNews> bestMatch = matcher.findBestMatch(matchmap);

                        bestMatch.keySet().forEach(article -> {
                            if (bestMatch.get(article) != null) {
                                article.setNews(clustering, bestMatch.get(article).getNews());
                            } else {
                                article.setNews(clustering, new News(clustering));
                                article.getNews(clustering).setDescription(article.getTitle());
                                article.getNews(clustering).setArticles(new ArrayList<>());
                            }
                            article.getNews(clustering).getArticles().add(article);
                            classifiedArticles.add(article);
                        });

                        progress.add(progressIncrement);

                    }

                    Set<News> generatedClustersRes = new HashSet<>();
                    classifiedArticles.forEach(article -> generatedClustersRes.add(article.getNews(clustering)));

                    List<News> expectedClustersRes = em.createQuery("select n from News n where n.clustering.name = 'manual'", News.class)
                            .getResultList();

                    if (generatedClustersRes.size() == 0)
                        throw new IllegalStateException("Empty generatedClusters collection");

                    Collection<News> rows;
                    Collection<News> cols;
                    if (generatedClustersRes.size() >= expectedClustersRes.size()) {
                        //Check congruency between predicted and effective graph
                        rows = generatedClustersRes;
                        cols = expectedClustersRes;
                    } else {
                        //Check congruency between effective and predicted graph
                        rows = expectedClustersRes;
                        cols = generatedClustersRes;
                    }

                    double[] distribution = rows.stream().mapToDouble(row -> {
                        return cols.stream().map(col -> {

                            //Compute Jaccard
                            long intersection = row.getArticles().stream().filter(a -> col.getArticles().contains(a)).collect(Collectors.counting());
                            long union = Stream.of(row, col).flatMap(l -> l.getArticles().stream()).collect(Collectors.toSet()).size();

                            return (double)intersection/union;

                        }).max(Double::compare).get();
                    }).toArray();

                    //Compute precision recall
                    class IRStats {
                        public double precision;
                        public double recall;

                        public IRStats(double precision, double recall) {
                            this.precision = precision;
                            this.recall = recall;
                        }

                        public double getPrecision() {
                            return precision;
                        }

                        public double getRecall() {
                            return recall;
                        }
                    }

                    List<IRStats> irstats = expectedClustersRes.stream().map(exp -> {
                        return generatedClustersRes.stream().map(gen -> {
                            long intersection = exp.getArticles().stream().filter(a -> gen.getArticles().contains(a)).collect(Collectors.counting());
                            return new IRStats((double)intersection/gen.size(), (double)intersection/exp.size());
                        }).max((a,b) -> Double.compare(a.precision, b.precision)).get();
                    }).collect(Collectors.toList());

                    DoubleSummaryStatistics precisionStats = irstats.stream().mapToDouble(IRStats::getPrecision).summaryStatistics();
                    DoubleSummaryStatistics recallStats = irstats.stream().mapToDouble(IRStats::getRecall).summaryStatistics();

                    double fmeasure = (2.0*(precisionStats.getAverage()*recallStats.getAverage()))/(precisionStats.getAverage()+recallStats.getAverage());

                    DoubleSummaryStatistics jaccardStats = Arrays.stream(distribution).summaryStatistics();

                    //Compute standard deviation
                    double squaredOffsetSum = Arrays.stream(distribution).map(x -> Math.pow(x-jaccardStats.getAverage(), 2)).sum();
                    double stdDeviation = Math.sqrt(squaredOffsetSum/jaccardStats.getCount());

                    ClusteringPerformanceResults r = new ClusteringPerformanceResults();
                    r.averagePrecision = precisionStats.getAverage();
                    r.minPrecision = precisionStats.getMin();
                    r.maxPrecision = precisionStats.getMax();

                    r.averageRecall = recallStats.getAverage();
                    r.minRecall = recallStats.getMin();
                    r.maxRecall = recallStats.getMax();

                    r.averageFMeasure = fmeasure;

                    r.averageJaccard = jaccardStats.getAverage();
                    r.stdDevJaccard = stdDeviation;
                    r.minJaccard = jaccardStats.getMin();
                    r.maxJaccard = jaccardStats.getMax();

                    results.put(threshold, r);

                });

        em.close();

        if (progress.get() >= 0.9999)
            progress.set(1.0);

    }

    @Override
    public Map<Double, ClusteringPerformanceResults> getResults() {
        return results;
    }
}
