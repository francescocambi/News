package it.fcambi.news.clustering;

import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.MatchingArticle;
import it.fcambi.news.model.MatchingNews;
import it.fcambi.news.model.News;

import java.util.List;
import java.util.Map;

/**
 * Created by Francesco on 02/11/15.
 */
public interface Matcher {
    Map<Article, MatchingNews> findBestMatch(Metric metric, Map<Article, List<MatchingArticle>> matchMap, double threshold);

    Map<Article, List<MatchingNews>> getRankedList(Metric metric, Map<Article, List<MatchingArticle>> matchMap, double threshold);
}
