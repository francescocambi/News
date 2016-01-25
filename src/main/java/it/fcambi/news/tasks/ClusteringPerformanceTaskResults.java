package it.fcambi.news.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Created by Francesco on 20/12/15.
 */
public class ClusteringPerformanceTaskResults {

    @JsonProperty
    protected ClusteringPerformanceResults trainingSet;
    @JsonProperty
    protected ClusteringPerformanceResults testSet;

    public ClusteringPerformanceTaskResults() {
    }

    public ClusteringPerformanceTaskResults(ClusteringPerformanceResults trainingSet) {
        this.trainingSet = trainingSet;
    }

    public ClusteringPerformanceTaskResults(ClusteringPerformanceResults trainingSet,
                                            ClusteringPerformanceResults testSet) {
        this.trainingSet = trainingSet;
        this.testSet = testSet;
    }

    public ClusteringPerformanceResults getTrainingSet() {
        return trainingSet;
    }

    public void setTrainingSet(ClusteringPerformanceResults trainingSet) {
        this.trainingSet = trainingSet;
    }

    public ClusteringPerformanceResults getTestSet() {
        return testSet;
    }

    public void setTestSet(ClusteringPerformanceResults testSet) {
        this.testSet = testSet;
    }

    @Override
    public String toString() {
        return "{" +
                "trainingSet=" + trainingSet +
                ", testSet=" + testSet +
                '}';
    }
}
