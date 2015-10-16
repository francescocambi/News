package it.fcambi.news.filters;

/**
 * Created by Francesco on 06/10/15.
 */
public class StandardizeStringFilter implements StringFilter {

    @Override
    public String filter(String s) {
        return s.toLowerCase()
                .replace('à','a')
                .replace('ò', 'o')
                .replaceAll("[éè]", "e")
                .replace('ù', 'u')
                .replace('\'',' ')
                .replaceAll("[^a-z ]", "");
    }
}
