package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.tasks.ComputeThresholdPerformanceTask;

import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Francesco on 09/11/15.
 */
@Path("/threshold-performance")
@Singleton
@RolesAllowed({})
public class ThresholdPerformanceService extends TaskService<ComputeThresholdPerformanceTask> {

    @GET
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNew(@QueryParam("start") double start,
                              @QueryParam("step") double step,
                              @QueryParam("limit") int limit,
                              @QueryParam("metricName") String metricName,
                              @QueryParam("noiseWordsFilter") boolean noiseWordsFilter,
                              @QueryParam("language") String languageString,
                              @QueryParam("stemming") boolean stemming,
                              @QueryParam("tfidf") boolean tfidf,
                              @QueryParam("tfidfDictionary") String tfDictionaryName,
                              @QueryParam("keywordExtraction") String keywordExtraction) {

        MatchMapGeneratorConfigurationParser parser = new MatchMapGeneratorConfigurationParser();
        try {
            parser.parse(metricName, noiseWordsFilter, stemming, tfidf, tfDictionaryName, languageString, keywordExtraction);
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }

        MatchMapGeneratorConfiguration conf = parser.getConfig();
//        conf.setKeywordSelectionFunction((title, description, body) -> new Text(title, description, body));

        ComputeThresholdPerformanceTask task;
        if (start >= 0 && step > 0 && limit > 0)
            task = new ComputeThresholdPerformanceTask(conf, parser.getMetric(), start, step, limit);
        else
            return Response.status(400).entity("Invalid threshold range parameters.").build();

        int id = super.nextId();

        super.putTask(id, task);

        Application.getScheduler().schedule(task);

        return Response.status(201).entity(id).build();
    }

}
