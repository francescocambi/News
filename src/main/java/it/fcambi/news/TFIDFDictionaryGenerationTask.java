package it.fcambi.news;

import it.fcambi.news.data.FrequenciesWordVector;
import it.fcambi.news.data.Text;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.TFDictionary;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by Francesco on 27/10/15.
 */
public class TFIDFDictionaryGenerationTask {

    public static void main(String[] args) {

        PersistenceManager persistenceManager = new PersistenceManager("it.fcambi.news.jpa.local");
        EntityManager em = persistenceManager.createEntityManager();

        List<Article> articles = em.createQuery("select a from Article a", Article.class).getResultList();

        TFDictionary dict = new TFDictionary();
        dict.setDescription("italian_stemmed");

        articles.forEach(article -> {
            String s = article.getTitle() + " " + article.getDescription() + " " + article.getBody();
            s = s.replaceAll("[^\\p{Alpha}\\p{Space}]", " ");
            Text t = new Text(s, "\\p{Space}+");
            t.applyFilter(new NoiseWordsTextFilter())
                    .applyFilter(new StemmerTextFilter());
            FrequenciesWordVector w = new FrequenciesWordVector();
            w.setFrom(t);
            dict.addDocument(w);
        });

        em.getTransaction().begin();
        em.persist(dict);
        em.getTransaction().commit();

        em.close();
        persistenceManager.close();

    }

}
