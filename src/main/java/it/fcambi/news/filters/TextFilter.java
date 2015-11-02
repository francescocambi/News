package it.fcambi.news.filters;

import java.util.List;

/**
 * Created by Francesco on 26/10/15.
 */
public interface TextFilter {

    /**
     * Filter words list, can generate new list or work on the existing one
     * @param words
     * @return Filtered string list
     */
    List<String> filter(List<String> words);

}
