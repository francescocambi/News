package it.fcambi.news.filters;

import it.fcambi.news.Application;
import it.fcambi.news.model.Language;
import it.fcambi.news.model.NoiseWordsList;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 26/10/15.
 */
public class NoiseWordsTextFilter extends LanguageDependentTextFilter  {

    private static Map<Language, List<String>> stopWordsLists = new ConcurrentHashMap<>();

    static {
        EntityManager em = Application.createEntityManager();
        stopWordsLists = em.createQuery("select l from NoiseWordsList l", NoiseWordsList.class).getResultList()
                .stream().collect(Collectors.toMap(NoiseWordsList::getLanguage, NoiseWordsList::getWords));
        em.close();
        if (stopWordsLists.size() == 0)
            throw new IllegalArgumentException("No noise words lists found. Please provide at least one list.");
    }

    public NoiseWordsTextFilter(Language language) {
        super(language);

        if (!stopWordsLists.containsKey(language)) {
            throw new LanguageNotSupportedException("Language "+language.toString().toUpperCase()+" is not supported by NoiseWordsTextFilter");
        }
    }

    public List<String> filter(List<String> words) {
        return words.parallelStream().filter(s -> !stopWordsLists.get(this.language).contains(s.toLowerCase()))
                .collect(Collectors.toList());
    }
}
