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

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setAveragePrecision(double averagePrecision) {
        this.averagePrecision = averagePrecision;
    }

    public void setMinPrecision(double minPrecision) {
        this.minPrecision = minPrecision;
    }

    public void setMaxPrecision(double maxPrecision) {
        this.maxPrecision = maxPrecision;
    }

    public void setAverageRecall(double averageRecall) {
        this.averageRecall = averageRecall;
    }

    public void setMinRecall(double minRecall) {
        this.minRecall = minRecall;
    }

    public void setMaxRecall(double maxRecall) {
        this.maxRecall = maxRecall;
    }

    public void setAverageFMeasure(double averageFMeasure) {
        this.averageFMeasure = averageFMeasure;
    }

    public void setAverageJaccard(double averageJaccard) {
        this.averageJaccard = averageJaccard;
    }

    public void setStdDevJaccard(double stdDevJaccard) {
        this.stdDevJaccard = stdDevJaccard;
    }

    public void setMinJaccard(double minJaccard) {
        this.minJaccard = minJaccard;
    }

    public void setMaxJaccard(double maxJaccard) {
        this.maxJaccard = maxJaccard;
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
