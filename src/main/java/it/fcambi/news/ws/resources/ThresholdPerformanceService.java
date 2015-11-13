package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.async.TaskStatus;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.metrics.*;
import it.fcambi.news.model.TFDictionary;
import it.fcambi.news.tasks.ComputeThresholdPerformanceTask;
import it.fcambi.news.tasks.ThresholdPerformanceResult;
import it.fcambi.news.ws.resources.dto.ProgressUpdateDTO;
import it.fcambi.news.ws.resources.dto.TaskDTO;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 09/11/15.
 */
@Path("/threshold-performance")
@Singleton
public class ThresholdPerformanceService extends TaskService<ComputeThresholdPerformanceTask> {

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
                              @QueryParam("keywordExtraction") String keywordExtraction) {

        MatchMapGeneratorConfigurationParser parser = new MatchMapGeneratorConfigurationParser();
        try {
            parser.parse(metricName, noiseWordsFilter, stemming, tfidf, keywordExtraction);
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }

        ComputeThresholdPerformanceTask task;
        if (start >= 0 && step > 0 && limit > 0)
            task = new ComputeThresholdPerformanceTask(parser.getConfig(), parser.getMetric(), start, step, limit);
        else
            return Response.status(400).entity("Invalid threshold range parameters.").build();

        int id = super.nextId();

        super.putTask(id, task);

        Application.getScheduler().schedule(task);

        return Response.status(201).entity(id).build();
    }

}
