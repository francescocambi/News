package it.fcambi.news;

/**
 * Created by Francesco on 28/09/15.
 */
public class ProgressHolder implements ProgressObserver {

    private float progress;

    public void update(float progress) {
        this.progress = progress;
    }

    public float getProgress() {
        return this.progress;
    }
}
