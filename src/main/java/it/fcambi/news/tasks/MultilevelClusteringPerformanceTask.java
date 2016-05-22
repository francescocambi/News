package it.fcambi.news.tasks;

import it.fcambi.news.Application;
import it.fcambi.news.async.Task;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.clustering.MetaMatchMapGenerator;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Created by Francesco on 24/02/16.
 */
public class MultilevelClusteringPerformanceTask extends Task {

    protected MatchMapGeneratorConfiguration conf;
    protected Metric metric;
    protected List<Newspaper> newspapersToExclude = new ArrayList<>();

    protected float testSetFraction;

    protected double metaNewsThresholdSeed = 0.0;
    protected int metaNewsThresholdLimit = 0;
    protected double newsThresholdSeed = 0.0;
    protected int newsThresholdLimit = 0;

    protected ClusteringPerformanceTaskResults results;

    public MultilevelClusteringPerformanceTask(MatchMapGeneratorConfiguration conf, Metric metric,
                                               List<Newspaper> newspapersToExclude, float testSetFraction,
                                               double metaNewsThresholdSeed, int metaNewsThresholdLimit,
                                               double newsThresholdSeed, int newsThresholdLimit) {
        this.conf = conf;
        this.metric = metric;
        this.newspapersToExclude = newspapersToExclude;
        this.testSetFraction = testSetFraction;
        this.metaNewsThresholdSeed = metaNewsThresholdSeed;
        this.metaNewsThresholdLimit = metaNewsThresholdLimit;
        this.newsThresholdSeed = newsThresholdSeed;
        this.newsThresholdLimit = newsThresholdLimit;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return "Multilevel clustering performance computation task";
    }

    @Override
    protected void executeTask() throws Exception {

        progress.set(0);
        EntityManager em = Application.createEntityManager();

        String query = "select a from Article a where key(a.news) = 'manual'";
        if (this.newspapersToExclude.size() > 0)
            query += " and a.source not in :npToExclude";

        TypedQuery<Article> trainingSetQuery = em.createQuery(query, Article.class);

        if (this.newspapersToExclude.size() > 0)
            trainingSetQuery.setParameter("npToExclude", this.newspapersToExclude);

        List<Article> trainingSet = trainingSetQuery.getResultList();
        trainingSet.forEach(a -> a.enableParallelism());

        List<Article> testSet = new ArrayList<>();

        //Prepare training and test datasets
        IntStream.range(0, Math.round(trainingSet.size()*testSetFraction)).forEach((x) -> {
            int i = (int) Math.round(Math.random()*(trainingSet.size()-1));
            testSet.add(trainingSet.get(i));
            trainingSet.remove(i);
        });

        List<News> news = em.createQuery("select n from News n where n.clustering.name = 'manual'", News.class)
                .getResultList();

        Clustering manualTraining = new Clustering("manual_training");
        Clustering manualTest = new Clustering("manual_test");

        List<News> trainingNews = generateClustering(trainingSet, news, manualTraining);

//        List<News> testNews = generateClustering(testSet, news, manualTest);

        double progressIncrement = 1.0/( (trainingSet.size()+testSet.size()) * this.metaNewsThresholdLimit*this.newsThresholdLimit );

        // --- Try for each threshold ---

        PrintWriter outfile = new PrintWriter(new FileWriter("thresholds_output.csv"));

        outfile.println("metanewsThreshold,newsThreshold,averagePrecisionWeighted,averageRecallWeighted,averageFMeasureWeighted," +
                "averageJaccardWeighted,averagePrecision,averageRecall,averageFMeasure,averageJaccard,numGeneratedClusters," +
                "numExpectedClusters,numOfArticles,timeElapsed_ms");

        Runnable statusRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        System.err.println(progress.get()*100 + " %");
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        };

        Thread statusThread = new Thread(statusRunnable);
        statusThread.start();

