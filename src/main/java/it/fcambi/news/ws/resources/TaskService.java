package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.async.Task;
import it.fcambi.news.async.TaskStatus;
import it.fcambi.news.tasks.ComputeThresholdPerformanceTask;
import it.fcambi.news.ws.resources.dto.TaskDTO;
import jdk.nashorn.internal.runtime.Logging;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 11/11/15.
 */
public abstract class TaskService<T extends Task> {

    private static Logger log = Logging.getLogger(TaskService.class.getName());

    private Map<Integer, T> tasks;
    private AtomicInteger nextId;

    public TaskService() {
        this.nextId = new AtomicInteger();
        this.tasks = new Hashtable<>();
        this.nextId.set(1);
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

    @GET
    @Path("/cancel/{taskId}")
    public Response cancelTask(@PathParam("taskId") int taskId) {
        if (tasks.get(taskId) == null)
            return Response.status(404).build();

        try {
            T task = this.tasks.get(taskId);
            if (Application.getScheduler().cancel(task))
                tasks.remove(taskId);
            else
                return Response.status(200).entity(false).build();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception canceling task "+tasks.get(taskId).getName(), e);
            return Response.status(500).entity(e.getMessage()).build();
        }

        return Response.status(200).entity(true).build();
    }


    protected final T getTask(int taskId) {
        return tasks.get(taskId);
    }

    protected final int nextId() {
        return nextId.getAndIncrement();
    }

    protected final void putTask(int id, T task) {
        this.tasks.put(id, task);
    }

    protected final int executeTask(T task) {
        int id = nextId();
        this.tasks.put(id, task);
        Application.getScheduler().schedule(task);
        return id;
    }
}
