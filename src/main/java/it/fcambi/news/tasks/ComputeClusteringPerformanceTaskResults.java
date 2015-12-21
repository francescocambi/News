package it.fcambi.news.tasks;

import java.util.Map;

/**
 * Created by Francesco on 20/12/15.
 */
public class ComputeClusteringPerformanceTaskResults {

    protected Map<Double, ClusteringPerformanceResults> trainingSet;
    protected Map<Double, ClusteringPerformanceResults> testSet;

    public Map<Double, ClusteringPerformanceResults> getTrainingSet() {
        return trainingSet;
    }

    public void setTrainingSet(Map<Double, ClusteringPerformanceResults> trainingSet) {
        this.trainingSet = trainingSet;
    }

    public Map<Double, ClusteringPerformanceResults> getTestSet() {
        return testSet;
    }

    public void setTestSet(Map<Double, ClusteringPerformanceResults> testSet) {
        this.testSet = testSet;
    }
}
