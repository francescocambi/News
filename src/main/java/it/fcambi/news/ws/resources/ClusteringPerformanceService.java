package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.clustering.HighestMeanMatcherFactory;
import it.fcambi.news.clustering.HighestMeanOverThresholdMatcherFactory;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.clustering.MatcherFactory;
import it.fcambi.news.model.Newspaper;
import it.fcambi.news.tasks.ClusteringPerformanceTask;

import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 11/12/15.
 */
@Path("/clustering-performance")
@Singleton
@RolesAllowed({"user", "admin", "guest"})
public class ClusteringPerformanceService extends TaskService<ClusteringPerformanceTask> {

    @GET
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNew(@QueryParam("threshold") double threshold,
                              @QueryParam("testSet") float testSetFraction,
                              @QueryParam("metricName") String metricName,
                              @QueryParam("noiseWordsFilter") boolean noiseWordsFilter,
                              @QueryParam("stemming") boolean stemming,
                              @QueryParam("tfidf") boolean tfidf,
                              @QueryParam("keywordExtraction") String keywordExtraction,
                              @QueryParam("matcherName") String matcherName,
                              @QueryParam("newspapers") String newspapersString) {

        MatchMapGeneratorConfigurationParser parser = new MatchMapGeneratorConfigurationParser();
        try {
            parser.parse(metricName, noiseWordsFilter, stemming, tfidf, keywordExtraction);
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }

        //Detect matcher
        MatcherFactory matcherFactory;
        switch (matcherName) {
            case "highest_mean":
                matcherFactory = new HighestMeanMatcherFactory();
                break;
            case "highest_mean_over_threshold":
                matcherFactory = new HighestMeanOverThresholdMatcherFactory();
                break;
            default:
                return Response.status(400).entity("Invalid matcher name.").build();
        }

        MatchMapGeneratorConfiguration conf = parser.getConfig();

        //Detects excluded newspapers
        List<String> newspapers = Arrays.asList(newspapersString.split(","));
        List<Newspaper> newspapersToExclude = Arrays.stream(Newspaper.values())
                .filter(x -> !newspapers.contains(x.toString())).collect(Collectors.toList());

        ClusteringPerformanceTask task = new ClusteringPerformanceTask(conf, parser.getMetric(),
                matcherFactory, threshold, testSetFraction, newspapersToExclude);
        int id = super.nextId();
        super.putTask(id, task);
        Application.getScheduler().schedule(task);

        return Response.status(201).entity(id).build();
    }

}
