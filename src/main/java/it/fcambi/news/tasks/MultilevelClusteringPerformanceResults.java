package it.fcambi.news.tasks;

/**
 * Created by Francesco on 24/02/16.
 */
public class MultilevelClusteringPerformanceResults extends ClusteringPerformanceResults {

    protected double metanewsThreshold;

    public MultilevelClusteringPerformanceResults(double metanewsThreshold, double threshold) {
        super(threshold);
        this.metanewsThreshold = metanewsThreshold;
    }

    public MultilevelClusteringPerformanceResults(double metanewsThreshold, double threshold, double averagePrecisionWeighted, double averageRecallWeighted,
                                                  double averageFMeasureWeighted, double averageJaccardWeighted,
                                                  double averagePrecision, double averageRecall, double averageFMeasure,
                                                  double averageJaccard, int numGeneratedClusters, int numExpectedClusters,
                                                  int numOfArticles) {
        super(threshold, averagePrecisionWeighted, averageRecallWeighted, averageFMeasureWeighted, averageJaccardWeighted,
                averagePrecision, averageRecall, averageFMeasure, averageJaccard, numGeneratedClusters, numExpectedClusters,
                numOfArticles);
        this.metanewsThreshold = metanewsThreshold;
    }

    public double getMetanewsThreshold() {
        return metanewsThreshold;
    }

    @Override
    public String toString() {
        return "{" +
                "metanewsThreshold=" + metanewsThreshold +
                ", newsThreshold=" + threshold +
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
