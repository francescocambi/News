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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Francesco on 23/12/15.
 */
public class ClusteringPerformanceTaskTwo extends Task {

    protected MatchMapGeneratorConfiguration conf;
    protected Metric metric;
    protected double thresholdStart;
    protected double thresholdIncrement;
    protected int thresholdLimit;
    protected MatcherFactory matcherFactory;

    protected ComputeClusteringPerformanceTaskResults results;

    public ClusteringPerformanceTaskTwo(MatchMapGeneratorConfiguration conf, Metric metric,
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

        progress.set(0);
        EntityManager em = Application.createEntityManager();

        Clustering manual = em.find(Clustering.class, "manual");

        List<Article> trainingSet = em.createQuery("select a from Article a where key(a.news) = 'manual'", Article.class)
                .getResultList();

        List<Article> testSet = new ArrayList<>();

        //Prepare training and test datasets
        IntStream.range(0, trainingSet.size()/3).forEach((x) -> {
            int i = (int) Math.round(Math.random()*(trainingSet.size()-1));
            testSet.add(trainingSet.get(i));
//            trainingSet.remove(i);
        });

        List<News> news = em.createQuery("select n from News n where n.clustering.name = 'manual'", News.class)
                .getResultList();

        Clustering training = new Clustering("training");
        Clustering test = new Clustering("test");

        List<News> trainingNews = generateClustering(trainingSet, news, training);

        List<News> testNews = new LinkedList<>();

//        List<News> testNews = generateClustering(testSet, news, test);

        MatchMapGenerator generator = new MatchMapGenerator(conf);

        double progressIncrement = 0.9/trainingSet.size();

        List<Article> classifiedArticles = new ArrayList<>();
        Clustering clustering = new Clustering();
        Matcher matcher = matcherFactory.createMatcher(metric, thresholdStart, clustering);

        for (int i=0; i < trainingSet.size(); i++) {
            List<Article> articleToCluster = new LinkedList<>();
            articleToCluster.add(trainingSet.get(i));
            Map<Article, List<MatchingArticle>> matchMap = generator.generateMap(articleToCluster, classifiedArticles);
            Map<Article, MatchingNews> bestMatch = matcher.findBestMatch(matchMap);

            bestMatch.keySet().forEach(article -> {
                if (bestMatch.get(article) != null) {
                    article.setNews(clustering, bestMatch.get(article).getNews());
                } else {
                    article.setNews(clustering, new News(clustering));
                    article.getNews(clustering).setDescription(article.getTitle());
                }
                article.getNews(clustering).getArticles().add(article);
                classifiedArticles.add(article);
            });
            progress.add(progressIncrement);
        }

        List<News> generatedNews = classifiedArticles.parallelStream().map(a -> a.getNews(clustering))
                .distinct().collect(Collectors.toList());

        List<News> expectedNews = trainingNews;

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
//                long union = gen.getArticles().size() + exp.getArticles().stream().filter(a -> !gen.getArticles().contains(a)).count();

//                double jaccard = (double) intersection / (double) union;
                double recall = (double)intersection/exp.size();
                double precision = (double)intersection/gen.size();

                double fmeasure = 0.0;
                if (recall > 0 || precision > 0)
                    fmeasure =  2.0*((precision*recall)/(precision+recall));

                return new Pair(gen, exp, fmeasure);
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

        System.out.println("" +
                "\n------------------------------------------------------------\n" +
                "THRESHOLD >> "+thresholdStart+"\n"+
                "Precision W: "+precisionWeighted+"\n" +
                "Recall W: "+recallWeighted+"\n" +
                "Jaccard W: "+jaccardWeighted+"\n" +
                "F-Measure W: "+fWeighted+"\n"+
                "          --------------          \n" +
                "Precision: "+precisionAvg+"\n" +
                "Recall: "+recallAvg+"\n" +
                "Jaccard: "+jaccardAvg+"\n" +
                "F-Measure: "+fAvg+"\n");



//        long okCount = classifiedArticles.stream().filter(a -> a.getNews(manual).getId() == a.getNews(clustering).getId())
//                .count();

//        System.out.print(
//                "------------------------------------------------------------------\n" +
//                        "THRESHOLD >> "+thresholdStart+"\n" +
////                        "  TP: "+truePositive+"   FP: "+falsePositive+"   TN: "+trueNegative+"   FN: "+falseNegative+"\n"+
//                        "  okCount: "+okCount+" on "+classifiedArticles.size()+" articles\n");

        progress.set(1.0);
        em.close();

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

    /*
    Genera una lista di news clone di quelle come parametro news,
    che però hanno come articoli sollo quelli presenti in trainingSet
    Clustering definisce il raggruppamento descritto
     */
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
}
