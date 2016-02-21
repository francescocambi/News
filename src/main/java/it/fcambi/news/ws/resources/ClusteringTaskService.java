package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.clustering.HighestMeanMatcher;
import it.fcambi.news.clustering.HighestMeanOverThresholdMatcher;
import it.fcambi.news.clustering.Matcher;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.Clustering;
import it.fcambi.news.model.Newspaper;
import it.fcambi.news.tasks.IncrementalClusteringTask;

import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 11/11/15.
 */
@Path("/clustering")
@Singleton
@RolesAllowed({"user", "admin"})
public class ClusteringTaskService extends TaskService<IncrementalClusteringTask> {

    @GET
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNew(@QueryParam("metricName") String metricName,
                              @QueryParam("noiseWordsFilter") boolean noiseWordsFilter,
                              @QueryParam("stemming") boolean stemming,
                              @QueryParam("tfidf") boolean tfidf,
                              @QueryParam("keywordExtraction") String keywordExtraction,
                              @QueryParam("clusteringName") String clusteringName,
                              @QueryParam("matcherName") String matcherName,
                              @QueryParam("threshold") double threshold,
                              @QueryParam("articlesFrom") String fromString,
                              @QueryParam("articlesTo") String toString,
                              @QueryParam("newspapers") String newspapersString) {

        EntityManager em;

        MatchMapGeneratorConfigurationParser parser = new MatchMapGeneratorConfigurationParser();
        try {
            parser.parse(metricName, noiseWordsFilter, stemming, tfidf, keywordExtraction);
        } catch (IllegalArgumentException e) {
            Response.status(400).entity(e.getMessage()).build();
        }

        //Checks threshold
        if (threshold < 0 || threshold > 1)
            return Response.status(400).entity("Invalid threshold value.").build();

        // Checks clustering existence. If not exists, create it.
        Clustering clustering;
        if (clusteringName != null && clusteringName.length() > 0) {
            em = Application.createEntityManager();
            try {
                clustering = em.find(Clustering.class, clusteringName);
                if (clustering == null) throw new Exception();
            } catch (Exception e) {
                clustering = new Clustering();
                clustering.setName(clusteringName);
                clustering.setDescription("Metric "+metricName+"@"+threshold+"; Stemming "+stemming+"; TFIDF "+tfidf+
                        "; Keyword Extraction "+keywordExtraction+"; Matcher "+matcherName);
            }
        } else
            return Response.status(400).entity("Invalid clustering name.").build();

        //Detect matcher
        Matcher matcher;
        switch (matcherName) {
            case "highest_mean":
                matcher = new HighestMeanMatcher(parser.getMetric(), threshold, clustering);
                break;
            case "highest_mean_over_threshold":
                matcher = new HighestMeanOverThresholdMatcher(parser.getMetric(), threshold, clustering);
                break;
            default:
                if (em != null) em.close();
                return Response.status(400).entity("Invalid matcher name.").build();
        }

        //Retrieve articleToBeClustered dataset
        String select = "select a from Article a where 1=1";

        //Detects excluded newspapers
        List<String> newspapers = Arrays.asList(newspapersString.split(","));
        List<Newspaper> newspapersToExclude = Arrays.stream(Newspaper.values())
                .filter(x -> !newspapers.contains(x.toString())).collect(Collectors.toList());
        if (newspapersToExclude.size() > 0) {
            select += " and a.source not in :newspapersToExclude";
        }

        Date from = null, to = null;
        if (fromString != null && toString != null) {
            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
            try {
                from = format.parse(fromString);
                to = format.parse(toString);
                if (to.before(from))
                    throw new IllegalArgumentException("FROM date is after TO date.");
            } catch (ParseException | IllegalArgumentException e) {
                return Response.status(400).entity("Invalid FROM TO date: " + e.getMessage()).build();
            }
        }

        //Detect and add from to filters
        if (from != null && to != null && from.before(to))
            select += " and a.created between :from and :to";

        if (em == null) em = Application.createEntityManager();
        Query q = em.createQuery(select, Article.class);


        if (from != null && to != null && from.before(to)) {
            q.setParameter("from", from);
            q.setParameter("to", to);
        }

        if (newspapersToExclude.size() > 0) {
            q.setParameter("newspapersToExclude", newspapersToExclude);
        }

        List<Article> articlesToBeClustered;
        try {
            articlesToBeClustered = q.getResultList();

        } catch (NoResultException e) {
            em.close();
            return Response.status(400).entity("Empty article dataset.").build();
        }

        IncrementalClusteringTask task = new IncrementalClusteringTask(
                parser.getConfig(), matcher, articlesToBeClustered, clustering);

        int id = super.executeTask(task);

        em.close();

        return Response.status(200).entity(id).build();

    }


}
