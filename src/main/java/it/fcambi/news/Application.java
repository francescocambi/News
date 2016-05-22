package it.fcambi.news;

import it.fcambi.news.async.PastTaskTracer;
import it.fcambi.news.async.Scheduler;
import it.fcambi.news.async.Task;
import it.fcambi.news.model.Language;
import it.fcambi.news.model.NoiseWordsList;
import it.fcambi.news.model.auth.User;
import it.fcambi.news.tasks.ArticlesDownloaderTask;
import it.fcambi.news.tasks.MultilevelClusteringPerformanceTask;
import it.fcambi.news.tasks.MultilevelIncrementalClusteringTask;
import it.fcambi.news.ws.server.Server;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Francesco on 29/09/15.
 */
public class Application {

    public static final String MANUAL_CLUSTERING_TFDICTIONARY = "clustering.manual.tfdictionary";
    public static final String MANUAL_CLUSTERING_LANGUAGE = "it";

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
        System.out.flush();

        if (args.length != 0 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h"))) {
            InputStream stream = Application.class.getResourceAsStream("/help.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            reader.lines().forEach(System.out::println);
            System.out.flush();
            return;
        }


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

        if (args.length != 0 && args[0].equals("multilevel-clustering")) {

            String clusteringName = args[1];
            double metaNewsThreshold = Double.parseDouble(args[2]);
            double newsThreshold = Double.parseDouble(args[3]);
            String tfDictionaryName = args[4];
            String languageString = args[5];

            try {
                MultilevelIncrementalClusteringTask.execTask(clusteringName, metaNewsThreshold,
                        newsThreshold, tfDictionaryName, languageString);
            } catch (Exception e) {
                e.printStackTrace();
                Application.tearDown();
                System.exit(-1);
            }

            Application.tearDown();
            System.exit(0);
        } else if (args.length != 0 && args[0].equals("multilevel-performance")) {

            double metaNewsThresholdSeed = Double.parseDouble(args[1]);
            int metaNewsThresholdLimit = Integer.parseInt(args[2]);
            double newsThresholdSeed = Double.parseDouble(args[3]);
            int newsThresholdLimit = Integer.parseInt(args[4]);
            String tfDictionaryName = args[5];
            String languageString = args[6];

            try {
                MultilevelClusteringPerformanceTask.execTask(metaNewsThresholdSeed, metaNewsThresholdLimit,
                        newsThresholdSeed, newsThresholdLimit, tfDictionaryName, languageString);
            } catch (Exception e) {
                e.printStackTrace();
                Application.tearDown();
                System.exit(-1);
            }

            Application.tearDown();
            System.exit(0);

        } else if (args.length != 0 && args[0].equals("generate-tf-dictionary")) {

            String dictionaryName = args[1];
            String languageString = args[2];

            if (dictionaryName == null || dictionaryName.length() == 0) {
                System.err.println("Please provide a valid name for TF Dictionary");
                Application.tearDown();
                System.exit(-1);
            }

            int returnCode = -1;

            try {
                TFDictionaryGenerationTask.generateDictionary(dictionaryName, languageString);
                returnCode = 0;
            } catch (Exception e) {
                e.printStackTrace();
                returnCode = -1;
            } finally {
                Application.tearDown();
                System.exit(returnCode);
            }

        } else if (args.length != 0 && args[0].equals("load-words-list")) {

            String filePath = args[1];
            String languageString = args[2];

            if (!Files.exists(Paths.get(filePath))) {
                System.err.println("File does not exists.");
                Application.tearDown();
                System.exit(-1);
            }

            Language language = null;
            try {
                language = Language.valueOf(languageString);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                Application.tearDown();
                System.exit(-1);
            }

            EntityManager em = Application.createEntityManager();

            em.getTransaction().begin();

            NoiseWordsList old = em.createQuery("select l from NoiseWordsList l where l.language=:lang", NoiseWordsList.class)
                    .setParameter("lang", language).getSingleResult();
            em.remove(old);

            NoiseWordsList list = new NoiseWordsList();
            list.setDescription(languageString);
            list.setLanguage(language);

            try {
                Files.lines(Paths.get(filePath)).forEach(w -> {
                    list.getWords().add(w.trim());
                });
            } catch (IOException e) {
                System.err.println("Cannot open file.");
                e.printStackTrace();
                Application.tearDown();
                System.exit(-1);
            }

            em.persist(list);

            em.getTransaction().commit();
            em.close();

            System.out.println("Task completed successfully.");
            Application.tearDown();
            System.exit(0);

        } else if (args.length != 0 && args[0].equals("add-user")) {
            if (args.length < 4) {
                System.err.println("Wrong command syntax, missing arguments.");
                Application.tearDown();
                System.exit(-1);
            }

            String username = args[1];
            String password = args[2];
            String role = args[3];

            if (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("guest")) {
                System.err.println("Role can be one of [admin, guest]");
                Application.tearDown();
                System.exit(-1);
            }

            EntityManager em = Application.createEntityManager();

            User user = null;
            try {
                user = em.createQuery("select u from User u where u.username=:uname", User.class)
                        .setParameter("uname", username).getSingleResult();
            } catch (NoResultException e) {
                user = new User();
                user.setEmail("");
            }

            user.setUsername(username);
            user.setPassword(password);
            user.setRole(role.toLowerCase());

            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();

            System.out.println("User successfully created.");
            Application.tearDown();
            System.exit(0);
        }

        log.info("Setting up Scheduler");
        Task.setLogger(Logging.registerLogger("it.fcambi.news.Tasks"));
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
                props.getProp("BIND_URI")+"/gui/app/\n");
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
        if (log != null)
            log.info("Shutting Down...");
        if (persistenceManager != null)
            persistenceManager.close();
        if (httpServer != null)
            httpServer.stop();
        Logging.tearDown();
    }

    public static String getProperty(String name) {
        return props.getProp(name);
    }

    public static EntityManager createEntityManager() {
        return persistenceManager.createEntityManager();
    }

    public static Scheduler getScheduler() { return scheduler; }


}
