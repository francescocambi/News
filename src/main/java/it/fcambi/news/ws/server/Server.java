package it.fcambi.news.ws.server;

import it.fcambi.news.Application;
import it.fcambi.news.PropertyConfig;
import it.fcambi.news.ws.auth.SecurityFilter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.logging.*;

/**
 * Created by Francesco on 03/10/15.
 */
public class Server {
    private HttpServer server;
    private PropertyConfig props;

    private static final Logger log = Logger.getLogger(Application.class.getName());

    public Server(PropertyConfig p) {
        this.props = p;
    }

    public HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("it.fcambi.news.ws.resources");
        rc.register(CORSFilter.class);
        rc.register(SecurityFilter.class);
        rc.register(RolesAllowedDynamicFeature.class);

        //SSL configuration
        SSLContextConfigurator sslContext = new SSLContextConfigurator();
        sslContext.setKeyStoreFile("sslkeys/keystore.jks");
        sslContext.setKeyStorePass("francesco");

        URI BASE_URI = UriBuilder.fromUri(props.getProp("BIND_URI")).build();

        if (!sslContext.validateConfiguration(true)) {
            log.severe(">>> SSL CONFIGURATION NOT VALID <<<");
        } else {
            log.info(">>> SSL CONFIGURATION VALID <<<");
        }

        SSLEngineConfigurator sslConf = new SSLEngineConfigurator(sslContext, false, false, false);
        server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc, true, sslConf);
        server.getServerConfiguration().addHttpHandler(new StaticHttpHandler(props.getProp("GUI_APP_PATH")), "/gui");

        // Set up web services logger
        if (Boolean.parseBoolean(props.getProp("FINE_LOGGING"))) {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(new SimpleFormatter());
            Logger l = Logger.getLogger("org.glassfish.grizzly.http.server.HttpHandler");
            l.setLevel(Level.FINE);
            l.setUseParentHandlers(false);
            l.addHandler(consoleHandler);
            try {
                FileHandler webServicesFileHandler = new FileHandler("./logs/http.server.HttpHandler.log");
                webServicesFileHandler.setFormatter(new SimpleFormatter());
                webServicesFileHandler.setLevel(Level.ALL);
                l.addHandler(webServicesFileHandler);
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        return server;
    }

    public void stop() {
        server.shutdownNow();
    }

}
