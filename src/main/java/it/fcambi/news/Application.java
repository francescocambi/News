package it.fcambi.news;

import it.fcambi.news.ws.server.Server;

import javax.persistence.EntityManager;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Francesco on 29/09/15.
 */
public class Application {

    private static PersistenceManager persistenceManager;
    private static Server httpServer;
    private static PropertyConfig props;

    private static Logger log;

    public static void main(String[] args) {

        String title = "\n"+
        "      _   __                     ___                \n"+
        "     / | / /__ _      _______   /   |  ____  ____   \n"+
        "    /  |/ / _ \\ | /| / / ___/  / /| | / __ \\/ __ \\  \n"+
        "   / /|  /  __/ |/ |/ (__  )  / ___ |/ /_/ / /_/ /  \n"+
        "  /_/ |_/\\___/|__/|__/____/  /_/  |_/ .___/ .___/   \n"+
        "                                   /_/   /_/        \n";

        System.out.println(title);
        System.out.println("Starting Up...");

                Logging.setUp();
        log = Logging.registerLogger(Application.class.getName());
        Logging.registerLogger("");
        Logging.registerLogger("org.hibernate", Level.WARNING);

        try {
            props = new PropertyConfig();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        log.info("Initializing Entity Manager factory...");
        log.info("Persistence Unit: "+props.getProp("PERSISTENCE_UNIT"));
        persistenceManager = new PersistenceManager(props.getProp("PERSISTENCE_UNIT"));

        log.info("Starting up Web Services...");
        log.info("Bind url >> "+props.getProp("BIND_URI"));
        httpServer = new Server(props);
        httpServer.startServer();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() { Application.tearDown(); }
        });

        configureScheduledTasks();

        log.info("Startup Completed - All OK");

        System.out.println("\nApplication Ready\n\t- Server @ "+props.getProp("BIND_URI")+"\n\t- GUI @ "+
                props.getProp("BIND_URI")+"/"+props.getProp("GUI_APP_PATH")+"app/\n");
        System.out.println("Press CTRL+C to stop...");

        try {
            Thread.currentThread().join();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error joining current thread.", e);
            System.exit(-1);
        }

    }

    private static void configureScheduledTasks() {
        if (!Boolean.parseBoolean(props.getProp("HOURLY_ARTICLE_DOWNLOAD"))) return;
        log.info("Configuring scheduled tasks...");
        //Execute Article Download task every hour
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable downloaderTask = () -> {
            new ArticlesDownloader().downloadArticles();
        };

        //Compute difference in minutes from now to next o'clock hour
        LocalTime now = LocalTime.now();
        int diff = 60-now.getMinute();

        scheduler.scheduleAtFixedRate(downloaderTask, diff, 60, TimeUnit.MINUTES);
    }

    private static void tearDown() {
        log.info("Shutting Down...");
        persistenceManager.close();
        httpServer.stop();
        Logging.tearDown();
    }

    public static EntityManager getEntityManager() {
        return persistenceManager.createEntityManager();
    }


}
