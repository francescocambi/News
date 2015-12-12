package it.fcambi.news.tasks;

/**
 * Created by Francesco on 11/12/15.
 */
public class ClusteringPerformanceResults {

    double threshold;

    double averagePrecision;
    double minPrecision;
    double maxPrecision;

    double averageRecall;
    double minRecall;
    double maxRecall;

    double averageFMeasure;

    double averageJaccard;
    double stdDevJaccard;
    double minJaccard;
    double maxJaccard;

    public ClusteringPerformanceResults(double threshold) {
        this.threshold = threshold;
    }

    public double getThreshold() {
        return threshold;
    }

    public double getAveragePrecision() {
        return averagePrecision;
    }

    public double getMinPrecision() {
        return minPrecision;
    }

    public double getMaxPrecision() {
        return maxPrecision;
    }

    public double getAverageRecall() {
        return averageRecall;
    }

    public double getMinRecall() {
        return minRecall;
    }

    public double getMaxRecall() {
        return maxRecall;
    }

    public double getAverageFMeasure() {
        return averageFMeasure;
    }

    public double getAverageJaccard() {
        return averageJaccard;
    }

    public double getStdDevJaccard() {
        return stdDevJaccard;
    }

    public double getMinJaccard() {
        return minJaccard;
    }

    public double getMaxJaccard() {
        return maxJaccard;
    }
}
