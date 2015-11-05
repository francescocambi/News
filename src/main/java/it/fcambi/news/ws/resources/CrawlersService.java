package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.async.Scheduler;
import it.fcambi.news.async.Task;
import it.fcambi.news.async.TaskStatus;
import it.fcambi.news.tasks.ArticlesDownloaderTask;
import it.fcambi.news.ProgressHolder;

import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Francesco on 27/09/15.
 */
@Path("/crawlers")
@Singleton
@RolesAllowed({"user", "admin"})
public class CrawlersService {

    private Task downloaderTask = new ArticlesDownloaderTask();

    public CrawlersService() {
        this.downloaderTask = new ArticlesDownloaderTask();
        this.downloaderTask.setCreator(this.getClass().getName());
    }

    @GET
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean startDownload() {

        if (downloaderTask.getStatus() != TaskStatus.QUEUED
                && downloaderTask.getStatus() != TaskStatus.RUNNING) {
            Application.getScheduler().schedule(downloaderTask);
        }

        return true;
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean getStatus() {
        return (downloaderTask.getStatus() == TaskStatus.QUEUED
                || downloaderTask.getStatus() == TaskStatus.RUNNING);
    }

    @GET
    @Path("/progress")
    @Produces(MediaType.APPLICATION_JSON)
    public double getProgress() {
        return downloaderTask.getProgress();
    }


}
