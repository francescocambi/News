package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.clustering.MatchMapGenerator;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.model.*;
import it.fcambi.news.ws.resources.dto.MatchArticlesRequestDTO;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Francesco on 30/09/15.
 */
@Path("/matcharticles")
@RolesAllowed({"user", "admin"})
public class MatchingArticlesService {

    @GET
    @Path("/matching/{id}-{matchId}")
    @Produces(MediaType.APPLICATION_JSON)
    public MatchingArticle getMatchingArticle(@PathParam("id") long articleId, @PathParam("matchId") long matchId) {

        EntityManager em = Application.createEntityManager();

        //TODO Surround not found article exception
        Article source = em.find(Article.class, articleId);

        Article match = em.find(Article.class, matchId);

        TFDictionary dictionary = em.find(TFDictionary.class, "italian_stemmed");

        MatchMapGeneratorConfiguration conf = new MatchMapGeneratorConfiguration()
                .addMetric(new CosineSimilarity())
                .addTextFilter(new NoiseWordsTextFilter())
                .addTextFilter(new StemmerTextFilter())
                .setWordVectorFactory(new TFIDFWordVectorFactory(dictionary));

        List<Article> sourceList = new ArrayList<>();
        sourceList.add(source);
        List<Article> matchList = new ArrayList<>();
        matchList.add(match);

        Map<Article, List<MatchingArticle>> matchMap = new MatchMapGenerator(conf).generateMap(sourceList, matchList);

        em.close();

        return matchMap.get(source).get(0);
    }

    @POST
    @Path("/match")
    @Produces(MediaType.APPLICATION_JSON)
    public Response matchArticles(MatchArticlesRequestDTO m) {

        EntityManager em = Application.createEntityManager();

        Article article = em.find(Article.class, m.getArticleId());
        Clustering clustering = em.find(Clustering.class, "manual");

        News n;
        if (m.getNewsId() == 0) {
            //Create new news
            n = new News(clustering);
            n.setDescription(article.getTitle());
        } else {
            //Retrieve existing one
            n = em.find(News.class, m.getNewsId());
        }
        //Checks that article's old news doesn't become orphan
        if (article.getNews(clustering) != null && article.getNews(clustering).getArticles().size() < 2) {
            // remove orphan
            em.remove(article.getNews(clustering));
        }

        //Matches article with news
        article.setNews(clustering, n);

        //Persist
        em.getTransaction().begin();
        if (m.getNewsId() == 0)
            em.persist(n);
        em.merge(article);
        em.flush();
        em.getTransaction().commit();
        em.close();

        return Response.status(201).entity(n.getId()).build();
    }

}
