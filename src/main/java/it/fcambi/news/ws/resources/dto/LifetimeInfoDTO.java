package it.fcambi.news.ws.resources.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Francesco on 28/12/15.
 */
public class LifetimeInfoDTO {

    protected double average;
    protected long max;
    protected long min;
    protected double median;
    protected double interquartileMean;
    protected List<ChartPoint<String, Long>> distribution = new ArrayList<>();

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getInterquartileMean() {
        return interquartileMean;
    }

    public void setInterquartileMean(double interquartileMean) {
        this.interquartileMean = interquartileMean;
    }

    public List<ChartPoint<String, Long>> getDistribution() {
        return distribution;
    }

    public void setDistribution(List<ChartPoint<String, Long>> distribution) {
        this.distribution = distribution;
    }
}