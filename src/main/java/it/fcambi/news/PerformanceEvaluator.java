package it.fcambi.news;

import it.fcambi.news.clustering.HighestMeanOverThresholdMatcher;
import it.fcambi.news.clustering.MatchMapGenerator;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.clustering.Matcher;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.*;

import javax.persistence.EntityManager;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Created by Francesco on 26/10/15.
 */

public class PerformanceEvaluator {

    enum m { TP, TN, FP, FN };

    enum c { OK, ERR};

    static TFDictionary dict;

//    public static void evaluatePerformance() {
//
//        PersistenceManager persistenceManager = new PersistenceManager("it.fcambi.news.jpa.local");
//        EntityManager em = persistenceManager.createEntityManager();
//
//        dict = em.find(TFDictionary.class, "italian_stemmed");
//
//        List<Article> articles = em.createQuery("select a from Article a where a.news is not null",
//                Article.class).getResultList();
////        articles.addAll(em.createQuery("select a from Article a where a.news.articles.size = 1",
////                Article.class).setMaxResults(101).getResultList());
//
//        Metric metric = new CosineSimilarity();
//
//        //Begin hard work!!
//        long start = System.currentTimeMillis();
//
//        MatchMapGenerator generator = new MatchMapGenerator(null);
//
//        Map<Article, List<MatchingArticle>> matchMap = generator.generateMap(articles, articles, metric, dict,
//                (a, b) -> !a.equals(b)
//        );
//
//        long end = System.currentTimeMillis();
//        System.out.println("Match map generation time "+(end-start)+" millis.");
//
//        int pairsCard = matchMap.entrySet().parallelStream().mapToInt(entry -> entry.getValue().size()).sum();
//
//        System.out.println("# of pairs to compare: "+pairsCard);
//
//        NumberFormat percent = NumberFormat.getPercentInstance();
//        percent.setMaximumFractionDigits(1);
//        NumberFormat decimal = NumberFormat.getNumberInstance();
//        decimal.setMaximumFractionDigits(2);
//
//        DoubleStream.iterate(0.1, a -> a+0.01).limit(81).forEach(threshold -> {
//    //        metrics.forEach(metric -> {
//            System.out.println("---------------------------------------------------------------------------------------------------");
//            System.out.println(">>> Results for " + metric.getName() + " similarity with threshold " + decimal.format(threshold) + " <<<");
//            System.out.println("---------------------------------------------------------------------------------------------------");
//
//            Map<m, Long> confusionMatrix = getConfusionMatrix(matchMap, metric.getName(), threshold);
//
//            System.out.println("> " + confusionMatrix.toString());
//
//            long pairsComputed = confusionMatrix.entrySet().stream().mapToLong(Map.Entry::getValue).sum();
//    //            System.out.println("> # of pairs compared: " + pairsComputed);
//
//            double precision = (double) confusionMatrix.getOrDefault(m.TP, 0L) / (confusionMatrix.getOrDefault(m.TP, 0L) + confusionMatrix.getOrDefault(m.FP, 0L));
//            double recall = (double) confusionMatrix.getOrDefault(m.TP, 0L) / (confusionMatrix.getOrDefault(m.TP, 0L) + confusionMatrix.getOrDefault(m.FN, 0L));
//    //            double missRate = (double) confusionMatrix.get("FN") / (confusionMatrix.get("TP") + confusionMatrix.get("FN"));
//    //            double specificity = (double) confusionMatrix.get("TN") / (confusionMatrix.get("FP") + confusionMatrix.get("TN"));
//
//            double fmeasure = 2*(precision*recall)/(precision+recall);
//
//            double positiveRate = (double) (confusionMatrix.getOrDefault(m.TP, 0L)+confusionMatrix.getOrDefault(m.TN, 0L))/pairsComputed;
//
//    //            System.out.println("> Precision " + precision + "\n> Recall " + recall +
//    //                    "\n> Miss Rate " + missRate + "\n> Specificity " + specificity);
//
//            System.out.println("> P: "+percent.format(precision)+"\t R: "+percent.format(recall));
//            System.out.println("> F-MEASURE:  "+decimal.format(fmeasure)+"\t Accuracy: "+percent.format(positiveRate));
//
//        });
//
//        em.close();
//        persistenceManager.close();
//
//    }

