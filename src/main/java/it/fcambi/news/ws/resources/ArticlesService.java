package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.model.Article;
import it.fcambi.news.ws.resources.dto.ArticleListDTO;
import it.fcambi.news.ws.resources.dto.StatsDTO;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 05/10/15.
 */
@Path("/articles")
@RolesAllowed({"user", "admin"})
public class ArticlesService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArticles(@QueryParam("newsId") Long newsId) {

        EntityManager em = Application.createEntityManager();

        TypedQuery<Article> select;
        if (newsId != null) {
            //Validate
            if (newsId < 1)
                return Response.status(400).entity("Invalid news id.").build();

            select = em.createQuery("select a from Article a join a.news n where n.id=:newsId", Article.class)
                    .setParameter("newsId", newsId);
        } else {
            select = em.createQuery("select a from Article a", Article.class);
        }

        List<ArticleListDTO> articleDTOs = select.getResultList().stream()
                .map(ArticleListDTO::createFrom)
                .collect(Collectors.toList());

        em.close();

        return Response.status(200).entity(articleDTOs).build();
    }

    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public StatsDTO getStatistics() {
        EntityManager em = Application.createEntityManager();

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
        EntityManager em = Application.createEntityManager();
        Article a = em.find(Article.class, articleId);
        em.close();
        return a;
    }

    @DELETE
    @Path("/{id}")
    public Response deleteArticle(@PathParam("id") long articleId) {
        EntityManager em = Application.createEntityManager();
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
