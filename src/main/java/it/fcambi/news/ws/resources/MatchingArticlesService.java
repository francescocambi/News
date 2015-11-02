package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.clustering.MatchMapGenerator;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.MatchingArticle;
import it.fcambi.news.model.News;
import it.fcambi.news.model.TFDictionary;
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
    @Produces("application/json")
    public MatchingArticle getMatchingArticle(@PathParam("id") long articleId, @PathParam("matchId") long matchId) {

        EntityManager em = Application.getEntityManager();

        //TODO Surround not found article exception
        Article source = em.find(Article.class, articleId);

        Article match = em.find(Article.class, matchId);

        MatchMapGeneratorConfiguration conf = new MatchMapGeneratorConfiguration()
                .addMetric(new CosineSimilarity())
                .addTextFilter(new NoiseWordsTextFilter())
                .addTextFilter(new StemmerTextFilter())
                .setWordVectorFactory(new TFIDFWordVectorFactory(em.find(TFDictionary.class, "italian_stemmed")));

        List<Article> sourceList = new ArrayList<>();
        sourceList.add(source);
        List<Article> matchList = new ArrayList<>();
        matchList.add(match);

        Map<Article, List<MatchingArticle>> matchMap = new MatchMapGenerator(conf).generateMap(sourceList, matchList);

        return matchMap.get(source).get(0);
    }

    @POST
    @Path("/match")
    @Produces(MediaType.APPLICATION_JSON)
    public Response matchArticles(MatchArticlesRequestDTO m) {

//        System.out.println("matchArticles("+m.getArticleId()+", "+m.getMatchId()+")");
        EntityManager em = Application.getEntityManager();
        em.getTransaction().begin();

        Article article = em.find(Article.class, m.getArticleId());

        News n;
        if (m.getNewsId() == 0) {
            //Create new news
            n = new News();
            n.setDescription(article.getTitle());
        } else {
            //Retrieve existing one
            n = em.find(News.class, m.getNewsId());
        }
        //Checks that article's old news doesn't become orphan
        if (article.getNews() != null && article.getNews().getArticles().size() < 2) {
            // remove orphan
            em.remove(article.getNews());

        }

        //Matches article with news
        article.setNews(n);

        //Persist
        if (m.getNewsId() == 0)
            em.persist(n);
        em.merge(article);
        em.flush();
        em.getTransaction().commit();
        em.close();

        return Response.status(201).build();
    }

}
