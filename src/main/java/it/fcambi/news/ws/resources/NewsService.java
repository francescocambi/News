package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.clustering.HighestMeanMatcher;
import it.fcambi.news.clustering.MatchMapGenerator;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.clustering.Matcher;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.data.Text;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.*;

import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Francesco on 18/10/15.
 */
@Path("/news")
@RolesAllowed({"user", "admin"})
public class NewsService {

    private static Map<Long, Text> matchMapKeywordsCache = new ConcurrentHashMap<>();
    private static Map<Long, Text> matchMapBodyCache = new ConcurrentHashMap<>();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNews(@QueryParam("clustering") String clusteringName) {
        EntityManager em = Application.createEntityManager();

        Clustering clustering;
        try {
            clustering = em.find(Clustering.class, clusteringName);
            if (clustering == null)
                throw new IllegalArgumentException("Cannot find clustering");
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }

        List<News> clusters = em.createQuery("select n from News n where n.clustering=:clustering", News.class)
                .setParameter("clustering", clustering)
                .getResultList();
        em.close();

        return Response.status(200).entity(clusters).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public News getNewsById(@PathParam("id") long newsId) {
        EntityManager em = Application.createEntityManager();
        News n = em.find(News.class, newsId);
        n.size();
        em.close();
        return n;
    }


    @GET
    @Path("/match-article/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MatchingNews> getMatchingNewsFor(@PathParam("id") long articleId) {

        EntityManager em = Application.createEntityManager();

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
//                .setKeywordSelectionFunction((title, description, body) -> new Text(title, description, body));
//                .setKeywordSelectionFunction((title, description, body) -> {

//                    String s = ">>>>>>>>>>>>>>>>>>> ARTICLE "+article.getId()+" <<<<<<<<<<<<<<<<<<<<\n";
//                    s += article.getTitle()+"\n";
//                    s += "-----------------------BODY-----------------------\n";
//                    s += body.toString()+"\n";
//                    s += "---------------------KEYWORDS---------------------\n";

//                    Stream<String> capitals = body.words().stream()
//                            .filter(w -> w.length() > 0 && Character.isUpperCase(w.charAt(0)));

//                    Stream<String> lowfreq = body.words().stream()
//                            .filter(w -> w.length() > 0 && dictionary.getNumOfDocumentsWithWord(w.toLowerCase()) < 37);

//                    return Stream.of(title.stream(), description.stream(), capitals, lowfreq).flatMap(s -> s)
//                            .collect(Text.collector());

//                    System.out.println(keywords.toString());

//                });
        MatchMapGenerator generator = new MatchMapGenerator(conf, matchMapBodyCache, matchMapKeywordsCache);

        Map<Article, List<MatchingArticle>> matchMap = generator.generateMap(targetArticles, articles);

        Matcher matcher = new HighestMeanMatcher(cosine, 0.47, clustering);
        Map<Article, List<MatchingNews>> clusterMap = matcher.getRankedList(matchMap);

        if (clusterMap.get(sourceArticle).size() > 50)
            return clusterMap.get(sourceArticle).subList(0, 50);
        else
            return clusterMap.get(sourceArticle);
    }

    @GET
    @Path("/merge")
    @Produces(MediaType.APPLICATION_JSON)
    public Response mergeClusters(@QueryParam("news") List<Long> newsIds) {

        if (newsIds.size() < 2)
            return Response.status(400).entity("Cannot merge less than 2 news.").build();

        EntityManager em = Application.createEntityManager();

        List<News> newsToMerge = newsIds.stream().map(id -> em.find(News.class, id))
                .filter(x -> x != null).collect(Collectors.toList());

        if (newsToMerge.size() != newsIds.size())
            return Response.status(400).entity("Invalid news ids.").build();

        em.getTransaction().begin();
        //Extract master news
        News master = newsToMerge.remove(0);
        //Merge all articles to master news
        newsToMerge.stream().flatMap(n -> n.getArticles().stream()).forEach(article -> {
            article.setNews(master.getClustering(), master);
        });
        //Remove all merged news
        newsToMerge.forEach(em::remove);

        em.getTransaction().commit();
        em.close();

        return Response.status(201).build();
    }

}

