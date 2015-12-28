package it.fcambi.news.relevance;

import it.fcambi.news.model.News;

/**
 * Created by Francesco on 26/12/15.
 */
public interface NewsRelevanceCalculator {

    public NewsRelevance computeRelevance(News n);

}
