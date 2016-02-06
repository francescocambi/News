package it.fcambi.news.filters;

import com.sun.javaws.exceptions.InvalidArgumentException;
import it.fcambi.news.Application;
import it.fcambi.news.PersistenceManager;
import it.fcambi.news.model.NoiseWordsList;

import javax.persistence.EntityManager;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 26/10/15.
 */
public class NoiseWordsTextFilter implements TextFilter  {

    private static final String DICTIONARY_NAME = "noise_words_FULL";
    private static List<String> stopWords;

    static {
        EntityManager em = Application.createEntityManager();
        NoiseWordsList list = em.createQuery("select l from NoiseWordsList l where l.description = :name", NoiseWordsList.class)
                .setParameter("name", DICTIONARY_NAME).getSingleResult();
        em.close();
        if (list != null)
            stopWords = list.getWords();
        else
            throw new IllegalArgumentException("Noise words dictionary "+DICTIONARY_NAME+" does not exists.");
    }

    public List<String> filter(List<String> words) {
        return words.parallelStream().filter(s -> !stopWords.contains(s.toLowerCase()))
                .collect(Collectors.toList());
    }
}
