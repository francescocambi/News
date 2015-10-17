package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.MatchingArticle;
import it.fcambi.news.data.WordVector;
import it.fcambi.news.filters.NoiseWordsVectorFilter;
import it.fcambi.news.filters.StandardizeStringFilter;
import it.fcambi.news.matchers.PerformanceMatcher;
import it.fcambi.news.metrics.*;
import it.fcambi.news.model.Article;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Created by Francesco on 16/10/15.
 */
@Path("/matcher-performance")
public class MatcherPerformanceService {

    List<Metric> metrics;

    public PerformanceMatcher getDistances () {

        EntityManager em = Application.getEntityManager();
        List<Article> articles = em
                .createQuery("select a from Article a where a.news is not null", Article.class)
                .getResultList();

        PerformanceMatcher performanceMatcher = new PerformanceMatcher();
        performanceMatcher.setArticlesSet(articles);
        performanceMatcher.computeDistances(metrics);

        em.close();

        return performanceMatcher;

    }


    public Map<String, Object> stats() {

        metrics = new LinkedList<>();
//        metrics.add(new CombinedCosineJaccard());
//        metrics.add(new CosineSimilarity());
        metrics.add(new JaccardSimilarity());
//        metrics.add(new TanimotoSimilarity());

        PerformanceMatcher distances = getDistances();
        double[][][] d = distances.getDistances();
        Article[] articles = distances.getIndex();

        // 0- TP 1- FP 2- TN 3- FN
        int[][] results = new int[metrics.size()][4];

        // For each metric
        for (int k=0; k<metrics.size(); k++) {

            //Take rows as article master
            // and compare it to all the other articles in row
            for (int i=0; i < d.length; i++) {
                double max = metrics.get(k).getMinValue();
                int j_max = 0;
                boolean existsMatching = false;
                for (int j=0; j < d.length; j++) {
                    if (j != i) {
                        // if value if greater than max, then d is the new max
                        if (metrics.get(k).compare(d[i][j][k], max) > 0) {
                            max = d[i][j][k];
                            j_max = j;
                        }
                        if (!existsMatching && articles[j].getNews().getId() == articles[i].getNews().getId())
                            existsMatching = true;
                    }
                }
                // Now I have maximum per row, check if it can be a matching
                if (max > metrics.get(k).getThreshold()) {
                    // TP FP
                    if (articles[i].getNews().getId() == articles[j_max].getNews().getId())
                        results[k][0]++;
                    else
                        results[k][1]++;
                } else {
                    // TN FN
                    if (existsMatching)
                        results[k][3]++;
                    else
                        results[k][2]++;
                }
            }

        }

        Map<String, Object> m = new HashMap<>();
        m.put("metrics", metrics.stream().map(metric -> metric.getName()).toArray());
        m.put("results", results);

        return m;

    }


    public Map<Article, List<MatchingArticle>> generateMatchMap(List<Metric> metrics) {

        EntityManager em = Application.getEntityManager();
        List<Article> articles = em
                .createQuery("select a from Article a where a.news is not null", Article.class)
                .getResultList();

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
                if (article.equals(match)) return;
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

    public Map<Integer, Long> countResults(double treshold, Map<Article, List<MatchingArticle>> matchMap, List<Metric> metrics) {

        // Works on each pair (Article, List<>)
        Map<Integer, Long> results =  matchMap.entrySet().stream().mapToInt(entry -> {

            Optional<MatchingArticle> max = entry.getValue().stream().max((a, b) -> {
                if (a.getSimilarity(0) < b.getSimilarity(0)) return -1;
                if (a.getSimilarity(0) > b.getSimilarity(0)) return 1;
                else return 0;
            });
            int result = -1;
            if (max.isPresent()) {
                System.out.println(entry.getKey().getId()+" "+entry.getKey().getTitle());
                System.out.println(max.get().getArticle().getId()+" "+max.get().getArticle().getTitle());
                System.out.print(
                        max.get().getArticle().getNews().getId() + "-" +
                                entry.getKey().getNews().getId() + " " +
                                max.get().getSimilarities().toString());
                // If max is over treshold
                if (max.get().getSimilarity(0) > treshold) {
                    // If max has is similar to key article
                    if (max.get().getArticle().getNews().equals(entry.getKey().getNews())) {
                        //True positive
                        result = 0;
                    } else {
                        //False positive
                        result = 1;
                    }
                } else if (!max.get().getArticle().getNews().equals(entry.getKey().getNews())) {
                    // True Negative
                    result = 2;
                } else {
                    // False Negative
                    result = 3;
                }
            }
            System.out.println("   "+result);
            return result;
        }).boxed().collect(Collectors.groupingBy(o -> o, Collectors.counting()));

        System.out.println(results.toString());

        return results;

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Double, Map<Integer, Long>> tryDifferentTresholds() {

        List<Metric> metrics = new ArrayList<>();
        metrics.add(new CombinedCosineJaccard());
        metrics.add(new CosineSimilarity());
        metrics.add(new JaccardSimilarity());

        Map<Article, List<MatchingArticle>> matchMap = generateMatchMap(metrics);

        Map<Double, Map<Integer, Long>> results = new HashMap<>();

        DoubleStream.of(1.71, 1.72, 1.73, 1.74).forEach(t -> {
            results.put(t, countResults(t, matchMap, metrics));
        });

        System.out.println(results.toString());

        return results;

    }

}

