package it.fcambi.news;

import it.fcambi.news.async.PastTaskTracer;
import it.fcambi.news.async.Scheduler;
import it.fcambi.news.tasks.ArticlesDownloaderTask;
import it.fcambi.news.ws.server.Server;

import javax.persistence.EntityManager;
import java.time.LocalTime;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Francesco on 29/09/15.
 */
public class Application {

    private static PersistenceManager persistenceManager;
    private static Server httpServer;
    private static PropertyConfig props;
    private static Scheduler scheduler;

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

        try {
            props = new PropertyConfig();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        Logging.setUp();
        log = Logging.registerLogger(Application.class.getName());
        Logging.registerLogger("");
        Logging.registerLogger("org.hibernate", Level.WARNING);

        log.info("Initializing Entity Manager factory...");
        log.info("Persistence Unit: "+props.getProp("PERSISTENCE_UNIT"));
        persistenceManager = new PersistenceManager(props.getProp("PERSISTENCE_UNIT"));

        log.info("Setting up Scheduler");
        configureScheduler();

        log.info("Starting up Web Services...");
        log.info("Bind url >> "+props.getProp("BIND_URI"));
        httpServer = new Server(props);
        httpServer.startServer();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() { Application.tearDown(); }
        });

        configureArticleDownloaderTask();

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

    private static void configureArticleDownloaderTask() {
        if (!Boolean.parseBoolean(props.getProp("HOURLY_ARTICLE_DOWNLOAD"))) return;
        log.info("Configuring hourly Article Download task...");
        ArticlesDownloaderTask task = new ArticlesDownloaderTask();
        task.setCreator(Application.class.getName());

        //Compute difference in minutes from now to next o'clock hour
//        LocalTime now = LocalTime.now();
//        int diff = 60-now.getMinute();
        long now = System.currentTimeMillis() / 3600000;
        long time = (now+1)*3600000;
        Date schedule = new Date(time);

        task.setScheduleTime(schedule);
        task.setPeriod(60*60*1000);

        scheduler.schedule(task);
    }

    private static void configureScheduler() {
        scheduler = new Scheduler();
        // This observer persist on db each tasks completed
        // tracing the history of passed tasks
        scheduler.addTaskCompletedObserver(new PastTaskTracer());
    }

    private static void tearDown() {
        log.info("Shutting Down...");
        persistenceManager.close();
        httpServer.stop();
        Logging.tearDown();
    }

    public static String getProperty(String name) {
        return props.getProp(name);
    }

    public static EntityManager getEntityManager() {
        return persistenceManager.createEntityManager();
    }

    public static Scheduler getScheduler() { return scheduler; }


}