        DoubleStream.iterate(this.metaNewsThresholdSeed, x -> x + 0.01).limit(this.metaNewsThresholdLimit).parallel().forEach(metanewsThreshold -> {

                    DoubleStream.iterate(this.newsThresholdSeed, x -> x + 0.01).limit(this.newsThresholdLimit).parallel().forEach(newsThreshold -> {

                        long startTime = System.currentTimeMillis();

                        Clustering trainingClustering = new Clustering("_training_"+metanewsThreshold+"_"+newsThreshold);

                        // Generates centroid for articles
                        trainingSet.stream().parallel()
                                .filter(article -> article.getCentroid(trainingClustering) == null)
                                .forEach(article -> {
                                    Centroid centroid = Centroid.builder().setConfiguration(conf).setArticle(article).build();
                                    article.setCentroid(trainingClustering, centroid);
                                });

                        MetaMatchMapGenerator generator = new MetaMatchMapGenerator(trainingClustering, this.conf);

                        // Clustering and evaluating training set

                        List<News> generatedNewsTraining =
                                clusterArticles(trainingSet, generator, progressIncrement, trainingClustering, metanewsThreshold, newsThreshold);

                        long endTime = System.currentTimeMillis();

                        MultilevelClusteringPerformanceResults trainingResults = evaluateClustering(generatedNewsTraining, trainingNews, metanewsThreshold, newsThreshold);

                        outfile.println(trainingResults.getMetanewsThreshold() + "," +
                                trainingResults.getThreshold() + "," + trainingResults.getAveragePrecisionWeighted() + "," +
                                trainingResults.getAverageRecallWeighted() + "," +
                                trainingResults.getAverageFMeasureWeighted() + "," + trainingResults.getAverageJaccardWeighted() + "," +
                                trainingResults.getAveragePrecision() + "," + trainingResults.getAverageRecall() + "," +
                                trainingResults.getAverageFMeasure() + "," + trainingResults.getAverageJaccard() + "," +
                                trainingResults.getNumGeneratedClusters() + "," + trainingResults.getNumExpectedClusters() + "," +
                                trainingResults.getNumOfArticles() + "," + (endTime-startTime));

                        //Cleanup phase
                        trainingSet.forEach(a -> {
                            a.getNewsMap().remove(trainingClustering.getName());
                            a.getCentroidsMap().remove(trainingClustering.getName());
                        });

                    });
                });

        // --- END ---

        statusThread.interrupt();

        outfile.flush();
        outfile.close();

        //TODO Clustering and evaluating test set

