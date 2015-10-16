package it.fcambi.news;

/**
 * Created by Francesco on 28/09/15.
 */
public interface ProgressObservable {

    public void addProgressObserver(ProgressObserver o);
    public void removeProgressObserver(ProgressObserver o);

}
