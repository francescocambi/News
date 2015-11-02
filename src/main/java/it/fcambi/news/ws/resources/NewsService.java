package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.clustering.*;
import it.fcambi.news.model.MatchingArticle;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.TFDictionary;
import it.fcambi.news.model.MatchingNews;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Francesco on 18/10/15.
 */
@Path("/news")
public class NewsService {

    @GET
    @Path("/match-article/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MatchingNews> getNews(@PathParam("id") long articleId) {

        EntityManager em = Application.getEntityManager();

        //TODO Surround NoResult exception
        Article sourceArticle = em.find(Article.class, articleId);
        List<Article> targetArticles = new ArrayList<>();
        targetArticles.add(sourceArticle);

        List<Article> articles = em.createQuery("select a from Article a where a.news is not null", Article.class).getResultList();

        Metric cosine = new CosineSimilarity();

        MatchMapGeneratorConfiguration conf = new MatchMapGeneratorConfiguration()
                .addMetric(cosine)
                .addTextFilter(new NoiseWordsTextFilter())
                .addTextFilter(new StemmerTextFilter())
                .setWordVectorFactory(new TFIDFWordVectorFactory(em.find(TFDictionary.class, "italian_stemmed")));
        MatchMapGenerator generator = new MatchMapGenerator(conf);

        Map<Article, List<MatchingArticle>> matchMap = generator.generateMap(targetArticles, articles);

        Matcher matcher = new HighestMeanMatcher();
        Map<Article, List<MatchingNews>> clusterMap = matcher.getRankedList(cosine, matchMap, 0.47);

        em.close();

        if (clusterMap.get(sourceArticle).size() > 50)
            return clusterMap.get(sourceArticle).subList(0, 50);
        else
            return clusterMap.get(sourceArticle);
    }

}

