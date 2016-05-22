package it.fcambi.news.ws.resources.dto;

import java.util.List;

/**
 * Created by Francesco on 28/12/15.
 */
public class LifetimeInfoDTO {

    protected double relevanceLifetimeAvg;
    protected long relevanceLifetimeMax;
    protected long relevanceLifetimeMin;
    protected double relevanceLifetimeMedian;
    protected double relevanceLifetimeInterdecileMean;

    protected double timeRangeAvg;
    protected double timeRangeMax;
    protected double timeRangeMin;
    protected double timeRangeMedian;
    protected double timeRangeInterdecileMean;

    protected List<NewsLifetimeDTO> lifetimes;

    public double getRelevanceLifetimeAvg() {
        return relevanceLifetimeAvg;
    }

    public void setRelevanceLifetimeAvg(double relevanceLifetimeAvg) {
        this.relevanceLifetimeAvg = relevanceLifetimeAvg;
    }

    public long getRelevanceLifetimeMax() {
        return relevanceLifetimeMax;
    }

    public void setRelevanceLifetimeMax(long relevanceLifetimeMax) {
        this.relevanceLifetimeMax = relevanceLifetimeMax;
    }

    public long getRelevanceLifetimeMin() {
        return relevanceLifetimeMin;
    }

    public void setRelevanceLifetimeMin(long relevanceLifetimeMin) {
        this.relevanceLifetimeMin = relevanceLifetimeMin;
    }

    public double getRelevanceLifetimeMedian() {
        return relevanceLifetimeMedian;
    }

    public void setRelevanceLifetimeMedian(double relevanceLifetimeMedian) {
        this.relevanceLifetimeMedian = relevanceLifetimeMedian;
    }

    public double getRelevanceLifetimeInterdecileMean() {
        return relevanceLifetimeInterdecileMean;
    }

    public void setRelevanceLifetimeInterdecileMean(double relevanceLifetimeInterdecileMean) {
        this.relevanceLifetimeInterdecileMean = relevanceLifetimeInterdecileMean;
    }

    public double getTimeRangeAvg() {
        return timeRangeAvg;
    }

    public void setTimeRangeAvg(double timeRangeAvg) {
        this.timeRangeAvg = timeRangeAvg;
    }

    public double getTimeRangeMax() {
        return timeRangeMax;
    }

    public void setTimeRangeMax(double timeRangeMax) {
        this.timeRangeMax = timeRangeMax;
    }

    public double getTimeRangeMin() {
        return timeRangeMin;
    }

    public void setTimeRangeMin(double timeRangeMin) {
        this.timeRangeMin = timeRangeMin;
    }

    public double getTimeRangeMedian() {
        return timeRangeMedian;
    }

    public void setTimeRangeMedian(double timeRangeMedian) {
        this.timeRangeMedian = timeRangeMedian;
    }

    public double getTimeRangeInterdecileMean() {
        return timeRangeInterdecileMean;
    }

    public void setTimeRangeInterdecileMean(double timeRangeInterdecileMean) {
        this.timeRangeInterdecileMean = timeRangeInterdecileMean;
    }

    public List<NewsLifetimeDTO> getLifetimes() {
        return lifetimes;
    }

    public void setLifetimes(List<NewsLifetimeDTO> lifetimes) {
        this.lifetimes = lifetimes;
    }
}