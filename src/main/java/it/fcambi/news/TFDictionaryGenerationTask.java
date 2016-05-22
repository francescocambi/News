package it.fcambi.news;

import it.fcambi.news.data.FrequenciesWordVector;
import it.fcambi.news.data.Text;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.Language;
import it.fcambi.news.model.TFDictionary;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Francesco on 27/10/15.
 */
public class TFDictionaryGenerationTask {

    private static Logger log = Logging.registerLogger(TFDictionaryGenerationTask.class.getName());

    public static void generateDictionary(String name, String languageString) throws Exception {
        log.info("TF-IDF Dictionary generation task started");

        Language language = Language.valueOf(languageString);

        EntityManager em = Application.createEntityManager();

        List<Article> articles = em.createQuery("select a from Article a", Article.class).getResultList();

        StemmerTextFilter stemmerFilter = new StemmerTextFilter(language);
        NoiseWordsTextFilter wordFilter = new NoiseWordsTextFilter(language);

        TFDictionary dict = new TFDictionary();
        dict.setDescription(name);

        articles.forEach(article -> {
            String s = article.getTitle() + " " + article.getDescription() + " " + article.getBody();
            s = s.replaceAll("[^\\p{Alpha}\\p{Space}]", " ");
            Text t = new Text(s, "\\p{Space}+");

            t.applyFilter(wordFilter)
                    .applyFilter(stemmerFilter);

            FrequenciesWordVector w = new FrequenciesWordVector();
            w.setFrom(t);
            dict.addDocument(w);
        });

        try {
            em.getTransaction().begin();

            // Delete dictionary if exists
            TFDictionary toDelete = em.find(TFDictionary.class, name);
            if (toDelete != null)
                em.remove(toDelete);

            em.persist(dict);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        }

        em.close();
    }

}
