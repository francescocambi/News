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
import java.util.stream.IntStream;
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

    protected ComputeClusteringPerformanceTaskResults results;

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

        List<Article> trainingSet = em.createQuery("select a from Article a where key(a.news) = 'manual'", Article.class)
                .getResultList().subList(0, 710);

        List<Article> testSet = new ArrayList<>();

        //Prepare training and test datasets
        IntStream.range(0, trainingSet.size()/3).forEach((x) -> {
            int i = (int) Math.round(Math.random()*(trainingSet.size()-1));
            testSet.add(trainingSet.get(i));
            trainingSet.remove(i);
        });

        List<News> news = em.createQuery("select n from News n where n.clustering.name = 'manual'", News.class)
                .getResultList();

        Clustering training = new Clustering("training");
        Clustering test = new Clustering("test");

        List<News> trainingNews = generateClustering(trainingSet, news, training);

        List<News> testNews = generateClustering(testSet, news, test);

        double progressIncrement = 1.0/( (trainingSet.size()+testSet.size()) * thresholdLimit);

        MatchMapGenerator generator = new MatchMapGenerator(conf);

        // For each threshold, compute performances on training and test dataset
        results = new ComputeClusteringPerformanceTaskResults();

        results.setTrainingSet( DoubleStream.iterate(thresholdStart, a -> a+thresholdIncrement).limit(thresholdLimit)
                .mapToObj(threshold -> {

                    Clustering clustering = new Clustering();

                    List<Article> trainingClassified = getArticles(trainingSet, progressIncrement, generator, threshold, clustering);

                    return getClusteringPerformanceResults(threshold, clustering, trainingClassified, trainingNews);

                }).collect(Collectors.toConcurrentMap(ClusteringPerformanceResults::getThreshold, r -> r)) );

        results.setTestSet( DoubleStream.iterate(thresholdStart, a -> a+thresholdIncrement).limit(thresholdLimit)
                .mapToObj(threshold -> {

                    Clustering clustering = new Clustering();

                    List<Article> testClassified = getArticles(testSet, progressIncrement, generator, threshold, clustering);

                    return getClusteringPerformanceResults(threshold, clustering, testClassified, testNews);

                }).collect(Collectors.toConcurrentMap(ClusteringPerformanceResults::getThreshold, r -> r)) );

        em.close();

        if (progress.get() >= 0.9999)
            progress.set(1.0);

    }

    private List<News> generateClustering(List<Article> trainingSet, List<News> news, Clustering training) {
        return news.stream().map(n -> {
            News clone = new News(training);
            clone.setDescription(n.getDescription());
            n.getArticles().stream().filter(trainingSet::contains)
                    .forEach(a -> {
                        clone.addArticle(a);
                        a.setNews(training, clone);
                    });
            return clone;
        }).filter(n -> n.getArticles().size() > 0)
                .collect(Collectors.toList());
    }

    private ClusteringPerformanceResults getClusteringPerformanceResults(double threshold,
                                                                         Clustering clustering,
                                                                         List<Article> classifiedArticles,
                                                                         List<News> expectedClustersRes) {
        Set<News> generatedClustersRes = new HashSet<>();
        classifiedArticles.forEach(article -> generatedClustersRes.add(article.getNews(clustering)));

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

        List<Double> distribution = rows.stream().mapToDouble(row -> {
            return cols.stream().map(col -> {

                //Compute Jaccard
                long intersection = row.getArticles().stream().filter(a -> col.getArticles().contains(a)).collect(Collectors.counting());
                long union = Stream.of(row, col).flatMap(l -> l.getArticles().stream()).collect(Collectors.toSet()).size();

                return (double)intersection/union;

            }).max(Double::compare).get();
        }).boxed().collect(Collectors.toList());

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

            public double getFMeasure() {
                return (precision > 0 || recall > 0) ? 2.0*((precision*recall)/(precision+recall)) : 0;
            }
        }

        List<IRStats> irstats = expectedClustersRes.stream().map(exp -> {
            return generatedClustersRes.stream().map(gen -> {
                long intersection = exp.getArticles().stream().filter(a -> gen.getArticles().contains(a)).collect(Collectors.counting());
                return new IRStats((double)intersection/gen.size(), (double)intersection/exp.size());
            }).max((a,b) -> Double.compare(a.getFMeasure(), b.getFMeasure())).get();
        }).collect(Collectors.toList());

        DoubleSummaryStatistics precisionStats = irstats.stream().mapToDouble(IRStats::getPrecision).summaryStatistics();
        DoubleSummaryStatistics recallStats = irstats.stream().mapToDouble(IRStats::getRecall).summaryStatistics();

        double fmeasure = (2.0*(precisionStats.getAverage()*recallStats.getAverage()))/(precisionStats.getAverage()+recallStats.getAverage());

        DoubleSummaryStatistics jaccardStats = distribution.stream().mapToDouble(x -> x).summaryStatistics();

        //Compute standard deviation
        double squaredOffsetSum = distribution.stream().mapToDouble(x -> Math.pow(x-jaccardStats.getAverage(), 2)).sum();
        double stdDeviation = Math.sqrt(squaredOffsetSum/jaccardStats.getCount());

        ClusteringPerformanceResults r = new ClusteringPerformanceResults(threshold);
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

        return r;
    }

    private List<Article> getArticles(List<Article> trainingSet, double progressIncrement,
                                      MatchMapGenerator generator, double threshold, Clustering clustering) {
        List<Article> classifiedArticles = new ArrayList<>();
        Matcher matcher = matcherFactory.createMatcher(metric, threshold, clustering);

        for (int i=0; i < trainingSet.size(); i++) {
            List<Article> articleToCluster = new LinkedList<>();
            articleToCluster.add(trainingSet.get(i));
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
        return classifiedArticles;
    }

    @Override
    public ComputeClusteringPerformanceTaskResults getResults() {
        return results;
    }
}
