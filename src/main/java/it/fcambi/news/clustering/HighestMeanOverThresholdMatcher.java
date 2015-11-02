package it.fcambi.news.clustering;

import it.fcambi.news.model.MatchingArticle;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.News;
import it.fcambi.news.model.MatchingNews;

import java.util.*;
import java.util.stream.Collectors;

/**
 * For each article if similarity is under threshold then discard the link, else take it.
 * Compute cluster similarity as the mean of the weights of these links.
 * The cluster with highest mean similarity will be chosen.
 */
public class HighestMeanOverThresholdMatcher implements Matcher {
    @Override
    public Map<Article, MatchingNews> findBestMatch(Metric metric, Map<Article, List<MatchingArticle>> matchMap, double threshold) {
        Map<Article, MatchingNews> results = new HashMap<>();

        matchMap.entrySet().forEach(entry -> {
            Optional<Map.Entry<News, DoubleSummaryStatistics>> maxEntry = entry.getValue().parallelStream()
                    .filter(match -> match.getSimilarity(metric.getName()) > threshold)
                    .collect(Collectors.groupingByConcurrent(MatchingArticle::getNews, Collectors.summarizingDouble(m -> m.getSimilarity(metric.getName()))))
                    .entrySet().stream().max((a, b) -> Double.compare(a.getValue().getAverage(), b.getValue().getAverage()));

            if (maxEntry.isPresent()) {
                MatchingNews news = new MatchingNews();
                news.setNews(maxEntry.get().getKey());
                news.addSimilarity(metric.getName(), maxEntry.get().getValue().getAverage());
                news.setMatchingArticles(
                        matchMap.get(entry.getKey()).stream().filter(m ->
                                m.getNews().equals(news.getNews())).collect(Collectors.toList())
                );
                results.put(entry.getKey(), news);
            } else
                results.put(entry.getKey(), null);
        });

        return results;
    }

    @Override
    public Map<Article, List<MatchingNews>> getRankedList(Metric metric, Map<Article, List<MatchingArticle>> matchMap, double threshold) {
        Map<Article, List<MatchingNews>> sortedMatchMap = new Hashtable<>();

        matchMap.entrySet().forEach(entry ->

            sortedMatchMap.put(entry.getKey(), entry.getValue().parallelStream()
                    .filter(match -> match.getSimilarity(metric.getName()) > threshold)
                    .collect(Collectors.groupingByConcurrent(MatchingArticle::getNews, Collectors.summarizingDouble(m -> m.getSimilarity(metric.getName()))))
                    .entrySet().parallelStream().map(a -> {
                        MatchingNews news = new MatchingNews();
                        news.setNews(a.getKey());
                        news.addSimilarity(metric.getName(), a.getValue().getAverage());
                        news.setMatchingArticles(
                                matchMap.get(entry.getKey()).stream().filter(m ->
                                        m.getNews().equals(a.getKey())).collect(Collectors.toList())
                        );
                        return news;
                    })
                    .sorted((a, b) -> Double.compare(b.getSimilarity(metric.getName()), a.getSimilarity(metric.getName())))
                    .collect(Collectors.toList()))

        );

        return sortedMatchMap;

    }
}