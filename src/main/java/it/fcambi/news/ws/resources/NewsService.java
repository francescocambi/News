package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.clustering.*;
import it.fcambi.news.model.*;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.metrics.Metric;

import javax.annotation.security.RolesAllowed;
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
@RolesAllowed({"user", "admin"})
public class NewsService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<News> getNews() {
        EntityManager em = Application.getEntityManager();
        List<News> news = em.createQuery("select n from News n", News.class).getResultList();
        return news;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public News getNewsById(@PathParam("id") long newsId) {
        EntityManager em = Application.getEntityManager();
        News n = em.find(News.class, newsId);
        n.size();
        em.close();
        return n;
    }


    @GET
    @Path("/match-article/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MatchingNews> getMatchingNewsFor(@PathParam("id") long articleId) {

        EntityManager em = Application.getEntityManager();

        //TODO Surround NoResult exception

        Article sourceArticle = em.find(Article.class, articleId);
        List<Article> targetArticles = new ArrayList<>();
        targetArticles.add(sourceArticle);

        List<Article> articles = em.createQuery("select a from Article a where key(a.news) = 'manual'", Article.class).getResultList();

        Clustering clustering = em.find(Clustering.class, "manual");
        TFDictionary dictionary = em.find(TFDictionary.class, "italian_stemmed");

        em.close();

        Metric cosine = new CosineSimilarity();

        MatchMapGeneratorConfiguration conf = new MatchMapGeneratorConfiguration()
                .addMetric(cosine)
                .addTextFilter(new NoiseWordsTextFilter())
                .addTextFilter(new StemmerTextFilter())
                .setWordVectorFactory(new TFIDFWordVectorFactory(dictionary));
        MatchMapGenerator generator = new MatchMapGenerator(conf);

        Map<Article, List<MatchingArticle>> matchMap = generator.generateMap(targetArticles, articles);

        Matcher matcher = new HighestMeanMatcher(cosine, 0.47, clustering);
        Map<Article, List<MatchingNews>> clusterMap = matcher.getRankedList(matchMap);

        if (clusterMap.get(sourceArticle).size() > 50)
            return clusterMap.get(sourceArticle).subList(0, 50);
        else
            return clusterMap.get(sourceArticle);
    }

}

