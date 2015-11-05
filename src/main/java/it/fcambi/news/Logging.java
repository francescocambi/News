package it.fcambi.news;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.*;

/**
 * Created by Francesco on 03/11/15.
 */
public class Logging {

    private static Map<String, Handler> logHandlers = new Hashtable<>();
    private static final int FILE_LIMIT_BYTES = 4*1024*1024;
    private static final int ROTATING_FILES_NUM = 1;

    private static final Level LEVEL = Level.INFO;

    public static void setUp() {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$TF %1$TT] [%2$s] %4$s: %5$s %6$s%n");

        Logger root = Logger.getLogger("");
        for (Handler h : root.getHandlers())
            root.removeHandler(h);
        registerLogger(root);
    }

    public static Logger registerLogger(Logger logger, Level requiredLevel) {

        if (logHandlers.containsKey(logger.getName()))
            return logger;

        Handler handler = new ConsoleHandler();
        try {
//            handler = new FileHandler("./logs/" + (logger.getName().equals("") ? "root" : logger.getName()) + ".log",
//                    FILE_LIMIT_BYTES, ROTATING_FILES_NUM, true);
        } catch (Exception e) {
            e.printStackTrace();
            handler = new ConsoleHandler();
        }

        logger.setUseParentHandlers(false);
        logger.setLevel(requiredLevel);

        handler.setLevel(LEVEL);
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        logHandlers.put(logger.getName(), handler);

        return logger;
    }

    public static Logger registerLogger(String loggerName) {
        return registerLogger(Logger.getLogger(loggerName), LEVEL);
    }

    public static Logger registerLogger(String loggerName, Level requiredLevel) {
        return registerLogger(Logger.getLogger(loggerName), requiredLevel);
    }

    public static Logger registerLogger(Logger logger) {
        return registerLogger(logger, LEVEL);
    }

    public static void tearDown() {
        logHandlers.values().forEach( h -> h.close() );
    }

}
