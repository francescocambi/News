package it.fcambi.news.clustering;

import it.fcambi.news.data.Text;

/**
 * Created by Francesco on 18/12/15.
 */
@FunctionalInterface
public interface KeywordsSelectionFunction {
    public Text apply(Text title, Text description, Text body);
}
