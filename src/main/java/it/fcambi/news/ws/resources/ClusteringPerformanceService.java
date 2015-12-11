package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.clustering.*;
import it.fcambi.news.tasks.ComputeClusteringPerformanceTask;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Francesco on 11/12/15.
 */
@Path("/clustering-performance")
@Singleton
public class ClusteringPerformanceService extends TaskService<ComputeClusteringPerformanceTask> {

    @GET
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNew(@QueryParam("start") double start,
                              @QueryParam("step") double step,
                              @QueryParam("limit") int limit,
                              @QueryParam("metricName") String metricName,
                              @QueryParam("noiseWordsFilter") boolean noiseWordsFilter,
                              @QueryParam("stemming") boolean stemming,
                              @QueryParam("tfidf") boolean tfidf,
                              @QueryParam("keywordExtraction") String keywordExtraction,
                              @QueryParam("matcherName") String matcherName) {

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

        if ( !(start >= 0 && step > 0 && limit > 0) ) {
            return Response.status(400).entity("Invalid threshold range parameters.").build();
        }

        ComputeClusteringPerformanceTask task = new ComputeClusteringPerformanceTask(parser.getConfig(),
                parser.getMetric(), matcherFactory, start, step, limit);
        int id = super.nextId();
        super.putTask(id, task);
        Application.getScheduler().schedule(task);

        return Response.status(201).entity(id).build();
    }

}
