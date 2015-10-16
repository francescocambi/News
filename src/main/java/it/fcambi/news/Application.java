package it.fcambi.news;

import it.fcambi.news.ws.server.Server;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Francesco on 29/09/15.
 */
public class Application {

    private static EntityManagerFactory emFactory;
    private static Server httpServer;
    private static PropertyConfig props;

    private static final Logger log = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) {

        try {
            props = new PropertyConfig();
            setUpLogging();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        log.info("Initializing Entity Manager factory...");
        log.info("Persistence Unit: "+props.getProp("PERSISTENCE_UNIT"));
        emFactory = Persistence.createEntityManagerFactory(props.getProp("PERSISTENCE_UNIT"));

        log.info("Starting up Web Services...");
        log.info("Bind url >> "+props.getProp("BIND_URI"));
        httpServer = new Server(props);
        httpServer.startServer();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() { Application.tearDown(); }
        });

        log.info("Configuring scheduled tasks...");
        configureScheduledTasks();
        log.info(">>> READY! <<<");

        try {
            Thread.currentThread().join();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error joining curent thread.", e);
            System.exit(-1);
        }

    }

    private static void configureScheduledTasks() {
        if (Boolean.parseBoolean(props.getProp("HOURLY_ARTICLE_DOWNLOAD"))) return;
        //Execute Article Download task every hour
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable downloaderTask = new Runnable() {
            @Override
            public void run() {
                log.info("Launching articles download scheduled task");
                new ArticlesDownloader().downloadArticles();
            }
        };

        //Compute difference in minutes from now to next o'clock hour
        LocalTime now = LocalTime.now();
        int diff = 60-now.getMinute();

        scheduler.scheduleAtFixedRate(downloaderTask, diff, 60, TimeUnit.MINUTES);
    }

    private static void tearDown() {
        log.info("Shutting Down...");
        httpServer.stop();
        emFactory.close();
    }

    public static EntityManager getEntityManager() {
        return emFactory.createEntityManager();
    }

    public static void setUpLogging() throws IOException {
        String[] loggers = {
                Application.class.getName(),
                ArticlesDownloader.class.getName(),
                "it.fcambi.news.ws.resources.requests"
        };
        Level level = Level.ALL;

        //Set up logger
        for (String logger : loggers) {
            FileHandler handler = new FileHandler("./logs/"+logger+".log");
            Logger l = Logger.getLogger(logger);
            l.setLevel(level);
            handler.setLevel(level);
            handler.setFormatter(new SimpleFormatter());
            l.addHandler(handler);
        }
    }
}
