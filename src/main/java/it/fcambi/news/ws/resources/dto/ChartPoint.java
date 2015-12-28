package it.fcambi.news.ws.resources.dto;

/**
 * Created by Francesco on 28/12/15.
 */
public class ChartPoint<L, V> {

    protected L label;
    protected V value;

    public ChartPoint(L label, V value) {
        this.label = label;
        this.value = value;
    }

    public L getLabel() {
        return label;
    }

    public void setLabel(L label) {
        this.label = label;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
