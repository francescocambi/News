package it.fcambi.news.relevance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.fcambi.news.model.News;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 26/12/15.
 */
public class NewsRelevance {

    private News news;
    /**
     * Maps timestamps with relevance of news at that instant
     */
    private Map<Long, Double> relevances;

    public NewsRelevance() {}

    public NewsRelevance(News news) {
        this.news = news;
        this.relevances = new ConcurrentHashMap<>();
    }

    public NewsRelevance(News news, Map<Long, Double> relevances) {
        this.news = news;
        this.relevances = relevances;
    }

    public News getNews() {
        return news;
    }

    public Map<Long, Double> getRelevances() {
        return relevances;
    }

    public void setRelevances(Map<Long, Double> relevances) {
        this.relevances = relevances;
    }

    public void putRelevance(Date d, Double r) {
        relevances.put(d.getTime(), r);
    }

    public void getRelevance(Date d) {
        relevances.get(d.getTime());
    }

    @JsonIgnore
    public double getAverageRelevance() {
        return relevances.entrySet().stream().mapToDouble(e -> e.getValue()).average().orElse(0.0);
    }

    @JsonIgnore
    public double getRelevanceSum() {
        return relevances.entrySet().stream().mapToDouble(e -> e.getValue()).sum();
    }

    public long getLifetime() {
        List<Map.Entry<Long, Double>> sortedRelevances = relevances.entrySet().parallelStream()
                .sorted((a,b) -> Long.compare(a.getKey(), b.getKey()))
                .collect(Collectors.toList());

        long periodStart = -1;
        long periodEnd = -1;
        long lifetimeMilliseconds = 0;
        for (int i = 0; i < sortedRelevances.size(); i++) {
            if (periodStart < 0 && sortedRelevances.get(i).getValue() > 0)
                periodStart = sortedRelevances.get(i).getKey();
            else if (periodStart > 0 && sortedRelevances.get(i).getValue() == 0) {
                periodEnd = sortedRelevances.get(i).getKey();
                lifetimeMilliseconds += (periodEnd - periodStart);
                periodStart = -1;
                periodEnd = -1;
            }
        }

        if (periodStart > 0 && periodEnd < 0) {
            periodEnd = sortedRelevances.get(sortedRelevances.size()-1).getKey()+3600000;
            lifetimeMilliseconds += periodEnd-periodStart;
        }

        return lifetimeMilliseconds;
    }

    public long getTimeRange() {
        List<Map.Entry<Long, Double>> nonZeroRelevances = relevances.entrySet().parallelStream()
                .filter(x -> x.getValue() > 0.0)
                .collect(Collectors.toList());

        if (nonZeroRelevances.size() < 2)
            return 3600000L;

        long min = nonZeroRelevances.parallelStream().map(x -> x.getKey()).min(Long::compare).get();
        long max = nonZeroRelevances.parallelStream().map(x -> x.getKey()).max(Long::compare).get();

        return (max-min)+3600000L;
    }
}
