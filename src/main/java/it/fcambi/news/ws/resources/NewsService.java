package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.matchers.NewsMatcher;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.metrics.JaccardSimilarity;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.News;
import it.fcambi.news.ws.resources.dto.MatchingNews;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

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

        List<News> news = em.createQuery("select n from News n", News.class).getResultList();

        NewsMatcher matcher = new NewsMatcher();
        matcher.setSourceArticle(sourceArticle);
        matcher.addMetric(new CosineSimilarity());
        matcher.addMetric(new JaccardSimilarity());

        List<MatchingNews> results = new ArrayList<>();

        // foreach news
        news.forEach(n -> {
            MatchingNews matchingNews = new MatchingNews();
            matchingNews.setNews(n);

            // foreach article related to news n
            n.getArticles().forEach(article -> matchingNews.addMatchingArticle(matcher.match(article)));

            results.add(matchingNews);
        });

        results.sort( (a, b) ->  {
            // If got NullPointer exception there are orphan news
            try {
                if (a.getMeanSimilarities().get("cosine") + a.getMeanSimilarities().get("jaccard") >
                        b.getMeanSimilarities().get("cosine") + b.getMeanSimilarities().get("jaccard"))
                    return -1;
                else if (a.getMeanSimilarities().get("cosine") + a.getMeanSimilarities().get("jaccard") <
                        b.getMeanSimilarities().get("cosine") + b.getMeanSimilarities().get("jaccard"))
                    return 1;
                else
                    return 0;
            } catch (NullPointerException e) {
                return 0;
            }
        });

        em.close();

        if (results.size() > 50)
            return results.subList(0, 50);
        else
            return results;

    }

}