        progress.set(1.0);
        em.close();
    }

    private List<News> clusterArticles(List<Article> trainingSet, MetaMatchMapGenerator generator,
                                          double progressIncrement, Clustering clustering, double metaNewsThreshold,
                                          double newsThreshold) {

        List<Cluster> existingClusters = new ArrayList<>();

        for (int i=0; i < trainingSet.size(); i++) {
            Article article = trainingSet.get(i);

            MatchingCluster bestMetaNews = findBestMatch(
                    generator.generateClustersMatchMap(article, existingClusters), metaNewsThreshold);

            if (bestMetaNews == null) {
                News newNews = News.createForArticle(article, clustering);
                article.setNews(clustering, newNews);

                MetaNews newMetaNews = new MetaNews(clustering);
                newMetaNews.addNews(newNews);
                newNews.setMetanews(newMetaNews);

                existingClusters.add(newMetaNews);
            } else {
                List<MatchingCluster> newsMatchMap = generator.generateClustersMatchMap(article, bestMetaNews.getCluster().getChildren());

                MatchingCluster bestNews = findBestMatch(newsMatchMap, newsThreshold);

                if (bestNews == null) {
                    News newNews = News.createForArticle(article, clustering);
                    newNews.setMetanews((MetaNews) bestMetaNews.getCluster());
                    article.setNews(clustering, newNews);
                    ((MetaNews) bestMetaNews.getCluster()).addNews(newNews);
                } else {
                    ((News) bestNews.getCluster()).addArticle(article);
                    article.setNews(clustering, (News) bestNews.getCluster());
                }
            }

            progress.add(progressIncrement);

//            System.out.println(progress.get()*100+" %");

        }

        return existingClusters.stream().flatMap(mn -> ((MetaNews) mn).getNews().stream()).collect(Collectors.toList());
    }

    /*
    Creates a list of news cloning news parameter,
    but new groups have only the items in articlesSet
     */
    private List<News> generateClustering(List<Article> articlesSet, List<News> news, Clustering training) {
        return news.stream().map(n -> {
            News clone = new News(training);
            clone.setId(n.getId());
            clone.setDescription(n.getDescription());
            n.getArticles().stream().filter(articlesSet::contains)
                    .forEach(a -> {
                        clone.addArticle(a);
                        a.setNews(training, clone);
                    });
            return clone;
        }).filter(n -> n.getArticles().size() > 0)
                .collect(Collectors.toList());
    }

    private MultilevelClusteringPerformanceResults evaluateClustering(List<News> generatedNews, List<News> expectedNews,
                                                            double metanewsThreshold, double newsThreshold) {
        class Pair {

            public Pair(News gen, News exp, double similarity) {
                this.gen = gen;
                this.exp = exp;
                this.similarity = similarity;
            }

            News gen;
            News exp;
            double similarity;
        }

        List<Pair> pairs = generatedNews.parallelStream().map(gen ->
                expectedNews.parallelStream().map(exp -> {
                    //Jaccard tra x e y
                    long intersection = gen.getArticles().stream().filter(a -> exp.getArticles().contains(a)).count();
                    long union = gen.getArticles().size() + exp.getArticles().stream().filter(a -> !gen.getArticles().contains(a)).count();

                    double jaccard = (double) intersection / (double) union;

                    return new Pair(gen, exp, jaccard);
                }).max((a, b) -> Double.compare(a.similarity, b.similarity)).get()
        ).collect(Collectors.toList());

        class Stats {
            public Stats(double precision, double recall, double jaccard, int size) {
                this.precision = precision;
                this.recall = recall;
                this.jaccard = jaccard;
                this.size = size;
            }

            double precision;
            double recall;
            double jaccard;
            int size;
        }

        List<Stats> stats = pairs.parallelStream().map(pair -> {
            long intersection = pair.gen.getArticles().stream().filter(a -> pair.exp.getArticles().contains(a)).count();
            long union = pair.gen.getArticles().size() + pair.exp.getArticles().stream().filter(a -> !pair.gen.getArticles().contains(a)).count();
            return new Stats((double)intersection/pair.gen.size(), (double)intersection/pair.exp.size(), (double)intersection/(double)union, pair.gen.size());
        }).collect(Collectors.toList());

        //stats vanno aggregate facendo la media pesata sulla dimensione dei cluster
        double precisionSum = stats.stream().mapToDouble(s -> s.precision*s.size).sum();
        double recallSum = stats.stream().mapToDouble(s -> s.recall*s.size).sum();
        double jaccardSum = stats.stream().mapToDouble(s -> s.jaccard*s.size).sum();
        double itemCount = stats.stream().mapToInt(s -> s.size).sum();
        double precisionWeighted = (precisionSum/itemCount);
        double recallWeighted = (recallSum/itemCount);
        double jaccardWeighted = (jaccardSum/itemCount);
        double fWeighted = 2.0*((precisionWeighted*recallWeighted)/(precisionWeighted+recallWeighted));

        double precisionAvg = stats.stream().mapToDouble(s -> s.precision).summaryStatistics().getAverage();
        double recallAvg = stats.stream().mapToDouble(s -> s.recall).summaryStatistics().getAverage();
        double jaccardAvg = stats.stream().mapToDouble(s -> s.jaccard).summaryStatistics().getAverage();
        double fAvg = 2.0*((precisionAvg*recallAvg)/(precisionAvg+recallAvg));

        int articlesCardinality = expectedNews.stream().mapToInt(News::size).sum();

        return new MultilevelClusteringPerformanceResults(metanewsThreshold, newsThreshold, precisionWeighted, recallWeighted, fWeighted, jaccardWeighted,
                precisionAvg, recallAvg, fAvg, jaccardAvg, generatedNews.size(), expectedNews.size(), articlesCardinality);

    }

    private MatchingCluster findBestMatch(List<MatchingCluster> matchMap, double threshold) {

        return matchMap.stream().filter(m -> m.getSimilarity("cosine") > threshold)
                .max((a,b) -> Double.compare(a.getSimilarity("cosine"), b.getSimilarity("cosine")))
                .orElse(null);

    }

    @Override
    public ClusteringPerformanceTaskResults getResults() {
        return results;
    }

    public static void execTask(double metaNewsThresholdSeed, int metaNewsThresholdLimit, double newsThresholdSeed,
                                int newsThresholdLimit, String tfidfDictionary, String languageString) throws Exception {

        EntityManager em = Application.createEntityManager();

        TFDictionary dict = em.find(TFDictionary.class, tfidfDictionary);
        if (dict == null)
            throw new IllegalArgumentException("TFDictionary does not exists.");

        Language language = Language.valueOf(languageString.toUpperCase());

        MatchMapGeneratorConfiguration conf = new MatchMapGeneratorConfiguration()
                .setWordVectorFactory(new TFIDFWordVectorFactory(dict))
                .addMetric(new CosineSimilarity())
                .addTextFilter(new NoiseWordsTextFilter(language))
                .addTextFilter(new StemmerTextFilter(language))
                .setStringToTextFunction(MatchMapGeneratorConfiguration.onlyAlphaSpaceSeparated)
                .setIgnorePairPredicate(MatchMapGeneratorConfiguration.ignoreReflectiveMatch)
                .setKeywordSelectionFunction(MatchMapGeneratorConfiguration.headlineAndCapitalsKeywords);

        MultilevelClusteringPerformanceTask cpt = new MultilevelClusteringPerformanceTask(conf, new CosineSimilarity(),
                new ArrayList<>(), 0F, metaNewsThresholdSeed, metaNewsThresholdLimit, newsThresholdSeed, newsThresholdLimit);

        cpt.executeTask();
    }
}
