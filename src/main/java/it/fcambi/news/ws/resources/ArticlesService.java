package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.model.Article;
import it.fcambi.news.ws.resources.dto.StatsDTO;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * Created by Francesco on 05/10/15.
 */
@Path("/articles")
@RolesAllowed({"user", "admin"})
public class ArticlesService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List getArticles(@QueryParam("clustering") String clusteringName,
                            @QueryParam("newsId") Long newsId) {
        if (clusteringName == null) clusteringName = "manual";
        EntityManager em = Application.getEntityManager();
        String select = "select a.id, a.title, a.source, n.id, a.created from Article a left join a.news n " +
                "on n.clustering.name = :clusteringName";
        if (newsId != null) select += " where n.id=:newsid";
        select += " order by a.id";
        Query o = em.createQuery(select);
        o.setParameter("clusteringName", clusteringName);
        if (newsId != null) o.setParameter("newsid", newsId);
        List articles = o.getResultList();
        em.close();

        return articles;
    }

    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public StatsDTO getStatistics() {
        EntityManager em = Application.getEntityManager();

        Long articlesCount = em.createQuery("select count(a) from Article a", Long.class).getSingleResult();
        Long matchedArtCount = em.createQuery("select count(a) from Article a where key(a.news) = 'manual'", Long.class).getSingleResult();
        Long notMatchedArtCount = articlesCount-matchedArtCount;
        Date mostRecentArticle = em.createQuery("select max(a.created) from Article a", Date.class).getSingleResult();

        StatsDTO o = new StatsDTO();
        o.setArticlesCount(articlesCount);
        o.setMatchedArticlesCount(matchedArtCount);
        o.setNotMatchedArticlesCount(notMatchedArtCount);
        o.setMostRecentArticleDate(mostRecentArticle);

        em.close();

        return o;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Article getArticle(@PathParam("id") long articleId) {
        EntityManager em = Application.getEntityManager();
        Article a = em.find(Article.class, articleId);
        em.close();
        return a;
    }

    @DELETE
    @Path("/{id}")
    public Response deleteArticle(@PathParam("id") long articleId) {
        EntityManager em = Application.getEntityManager();
        Article a = em.find(Article.class, articleId);
        if (!em.isOpen())
            return Response.status(500).build();
        if (a == null)
            return Response.status(404).build();

        em.getTransaction().begin();
        em.remove(a);
        em.flush();
        em.getTransaction().commit();
        em.close();

        return Response.status(200).build();
    }

}
