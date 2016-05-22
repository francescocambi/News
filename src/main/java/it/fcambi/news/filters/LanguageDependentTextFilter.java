package it.fcambi.news.filters;

import it.fcambi.news.model.Language;

/**
 * Created by Francesco on 15/05/16.
 */
abstract class LanguageDependentTextFilter implements TextFilter {

    protected Language language;

    public LanguageDependentTextFilter(Language language) {
        this.language = language;
    }
}
