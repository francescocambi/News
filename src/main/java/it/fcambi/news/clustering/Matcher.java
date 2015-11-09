package it.fcambi.news.clustering;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.MatchingArticle;
import it.fcambi.news.model.MatchingNews;

import java.util.List;
import java.util.Map;

/**
 * Created by Francesco on 02/11/15.
 */
public interface Matcher {
    Map<Article, MatchingNews> findBestMatch(Map<Article, List<MatchingArticle>> matchMap);

    Map<Article, List<MatchingNews>> getRankedList(Map<Article, List<MatchingArticle>> matchMap);
}
