package it.fcambi.news.fpclustering;

import java.util.Calendar;

/**
 * Created by Francesco on 20/11/15.
 */
public class NewspapersDistance {

    private double[][] distances;
    private Calendar timestamp;

    public NewspapersDistance() {
    }

    public NewspapersDistance(double[][] distances, Calendar timestamp) {
        this.distances = distances;
        this.timestamp = timestamp;
    }

    public double[][] getDistances() {
        return distances;
    }

    public void setDistances(double[][] distances) {
        this.distances = distances;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }
}
