package it.fcambi.news.ws.resources;

import it.fcambi.news.ArticlesDownloader;
import it.fcambi.news.ProgressHolder;
import it.fcambi.news.crawlers.Crawler;

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

    private Thread downloaderThread;
    private ProgressHolder taskProgress;

    @GET
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean startDownload() {
        if (downloaderThread == null || !downloaderThread.isAlive()) {
            taskProgress = new ProgressHolder();
            downloaderThread = new Thread(new Runnable() {
                public void run() {
                    ArticlesDownloader downloader = new ArticlesDownloader();
                    downloader.addProgressObserver(taskProgress);
                    downloader.downloadArticles();
                }
            });
        }

        if (!downloaderThread.isAlive())
            downloaderThread.start();

        return downloaderThread.isAlive();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean getStatus() {
        return (downloaderThread != null && downloaderThread.isAlive());
    }

    @GET
    @Path("/progress")
    @Produces(MediaType.APPLICATION_JSON)
    public float getProgress() {
        return taskProgress.getProgress();
    }


}
