package it.fcambi.news.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Francesco on 16/10/15.
 */
public class MatchingCluster {

    private Cluster cluster;
    private Map<String, Double> similarities;

    public MatchingCluster() {
        similarities = new HashMap<>();
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public void addSimilarity(String k, double d) {
        this.similarities.put(k, d);
    }

    public Map<String, Double> getSimilarities() {
        return similarities;
    }

    public double getSimilarity(String k) {
        return similarities.get(k);
    }
}
