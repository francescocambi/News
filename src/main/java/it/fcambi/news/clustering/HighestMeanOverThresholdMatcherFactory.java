package it.fcambi.news.clustering;

import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.Clustering;

/**
 * Created by Francesco on 11/12/15.
 */
public class HighestMeanOverThresholdMatcherFactory implements MatcherFactory {

    @Override
    public Matcher createMatcher(Metric metric, double threshold, Clustering clustering) {
        return new HighestMeanOverThresholdMatcher(metric, threshold, clustering);
    }
}
