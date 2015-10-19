package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.MatchingArticle;
import it.fcambi.news.PersistenceManager;
import it.fcambi.news.data.WordVector;
import it.fcambi.news.filters.NoiseWordsVectorFilter;
import it.fcambi.news.filters.StandardizeStringFilter;
import it.fcambi.news.metrics.*;
import it.fcambi.news.model.Article;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Created by Francesco on 16/10/15.
 */
@Path("/matcher-performance")
public class MatcherPerformanceService {

    /**
     *
     * @param articles Set of articles to match
     * @param metrics List of metrics to compute
     * @param matchingPredicate Takes two articles (a,b) and return true if a and b must be matched, false otherwise
     * @return Map that bind each article with a list of possible matchings
     */
    public Map<Article, List<MatchingArticle>> generateMatchMap(List<Article> articles, List<Metric> metrics,
                                                                BiPredicate<Article, Article> matchingPredicate) {

        NoiseWordsVectorFilter noiseFilter = new NoiseWordsVectorFilter();

        // Source Article -> Similarities with all articles (it included)
        Map<Article, List<MatchingArticle>> matchMap = new HashMap<>();

        articles.parallelStream().forEach( article -> {

            // Prepare source vector
            WordVector w = new WordVector();
            w.addStringFilter(new StandardizeStringFilter());
            w.setSourceText(article.getTitle()+" "+article.getDescription(), "[ ]+");
            noiseFilter.filter(w);

            int[] sourceFrequencies = w.getWordsFrequencyIn(article.getBody(), "[ ]+");

            List<MatchingArticle> matchingArticles = new ArrayList<>();

            articles.forEach( match -> {
                if (!matchingPredicate.test(article, match)) return;
                // Prepare destination vector
                int[] matchingFrequencies = w.getWordsFrequencyIn(match.getBody(), "[ ]+");
                // Compute similarity between articles
                MatchingArticle a = new MatchingArticle();
                a.setArticle(match);

                metrics.stream().forEachOrdered(metric -> a.addSimilarity(
                        metric.compute(sourceFrequencies, matchingFrequencies)
                ));

                matchingArticles.add(a);

            });

            matchMap.put(article, matchingArticles);

        });

        return matchMap;

    }

    public Map<String, Long> countResults(double treshold, Map<Article, List<MatchingArticle>> matchMap, int metricIndex) {

        // Works on each pair (Article, List<>)
        return matchMap.entrySet().stream().map(entry -> {

            Optional<MatchingArticle> max = entry.getValue().stream().max((a, b) -> {
                if (a.getSimilarity(metricIndex) < b.getSimilarity(metricIndex)) return -1;
                if (a.getSimilarity(metricIndex) > b.getSimilarity(metricIndex)) return 1;
                else return 0;
            });
            String result = "";
            if (max.isPresent()) {
//                System.out.println(entry.getKey().getId()+" "+entry.getKey().getTitle());
//                System.out.println(max.get().getArticle().getId()+" "+max.get().getArticle().getTitle());
//                System.out.print(
//                        max.get().getArticle().getNews().getId() + "-" +
//                                entry.getKey().getNews().getId() + " " +
//                                max.get().getSimilarities().toString());
                // If max is over treshold
                if (max.get().getSimilarity(metricIndex) > treshold) {
                    // If max has is similar to key article
                    if (max.get().getArticle().getNews().equals(entry.getKey().getNews())) {
                        //True positive
                        result = "TP";
                    } else {
                        //False positive
                        result = "FP";
                    }
                } else if (!max.get().getArticle().getNews().equals(entry.getKey().getNews())) {
                    // True Negative
                    result = "TN";
                } else {
                    // False Negative
                    result = "FN";
                }
            }
//            System.out.println("   "+result);
            return result;
        }).collect(Collectors.groupingBy(o -> o, Collectors.counting()));

//        System.out.println(results.toString());

//        return results;

    }

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public Map<Double, Map<Integer, Long>> tryDifferentTresholds() {
//
//        List<Metric> metrics = new ArrayList<>();
//        metrics.add(new CombinedCosineJaccard());
//        metrics.add(new CosineSimilarity());
//        metrics.add(new JaccardSimilarity());
//
//        Map<Article, List<MatchingArticle>> matchMap = generateMatchMap(metrics);
//
//        Map<Double, Map<Integer, Long>> results = new HashMap<>();
//
//        DoubleStream.of(1.71, 1.72, 1.73, 1.74).forEach(t -> {
//            results.put(t, countResults(t, matchMap, metrics));
//        });
//
//        System.out.println(results.toString());
//
//        return results;
//
//    }

    public static void main(String[] args) {
        //Preparation
        MatcherPerformanceService service = new MatcherPerformanceService();

        PersistenceManager persistenceManager = new PersistenceManager("it.fcambi.news.jpa.local");
        EntityManager em = persistenceManager.createEntityManager();
        List<Article> articles = em.createQuery("select a from Article a where a.news is not null", Article.class).getResultList();

        List<Metric> metrics = new ArrayList<>();
        metrics.add(new CombinedCosineJaccard());
        metrics.add(new CosineSimilarity());
        metrics.add(new JaccardSimilarity());
        metrics.add(new TanimotoSimilarity());

        //Begin hard work!!

        Map<Article, List<MatchingArticle>> matchMap = service.generateMatchMap(articles, metrics, (a,b) -> {
            return !a.equals(b) && a.getSource().equals(b.getSource());
        });

        matchMap.entrySet().forEach(entry -> {

            System.out.println(entry.getKey().getNews().getId()+" - "+entry.getKey().getTitle());

            IntStream.range(0, metrics.size()).forEach(i -> {
                MatchingArticle best = entry.getValue().stream()
                        .max((a, b) -> metrics.get(i).compare(a.getSimilarity(i), b.getSimilarity(i)))
                        .get();
                System.out.println(metrics.get(i).getName()+" = "+best.getSimilarity(i)+" >> "+
                        best.getArticle().getNews().getId() + " - " + best.getArticle().getTitle());
            });
            System.out.println("-----------------------------------------------------------------");

        });

//        Map<Double, Map<String, Long>> results = new HashMap<>();
//
//        DoubleStream.iterate(0.5, i -> i + 0.1).limit(5).forEach(t -> {
//            results.put(t, service.countResults(t, matchMap, 0));
//        });
//
//        System.out.println(results.toString());

        em.close();
        persistenceManager.close();

    }

}

