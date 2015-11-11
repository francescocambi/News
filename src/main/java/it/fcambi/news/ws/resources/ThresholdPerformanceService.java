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
public class ThresholdPerformanceService {

    private Map<Integer, ComputeThresholdPerformanceTask> tasks;
    private AtomicInteger nextId;

    public ThresholdPerformanceService() {
        this.nextId = new AtomicInteger();
        this.nextId.set(1);
        this.tasks = new Hashtable<>();
    }

    @GET
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNew(@QueryParam("start") double start,
                              @QueryParam("step") double step,
                              @QueryParam("limit") int limit,
                              @QueryParam("metricName") String metricName,
                              @QueryParam("noiseWordsFilter") boolean noiseWordsFilter,
                              @QueryParam("stemming") boolean stemming,
                              @QueryParam("tfidf") boolean tfidf) {

        if (metricName == null)
            return Response.status(400).build();

        MatchMapGeneratorConfiguration config = new MatchMapGeneratorConfiguration();
        if (noiseWordsFilter) config.addTextFilter(new NoiseWordsTextFilter());
        if (stemming) config.addTextFilter(new StemmerTextFilter());
        if (tfidf) {
            EntityManager em = Application.getEntityManager();
            TFDictionary dict = em.find(TFDictionary.class, "italian_stemmed");
            config.setWordVectorFactory(new TFIDFWordVectorFactory(dict));
            em.close();
        }
        Metric metric;
        switch (metricName) {
            case "cosine":
                metric = new CosineSimilarity();
                break;
            case "jaccard":
                metric = new JaccardSimilarity();
                break;
            case "combined":
                metric = new MyMetric();
                break;
            case "tanimoto":
                metric = new TanimotoSimilarity();
                break;
            default:
                metric = new CosineSimilarity();
                break;
        }
        config.addMetric(metric);

        ComputeThresholdPerformanceTask task;
        if (start >= 0 && step > 0 && limit > 0)
            task = new ComputeThresholdPerformanceTask(config, metric, start, step, limit);
        else
            task = new ComputeThresholdPerformanceTask(config, metric);

        int id = this.nextId.getAndIncrement();

        this.tasks.put(id, task);

        Application.getScheduler().schedule(task);

        return Response.status(201).entity(id).build();
    }

    @GET
    @Path("/progress/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProgressForTask(@PathParam("taskId") int taskId) {
        if (tasks.get(taskId) == null)
            return Response.status(404).build();
        return Response.status(200).entity(tasks.get(taskId).getProgress()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TaskDTO> getAllTasks() {
        return tasks.entrySet().stream().map(entry -> new TaskDTO(entry.getKey(), entry.getValue().getCreationTime(),
                    entry.getValue().getProgress(), entry.getValue().getStatus())
        ).collect(Collectors.toList());
    }

    @GET
    @Path("/results/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResult(@PathParam("taskId") int taskId) {
        if (tasks.get(taskId) == null)
            return Response.status(404).build();

        if (tasks.get(taskId).getStatus() != TaskStatus.COMPLETED)
            return Response.status(400).build();

        return Response.status(200).entity(tasks.get(taskId).getResults()).build();
    }

}
