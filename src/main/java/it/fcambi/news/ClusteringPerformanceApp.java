package it.fcambi.news;

import it.fcambi.news.async.Progress;
import it.fcambi.news.clustering.*;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.*;
import it.fcambi.news.tasks.ClusteringPerformanceResults;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Francesco on 13/12/15.
 */
public class ClusteringPerformanceApp {

//    public static void main(String[] args) {
//
//        int parallelismLevel = Runtime.getRuntime().availableProcessors();
//        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", ""+(parallelismLevel-1));
//
//        PersistenceManager pm = new PersistenceManager("it.fcambi.news.jpa.local");
//        EntityManager em = pm.createEntityManager();
//
//        TFDictionary dict = em.find(TFDictionary.class, "italian_stemmed");
//
//        Metric metric = new CosineSimilarity();
//
//        MatchMapGeneratorConfiguration conf = new MatchMapGeneratorConfiguration();
//        conf.setWordVectorFactory(new TFIDFWordVectorFactory(dict))
//                .addMetric(metric)
//                .addTextFilter(new NoiseWordsTextFilter())
//                .addTextFilter(new StemmerTextFilter());
//
//        MatcherFactory matcherFactory = new HighestMeanOverThresholdMatcherFactory();
//
////        Progress progress = new Progress();
//
//        double thresholdStart = 0.45;
//        double thresholdIncrement = 0.01;
//        int thresholdLimit = 11;
//
////        progress.set(0);
//
//        List<Article> articles = em.createQuery("select a from Article a where key(a.news) = 'manual'", Article.class)
//                .getResultList();
//
//        double progressIncrement = 1.0/(articles.size()*thresholdLimit);
//
//        MatchMapGenerator generator = new MatchMapGenerator(conf);
//
//        List<ClusteringPerformanceResults> results = DoubleStream.iterate(thresholdStart, a -> a+thresholdIncrement).limit(thresholdLimit).parallel()
//                .mapToObj(threshold -> {
//
//                    List<Article> classifiedArticles = new ArrayList<>();
//
//                    Clustering clustering = new Clustering();
//                    Matcher matcher = matcherFactory.createMatcher(metric, threshold, clustering);
//
//                    for (int i=0; i < articles.size(); i++) {
//                        List<Article> articleToCluster = new LinkedList<>();
//                        articleToCluster.add(articles.get(i));
//                        Map<Article, List<MatchingArticle>> matchmap =
//                                generator.generateMap(articleToCluster, classifiedArticles);
//                        Map<Article, MatchingNews> bestMatch = matcher.findBestMatch(matchmap);
//
//                        bestMatch.keySet().forEach(article -> {
//                            if (bestMatch.get(article) != null) {
//                                article.setNews(clustering, bestMatch.get(article).getNews());
//                            } else {
//                                article.setNews(clustering, new News(clustering));
//                                article.getNews(clustering).setDescription(article.getTitle());
//                                article.getNews(clustering).setArticles(new ArrayList<>());
//                            }
//                            article.getNews(clustering).getArticles().add(article);
//                            classifiedArticles.add(article);
//                        });
//
////                        progress.add(progressIncrement);
////                        System.out.println("Progress "+progress.get());
//
//                    }
//
//                    Set<News> generatedClustersRes = new HashSet<>();
//                    classifiedArticles.forEach(article -> generatedClustersRes.add(article.getNews(clustering)));
//
//                    List<News> expectedClustersRes = em.createQuery("select n from News n where n.clustering.name = 'manual'", News.class)
//                            .getResultList();
//
//                    if (generatedClustersRes.size() == 0)
//                        throw new IllegalStateException("Empty generatedClusters collection");
//
//                    Collection<News> rows;
//                    Collection<News> cols;
//                    if (generatedClustersRes.size() >= expectedClustersRes.size()) {
//                        //Check congruency between predicted and effective graph
//                        rows = generatedClustersRes;
//                        cols = expectedClustersRes;
//                    } else {
//                        //Check congruency between effective and predicted graph
//                        rows = expectedClustersRes;
//                        cols = generatedClustersRes;
//                    }
//
//                    double[] distribution = rows.stream().mapToDouble(row -> {
//                        return cols.stream().map(col -> {
//
//                            //Compute Jaccard
//                            long intersection = row.getArticles().stream().filter(a -> col.getArticles().contains(a)).collect(Collectors.counting());
//                            long union = Stream.of(row, col).flatMap(l -> l.getArticles().stream()).collect(Collectors.toSet()).size();
//
//                            return (double)intersection/union;
//
//                        }).max(Double::compare).get();
//                    }).toArray();
//
//                    //Compute precision recall
//                    class IRStats {
//                        public double precision;
//                        public double recall;
//
//                        public IRStats(double precision, double recall) {
//                            this.precision = precision;
//                            this.recall = recall;
//                        }
//
//                        public double getPrecision() {
//                            return precision;
//                        }
//
//                        public double getRecall() {
//                            return recall;
//                        }
//                    }
//
//                    List<IRStats> irstats = expectedClustersRes.stream().map(exp -> {
//                        return generatedClustersRes.stream().map(gen -> {
//                            long intersection = exp.getArticles().stream().filter(a -> gen.getArticles().contains(a)).collect(Collectors.counting());
//                            return new IRStats((double)intersection/gen.size(), (double)intersection/exp.size());
//                        }).max((a,b) -> Double.compare(a.precision, b.precision)).get();
//                    }).collect(Collectors.toList());
//
//                    DoubleSummaryStatistics precisionStats = irstats.stream().mapToDouble(IRStats::getPrecision).summaryStatistics();
//                    DoubleSummaryStatistics recallStats = irstats.stream().mapToDouble(IRStats::getRecall).summaryStatistics();
//
//                    double fmeasure = (2.0*(precisionStats.getAverage()*recallStats.getAverage()))/(precisionStats.getAverage()+recallStats.getAverage());
//
//                    DoubleSummaryStatistics jaccardStats = Arrays.stream(distribution).summaryStatistics();
//
//                    //Compute standard deviation
//                    double squaredOffsetSum = Arrays.stream(distribution).map(x -> Math.pow(x-jaccardStats.getAverage(), 2)).sum();
//                    double stdDeviation = Math.sqrt(squaredOffsetSum/jaccardStats.getCount());
//
//                    ClusteringPerformanceResults r = new ClusteringPerformanceResults(threshold);
//                    r.setAveragePrecision(precisionStats.getAverage());
//                    r.setMinPrecision(precisionStats.getMin());
//                    r.setMaxPrecision(precisionStats.getMax());
//
//                    r.setAverageRecall(recallStats.getAverage());
//                    r.setMinRecall(recallStats.getMin());
//                    r.setMaxRecall(recallStats.getMax());
//
//                    r.setAverageFMeasure(fmeasure);
//
//                    r.setAverageJaccard(jaccardStats.getAverage());
//                    r.setStdDevJaccard(stdDeviation);
//                    r.setMinJaccard(jaccardStats.getMin());
//                    r.setMaxJaccard(jaccardStats.getMax());
//
//                    return r;
//
//                }).collect(Collectors.toList());
//
//        System.out.println(results.toString());
//
//        em.close();
//
//    }

    public static void main(String[] args) {
        int[] a = IntStream.iterate(400, i -> i+1).limit(200).parallel().map(i -> {
            double x = 20000000000000000000000.0;
            while (x > 0.000000000000000000000001) {
                x /= 1.000000000000000001;
            }
            return i;
        }).toArray();

        Arrays.asList(a).forEach(System.out::println);
    }

}
