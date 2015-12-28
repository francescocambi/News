package it.fcambi.news.relevance;

import it.fcambi.news.fpclustering.FrontPagesTimestampGroup;
import it.fcambi.news.model.Clustering;
import it.fcambi.news.model.News;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 26/12/15.
 */
public class OrderBasedRelevance implements NewsRelevanceCalculator {

    private Clustering clustering;
    private List<FrontPagesTimestampGroup> frontPagesGroups;

    private List<NewsRelevance> relevances;


    public OrderBasedRelevance(Clustering clustering) {
        this.clustering = clustering;
    }

    public void computeRelevances() {

        class Row {

            public Row(News news, Long t, double relevance) {
                this.news = news;
                this.relevance = relevance;
                this.timestamp = t;
            }

            News news;
            double relevance;
            long timestamp;
        }

        Set<Long> timestampsSet = frontPagesGroups.parallelStream().mapToLong(g -> g.getTimestamp().getTimeInMillis())
                .boxed().collect(Collectors.toSet());

        relevances = frontPagesGroups.parallelStream().flatMap(group ->

            group.getFrontPages().parallelStream().flatMap(fp -> {
                double norm = fp.getArticles().size();
                return fp.getArticles().stream().map(article ->
                        new Row(article.getNews(clustering), group.getTimestamp().getTimeInMillis(), ( norm-fp.orderOf(article) )/norm ));
            })

        ).filter(r -> r.news != null)
         .collect(Collectors.groupingByConcurrent(r -> r.news,
                Collectors.groupingByConcurrent(r -> r.timestamp, Collectors.summingDouble(r -> r.relevance))))
                .entrySet().parallelStream().map(entry -> {

                    long min = entry.getValue().entrySet().stream().mapToLong(e -> e.getKey()).min().orElse(0);
                    long max = entry.getValue().entrySet().stream().mapToLong(e -> e.getKey()).max().orElse(System.currentTimeMillis());

                    timestampsSet.stream().filter(t -> t > min && t < max).forEach(t -> entry.getValue().putIfAbsent(t, 0.0));
                    return new NewsRelevance(entry.getKey(), entry.getValue());
                }).collect(Collectors.toList());


    }

    @Override
    public NewsRelevance computeRelevance(News n) {
        return relevances.parallelStream().filter(r -> r.getNews().equals(n)).findFirst().orElse(null);
    }

    public List<NewsRelevance> getRelevances() {
        return relevances;
    }

    public void setFrontPagesGroups(List<FrontPagesTimestampGroup> frontPagesGroups) {
        this.frontPagesGroups = frontPagesGroups;
    }
}
