package it.fcambi.news.clustering;

import it.fcambi.news.model.*;
import it.fcambi.news.metrics.Metric;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * For each article if similarity is under threshold then discard the link, else take it.
 * Compute cluster similarity as the mean of the weights of these links.
 * The cluster with highest mean similarity will be chosen.
 */
public class HighestMeanOverThresholdMatcher implements Matcher {

    private Metric metric;
    private double threshold;
    private Clustering clustering;

    public HighestMeanOverThresholdMatcher(Metric metric, double threshold, Clustering clustering) {
        this.metric = metric;
        this.threshold = threshold;
        this.clustering = clustering;
    }

    @Override
    public Map<Article, MatchingNews> findBestMatch(Map<Article, List<MatchingArticle>> matchMap) {
        Map<Article, MatchingNews> results = new HashMap<>();

        matchMap.entrySet().forEach(entry -> {
            Optional<Map.Entry<News, DoubleSummaryStatistics>> maxEntry = entry.getValue().parallelStream()
                    .filter(match -> match.getSimilarity(metric.getName()) > threshold)
                    .collect(Collectors.groupingByConcurrent(this::getNews, Collectors.summarizingDouble(m -> m.getSimilarity(metric.getName()))))
                    .entrySet().stream().max((a, b) -> Double.compare(a.getValue().getAverage(), b.getValue().getAverage()));

            if (maxEntry.isPresent()) {
                MatchingNews news = new MatchingNews();
                news.setNews(maxEntry.get().getKey());
                news.addSimilarity(metric.getName(), maxEntry.get().getValue().getAverage());
                news.setMatchingArticles(
                        matchMap.get(entry.getKey()).stream().filter(m ->
                                m.getArticle().getNews(clustering).equals(news.getNews())).collect(Collectors.toList())
                );
                results.put(entry.getKey(), news);
            } else {
                results.put(entry.getKey(), null);
            }
        });

        return results;
    }

    private News getNews(MatchingArticle m) {
        if (m.getArticle().getNews(clustering) == null)
            throw new IllegalArgumentException(m.getArticle().getId()+" does not have matched news from this clustering");
        else
            return m.getArticle().getNews(clustering);
    }

    @Override
    public Map<Article, List<MatchingNews>> getRankedList(Map<Article, List<MatchingArticle>> matchMap) {
        Map<Article, List<MatchingNews>> sortedMatchMap = new Hashtable<>();

        matchMap.entrySet().forEach(entry ->
            sortedMatchMap.put(entry.getKey(), entry.getValue().parallelStream()
                    .filter(match -> match.getSimilarity(metric.getName()) > threshold)
                    .collect(Collectors.groupingByConcurrent(this::getNews, Collectors.summarizingDouble(m -> m.getSimilarity(metric.getName()))))
                    .entrySet().parallelStream().map(a -> {
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