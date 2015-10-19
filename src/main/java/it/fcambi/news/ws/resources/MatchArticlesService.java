package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.matchers.NewsMatcher;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.metrics.JaccardSimilarity;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.News;
import it.fcambi.news.ws.resources.dto.MatchArticlesRequestDTO;
import it.fcambi.news.ws.resources.dto.MatchingArticleDTO;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by Francesco on 30/09/15.
 */
@Path("/matcharticles")
@RolesAllowed({"user", "admin"})
public class MatchArticlesService {

    @GET
    @Path("/matching/{id}")
    @Produces("application/json")
    @Deprecated
    public List<MatchingArticleDTO> getMatchingArticles(@PathParam("id") long articleId) {
        List<MatchingArticleDTO> result = matchingArticles(articleId, -1);

        Comparator<MatchingArticleDTO> compare = (a,b) -> {
            double value = a.getSimilarity("cosine") + a.getSimilarity("jaccard")
                    - (b.getSimilarity("cosine") + b.getSimilarity("jaccard"));
            if (value > 0)
                return -1;
            else if (value < 0)
                return 1;
            else
                return 0;
        };

        result.sort(compare);
        return result.subList(0, 40);
    }

    @GET
    @Path("/matching/{id}-{matchId}")
    @Produces("application/json")
    public MatchingArticleDTO getMatchingArticle(@PathParam("id") long articleId, @PathParam("matchId") long matchId) {
        return matchingArticles(articleId, matchId).get(0);
    }

    @POST
    @Path("/match")
    @Produces(MediaType.APPLICATION_JSON)
    public Response matchArticles(MatchArticlesRequestDTO m) {

//        System.out.println("matchArticles("+m.getArticleId()+", "+m.getMatchId()+")");
        EntityManager em = Application.getEntityManager();

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
        //Matches article with news
        article.setNews(n);

        //Persist
        em.getTransaction().begin();
        if (m.getNewsId() == 0)
            em.persist(n);
        em.merge(article);
        em.flush();
        em.getTransaction().commit();
        em.close();

        return Response.status(201).build();
    }

    private List<MatchingArticleDTO> matchingArticles(long articleId, long matchId) {
        EntityManager em = Application.getEntityManager();

        Article source = em.find(Article.class, articleId);

        List<Article> articles;
        if (matchId < 0) {
            articles = em.createQuery("select a from Article a where a.source <> ?1")
                    .setParameter(1, source.getSource())
                    .getResultList();
        } else {
            articles = new LinkedList<>();
            articles.add(em.find(Article.class, matchId));
            if (articles.size() < 1) throw new IllegalArgumentException("Match article doesn't exists. [ID="+matchId+"]");
        }

        NewsMatcher matcher = new NewsMatcher();
        matcher.setSourceArticle(source);
        matcher.addMetric(new CosineSimilarity());
        matcher.addMetric(new JaccardSimilarity());
        return matcher.match(articles);
    }

}
