package it.fcambi.news.filters;

import it.fcambi.news.data.WordVector;

import java.util.List;

/**
 * Created by Francesco on 05/10/15.
 */
public interface VectorFilter {
    public void filter(WordVector w);
}
