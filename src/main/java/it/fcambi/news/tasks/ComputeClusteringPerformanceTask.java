package it.fcambi.news.tasks;

import it.fcambi.news.Application;
import it.fcambi.news.Logging;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Francesco on 11/12/15.
 */
public class ComputeClusteringPerformanceTask extends Task {

    private static Logger log = Logging.registerLogger("ClusteringPerformance");

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

        Clustering manual = em.find(Clustering.class, "manual");

        List<Article> trainingSet = em.createQuery("select a from Article a where key(a.news) = 'manual'", Article.class)
                .getResultList();
//                .getResultList().stream().filter(a -> a.getNews(manual).size() > 1).collect(Collectors.toList());

        List<Article> testSet = new ArrayList<>();

        //Prepare training and test datasets
        IntStream.range(0, trainingSet.size()/3).forEach((x) -> {
            int i = (int) Math.round(Math.random()*(trainingSet.size()-1));
            testSet.add(trainingSet.get(i));
//            trainingSet.remove(i);
        });

        List<News> news = em.createQuery("select n from News n where n.clustering.name = 'manual'", News.class)
                .getResultList();
//                .getResultList().stream().filter(n -> n.size() > 1).collect(Collectors.toList());

        Clustering training = new Clustering("training");
        Clustering test = new Clustering("test");

        List<News> trainingNews = generateClustering(trainingSet, news, training);

        List<News> testNews = new LinkedList<>();

//        List<News> testNews = generateClustering(testSet, news, test);

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

//        results.setTestSet( DoubleStream.iterate(thresholdStart, a -> a+thresholdIncrement).limit(thresholdLimit)
//                .mapToObj(threshold -> {
//
//                    Clustering clustering = new Clustering();
//
//                    List<Article> testClassified = getArticles(testSet, progressIncrement, generator, threshold, clustering);
//
//                    return getClusteringPerformanceResults(threshold, clustering, testClassified, testNews);
//
//                }).collect(Collectors.toConcurrentMap(ClusteringPerformanceResults::getThreshold, r -> r)) );

        em.close();

        if (progress.get() >= 0.9999)
            progress.set(1.0);

    }

    private List<News> generateClustering(List<Article> trainingSet, List<News> news, Clustering training) {
        return news.stream().map(n -> {
            News clone = new News(training);
            clone.setId(n.getId());
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

        log.info(
                "# of clusters obtained "+generatedClustersRes.size()+"\n" +
                "# of clusters expected "+expectedClustersRes.size()+"\n"
        );

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

//        List<Double> distribution = rows.stream().mapToDouble(row -> {
//            return cols.stream().map(col -> {
//
//                //Compute Jaccard
//                long intersection = row.getArticles().stream().filter(a -> col.getArticles().contains(a)).collect(Collectors.counting());
//                long union = Stream.of(row, col).flatMap(l -> l.getArticles().stream()).collect(Collectors.toSet()).size();
//
//                return (double)intersection/union;
//
//            }).max(Double::compare).get();
//        }).boxed().collect(Collectors.toList());

        //Compute precision recall
        class IRStats {
            public News generated;
            public News expected;
            public double precision;
            public double recall;

            public double jaccard;

            public IRStats(News generated, News expected, double precision, double recall, double jaccard) {
                this.generated = generated;
                this.expected = expected;
                this.precision = precision;
                this.recall = recall;
                this.jaccard = jaccard;
            }

            public double getPrecision() {
                return precision;
            }

            public double getRecall() {
                return recall;
            }

            public double getSize() {
                return (generated != null) ? generated.size() : 0.0;
            }

            public double getFMeasure() {
                return (precision > 0 || recall > 0) ? 2.0*((precision*recall)/(precision+recall)) : 0;
            }
        }

        List<IRStats> irstats = expectedClustersRes.stream().map(exp -> {
            Optional<IRStats> s = generatedClustersRes.stream().map(gen -> {
                long intersection = exp.getArticles().stream().filter(a -> gen.getArticles().contains(a)).collect(Collectors.counting());
                long union = Stream.concat(exp.getArticles().stream(), gen.getArticles().stream()).collect(Collectors.toSet()).size();
                return new IRStats(gen, exp, (double)intersection/gen.size(), (double)intersection/exp.size(), (double)intersection/union);
            }).max((a,b) -> Double.compare(a.jaccard, b.jaccard));
            if (!s.isPresent())
                return new IRStats(null, exp, 0.0, 0.0, 0.0);
//            generatedClustersRes.remove(s.get().generated);
            return s.get();
        }).collect(Collectors.toList());

//        log.info("Best matching cluster pairs");
//        log.info("EXPECTED - GENERATED - PRECISION - RECALL - FMEASURE - JACCARD");
//        irstats.forEach(x -> {
//            log.info(x.expected.getId()+"-"+x.expected.getDescription()+"-"+x.generated.getId()+"-"+x.generated.getDescription()+"-"+x.precision+"-"+x.recall+"-"+x.getFMeasure()+"-"+x.jaccard);
//        });

        DoubleSummaryStatistics precisionStats = irstats.stream().mapToDouble(s -> s.precision*s.getSize()).summaryStatistics();
        DoubleSummaryStatistics recallStats = irstats.stream().mapToDouble(s -> s.recall*s.getSize()).summaryStatistics();

//        double fmeasure = (2.0*(precisionStats.getAverage()*recallStats.getAverage()))/(precisionStats.getAverage()+recallStats.getAverage());

//        DoubleSummaryStatistics jaccardStats = distribution.stream().mapToDouble(x -> x).summaryStatistics();
        DoubleSummaryStatistics jaccardStats = irstats.stream().mapToDouble(s -> s.jaccard*s.getSize()).summaryStatistics();

        //Compute standard deviation
//        double squaredOffsetSum = distribution.stream().mapToDouble(x -> Math.pow(x-jaccardStats.getAverage(), 2)).sum();
//        double stdDeviation = Math.sqrt(squaredOffsetSum/jaccardStats.getCount());

        ClusteringPerformanceResults r = new ClusteringPerformanceResults(threshold);
        r.averagePrecision = precisionStats.getSum()/classifiedArticles.size();
//        r.minPrecision = precisionStats.getMin();
//        r.maxPrecision = precisionStats.getMax();

        r.averageRecall = recallStats.getSum()/classifiedArticles.size();
//        r.minRecall = recallStats.getMin();
//        r.maxRecall = recallStats.getMax();

        r.averageFMeasure = 2.0*((r.averagePrecision*r.averageRecall)/(r.averagePrecision+r.averageRecall));

        r.averageJaccard = jaccardStats.getSum()/classifiedArticles.size();
//        r.stdDevJaccard = stdDeviation;
//        r.minJaccard = jaccardStats.getMin();
//        r.maxJaccard = jaccardStats.getMax();

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
