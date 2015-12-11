package it.fcambi.news.clustering;

import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.Clustering;

/**
 * Created by Francesco on 11/12/15.
 */
public interface MatcherFactory {

    Matcher createMatcher(Metric metric, double threshold, Clustering clustering);
}
