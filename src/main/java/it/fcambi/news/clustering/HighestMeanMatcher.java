package it.fcambi.news.clustering;

import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 02/11/15.
 */

/**
 * Compute cluster similarity as the mean of the weights of links.
 * The cluster with highest mean similarity will be chosen.
 */
public class HighestMeanMatcher implements Matcher {

    private Metric metric;
    private double threshold;
    private Clustering clustering;

    public HighestMeanMatcher(Metric metric, double threshold, Clustering clustering) {
        this.metric = metric;
        this.threshold = threshold;
        this.clustering = clustering;
    }

    @Override
    public Map<Article, MatchingNews> findBestMatch(Map<Article, List<MatchingArticle>> matchMap) {
        Map<Article, MatchingNews> results = new HashMap<>();

        matchMap.entrySet().forEach(entry -> {
            Optional<Map.Entry<News, DoubleSummaryStatistics>> maxEntry = entry.getValue().parallelStream()
                    .collect(Collectors.groupingByConcurrent(this::getNews, Collectors.summarizingDouble(m -> m.getSimilarity(metric.getName()))))
                    .entrySet().stream().max((a, b) -> Double.compare(a.getValue().getAverage(), b.getValue().getAverage()));

            if (maxEntry.isPresent() && maxEntry.get().getValue().getAverage() > threshold) {
                MatchingNews news = new MatchingNews();
                news.setNews(maxEntry.get().getKey());
                news.addSimilarity(metric.getName(), maxEntry.get().getValue().getAverage());
                news.setMatchingArticles(
                        matchMap.get(entry.getKey()).stream().filter(m ->
                        m.getArticle().getNews(clustering).equals(news.getNews())).collect(Collectors.toList())
                );
                results.put(entry.getKey(), news);
            } else
                results.put(entry.getKey(), null);
        });
        return results;
    }

    private News getNews(MatchingArticle m) {
        return m.getArticle().getNews(clustering);
    }

    @Override
    public Map<Article, List<MatchingNews>> getRankedList(Map<Article, List<MatchingArticle>> matchMap) {
        Map<Article, List<MatchingNews>> sortedMatchMap = new Hashtable<>();

        matchMap.entrySet().forEach(entry ->
            sortedMatchMap.put(entry.getKey(), entry.getValue().parallelStream()
                    .collect(Collectors.groupingByConcurrent(this::getNews, Collectors.summarizingDouble(m -> m.getSimilarity(metric.getName()))))
                    .entrySet().stream().map(a -> {
                        MatchingNews news = new MatchingNews();
                        news.setNews(a.getKey());
                        news.addSimilarity(metric.getName(), a.getValue().getAverage());
                        news.setMatchingArticles(
                                matchMap.get(entry.getKey()).stream().filter(m ->
                                        m.getArticle().getNews(clustering).equals(a.getKey())).collect(Collectors.toList())
                        );

                        return news;
                    })
                    .sorted((a, b) -> Double.compare(b.getSimilarity(metric.getName()), a.getSimilarity(metric.getName())))
                    .collect(Collectors.toList()))
        );

        return sortedMatchMap;

    }
}
