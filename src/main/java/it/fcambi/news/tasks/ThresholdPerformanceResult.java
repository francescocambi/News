package it.fcambi.news.tasks;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Francesco on 09/11/15.
 */
public class ThresholdPerformanceResult {

    protected Long matchMapGenerationTime;
    protected int itemCount;
    protected Map<Double, Integer> thresholdsResults = new LinkedHashMap<>();

    public Long getMatchMapGenerationTime() {
        return matchMapGenerationTime;
    }

    public void setMatchMapGenerationTime(Long matchMapGenerationTime) {
        this.matchMapGenerationTime = matchMapGenerationTime;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public Map<Double, Integer> getThresholdsResults() {
        return thresholdsResults;
    }

    public void setThresholdsResults(Map<Double, Integer> thresholdsResults) {
        this.thresholdsResults = thresholdsResults;
    }

    public void addThresholdResult(double threshold, int okCount ) {
        this.thresholdsResults.put(threshold, okCount);
    }
}
