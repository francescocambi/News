package it.fcambi.news.tasks;

/**
 * Created by Francesco on 11/12/15.
 */
public class ClusteringPerformanceResults {

    double threshold;

    double averagePrecisionWeighted;
    double averageRecallWeighted;
    double averageFMeasureWeighted;
    double averageJaccardWeighted;

    double averagePrecision;
    double averageRecall;
    double averageFMeasure;
    double averageJaccard;

    int numGeneratedClusters;
    int numExpectedClusters;

    int numOfArticles;

    public ClusteringPerformanceResults(double threshold) {
        this.threshold = threshold;
    }

    public ClusteringPerformanceResults(double threshold, double averagePrecisionWeighted,
                                        double averageRecallWeighted, double averageFMeasureWeighted,
                                        double averageJaccardWeighted, double averagePrecision, double averageRecall,
                                        double averageFMeasure, double averageJaccard, int numGeneratedClusters,
                                        int numExpectedClusters, int numOfArticles) {
        this.threshold = threshold;
        this.averagePrecisionWeighted = averagePrecisionWeighted;
        this.averageRecallWeighted = averageRecallWeighted;
        this.averageFMeasureWeighted = averageFMeasureWeighted;
        this.averageJaccardWeighted = averageJaccardWeighted;
        this.averagePrecision = averagePrecision;
        this.averageRecall = averageRecall;
        this.averageFMeasure = averageFMeasure;
        this.averageJaccard = averageJaccard;
        this.numGeneratedClusters = numGeneratedClusters;
        this.numExpectedClusters = numExpectedClusters;
        this.numOfArticles = numOfArticles;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getAveragePrecisionWeighted() {
        return averagePrecisionWeighted;
    }

    public void setAveragePrecisionWeighted(double averagePrecisionWeighted) {
        this.averagePrecisionWeighted = averagePrecisionWeighted;
    }

    public double getAverageRecallWeighted() {
        return averageRecallWeighted;
    }

    public void setAverageRecallWeighted(double averageRecallWeighted) {
        this.averageRecallWeighted = averageRecallWeighted;
    }

    public double getAverageFMeasureWeighted() {
        return averageFMeasureWeighted;
    }

    public void setAverageFMeasureWeighted(double averageFMeasureWeighted) {
        this.averageFMeasureWeighted = averageFMeasureWeighted;
    }

    public double getAverageJaccardWeighted() {
        return averageJaccardWeighted;
    }

    public void setAverageJaccardWeighted(double averageJaccardWeighted) {
        this.averageJaccardWeighted = averageJaccardWeighted;
    }

    public double getAveragePrecision() {
        return averagePrecision;
    }

    public void setAveragePrecision(double averagePrecision) {
        this.averagePrecision = averagePrecision;
    }

    public double getAverageRecall() {
        return averageRecall;
    }

    public void setAverageRecall(double averageRecall) {
        this.averageRecall = averageRecall;
    }

    public double getAverageFMeasure() {
        return averageFMeasure;
    }

    public void setAverageFMeasure(double averageFMeasure) {
        this.averageFMeasure = averageFMeasure;
    }

    public double getAverageJaccard() {
        return averageJaccard;
    }

    public void setAverageJaccard(double averageJaccard) {
        this.averageJaccard = averageJaccard;
    }

    public int getNumGeneratedClusters() {
        return numGeneratedClusters;
    }

    public void setNumGeneratedClusters(int numGeneratedClusters) {
        this.numGeneratedClusters = numGeneratedClusters;
    }

    public int getNumExpectedClusters() {
        return numExpectedClusters;
    }

    public void setNumExpectedClusters(int numExpectedClusters) {
        this.numExpectedClusters = numExpectedClusters;
    }

    public int getNumOfArticles() {
        return numOfArticles;
    }

    public void setNumOfArticles(int numOfArticles) {
        this.numOfArticles = numOfArticles;
    }

    @Override
    public String toString() {
        return "{" +
                "newsThreshold=" + threshold +
                ", averagePrecisionWeighted=" + averagePrecisionWeighted +
                ", averageRecallWeighted=" + averageRecallWeighted +
                ", averageFMeasureWeighted=" + averageFMeasureWeighted +
                ", averageJaccardWeighted=" + averageJaccardWeighted +
                ", averagePrecision=" + averagePrecision +
                ", averageRecall=" + averageRecall +
                ", averageFMeasure=" + averageFMeasure +
                ", averageJaccard=" + averageJaccard +
                ", numGeneratedClusters=" + numGeneratedClusters +
                ", numExpectedClusters=" + numExpectedClusters +
                ", numOfArticles=" + numOfArticles +
                '}';
    }
}