    public static void evaluateMatcherPerformance() {

        PersistenceManager persistenceManager = new PersistenceManager("it.fcambi.news.jpa.local");
        EntityManager em = persistenceManager.createEntityManager();

        Clustering manualClustering = em.find(Clustering.class, "manual");
        Clustering generatedClustering = new Clustering();

        dict = em.find(TFDictionary.class, "italian_stemmed");

        List<Article> articles = em.createQuery("select a from Article a where key(a.news) = 'manual'",
                Article.class).getResultList();
//        List<Article> articles1 = em.createQuery("select a from Article a where a.id=98", Article.class).getResultList();
//        List<Article> articles = em.createQuery("select a from Article a where a.news.articles.size > 1", Article.class)
//                .getResultList();
//        articles.addAll(em.createQuery("select a from Article a where a.news.articles.size = 1", Article.class)
//                .setMaxResults(50).getResultList());

        Metric metric = new CosineSimilarity();

        MatchMapGeneratorConfiguration conf = new MatchMapGeneratorConfiguration()
                .addTextFilter(new NoiseWordsTextFilter())
                .addTextFilter(new StemmerTextFilter())
                .addMetric(metric)
                .setWordVectorFactory(new TFIDFWordVectorFactory(dict));
        MatchMapGenerator generator = new MatchMapGenerator(conf);

        //Begin hard work!!
        long start = System.currentTimeMillis();

        Map<Article, List<MatchingArticle>> matchMap = generator.generateMap(articles, articles);

        long end = System.currentTimeMillis();
        System.out.println("Match map generation time "+(end-start)+" millis.");

        NumberFormat percent = NumberFormat.getPercentInstance();
        percent.setMaximumFractionDigits(1);
        NumberFormat decimal = NumberFormat.getNumberInstance();
        decimal.setMaximumFractionDigits(2);

        DoubleStream.iterate(0.1, a -> a+0.01).limit(81).forEach(threshold -> {

            Matcher matcher = new HighestMeanOverThresholdMatcher(metric, threshold, manualClustering);
            Map<Article, MatchingNews> results = matcher.findBestMatch(matchMap);

            //Compute confusion matrix
            Map<c, Long> stats = results.entrySet().stream().map(entry -> {

                if (entry.getValue() == null) {
                    // Article is the only member of his cluster
                    if (entry.getKey().getNews(manualClustering).getArticles().size() == 1) return c.OK;
                    else {
//                        System.err.println("------------------------------------------------------");
//                        System.err.println("!! WRONG MATCH");
//                        System.err.println("ARTICLE -> "+entry.getKey().getTitle());
//                        System.err.println("ACTUAL -> null");
//                        System.err.println("EXPECTED ("+entry.getKey().getNews().getArticles().size()+") -> "+entry.getKey().getNews().getDescription());
                        return c.ERR;
                    }
                } else {
                    // Article is in a cluster
                    // but it's the right cluster?
                    if (entry.getKey().getNews(manualClustering).equals(entry.getValue().getNews())) return c.OK;
                    else {
//                        System.err.println("------------------------------------------------------");
//                        System.err.println("!! WRONG MATCH");
//                        System.err.println("ARTICLE -> "+entry.getKey().getTitle());
//                        System.err.println("ACTUAL ("+entry.getValue().getArticles().size()+") -> " + entry.getValue().getDescription());
//                        System.err.println("EXPECTED ("+entry.getKey().getNews().getArticles().size()+") -> "+entry.getKey().getNews().getDescription());
                        return c.ERR;
                    }
                }

            }).collect(Collectors.groupingBy(a -> a, Collectors.counting()));

            double sum = (stats.getOrDefault(c.OK, 0L) + stats.getOrDefault(c.ERR, 0L));
            double okPercent = stats.getOrDefault(c.OK, 0L) / sum;
            double errPercent = stats.getOrDefault(c.ERR, 0L) / sum;
//            double jaccard = stats.get(c.OK)/stats.get(c.ERR);

            System.out.println("---------------------------------------------------------------------------------------------------");
            System.out.println(">>> Results for " + metric.getName() + " similarity with threshold " + decimal.format(threshold) + " <<<");
            System.out.println("---------------------------------------------------------------------------------------------------");
            System.out.println("> # OK: " + stats.getOrDefault(c.OK, 0L) + "\t # ERR: " + stats.getOrDefault(c.ERR, 0L));
            System.out.println("> % OK: " + percent.format(okPercent) + "\t % ERR: " + percent.format(errPercent));
//            System.out.println("> Jaccard "+percent.format(jaccard));

        });


        em.close();
        persistenceManager.close();

    }

//    private static Map<m, Long> getConfusionMatrix(Map<Article, List<MatchingArticle>> matchMap,
//                                                   String metricName,
//                                                   double threshold,
//                                                   Clustering manualClustering,
//                                                   Clustering generatedClustering) {
//        Iterator<Map<m, Long>> maps = matchMap.entrySet().stream().map(entry -> {
//
//            return entry.getValue().parallelStream().map(matchingArticle -> {
//                //Checks threshold and detect result
//                if (matchingArticle.getSimilarity(metricName) > threshold) {
//                    if (entry.getKey().getNews().equals(matchingArticle.getArticle().getNews()))
//                        return m.TP;
//                    else
//                        return m.FP;
//                } else {
//                    if (entry.getKey().getNews().equals(matchingArticle.getArticle().getNews()))
//                        return m.FN;
//                    else
//                        return m.TN;
//                }
//            }).collect(Collectors.groupingBy(s -> s, Collectors.counting()));
//
//        }).iterator();
//
//        Map<m, Long> results = new HashMap<>();
//        while (maps.hasNext()) {
//            maps.next().forEach( (key, val) -> results.put(key, results.getOrDefault(key, 0L)+val));
//        }
//        return results;
//    }

    public static void main(String[] args) {

        evaluateMatcherPerformance();

    }

}
