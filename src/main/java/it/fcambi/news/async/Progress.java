package it.fcambi.news.async;

/**
 * Created by Francesco on 09/11/15.
 */
public class Progress {

    protected double progress = 0.0;

    public double get() {
        return progress;
    }

    public synchronized void set(double progress) {
        this.progress = progress;
    }

    public synchronized void add(double a) {
        this.progress += a;
    }

}
