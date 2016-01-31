package it.fcambi.news.ws.server;

import it.fcambi.news.Application;
import it.fcambi.news.Logging;
import it.fcambi.news.PropertyConfig;
import it.fcambi.news.ws.auth.SecurityFilter;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        rc.register(JacksonFeature.class);
        rc.register(CORSFilter.class);
        rc.register(SecurityFilter.class);
        rc.register(RolesAllowedDynamicFeature.class);

        //SSL configuration
        SSLContextConfigurator sslContext = new SSLContextConfigurator();
        sslContext.setKeyStoreFile("sslkeys/keystore.jks");
        byte[] ks = new byte[5120];
        try {
            getClass().getResourceAsStream("/sslkeys/keystore.jks").read(ks);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read keystore file.");
        }
        sslContext.setKeyStoreBytes(ks);
        sslContext.setKeyStorePass("francesco");

        URI BASE_URI = UriBuilder.fromUri(props.getProp("BIND_URI")).build();

        if (!sslContext.validateConfiguration(true)) {
            log.severe(" !!! Invalid SSL Configuration");
        } else {
            log.info("SSL Configuration OK");
        }

        SSLEngineConfigurator sslConf = new SSLEngineConfigurator(sslContext, false, false, false);
        server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc, true, sslConf);

        if (Boolean.parseBoolean(props.getProp("DEVELOPMENT_MODE"))) {
            log.info("Setting development document root "+props.getProp("DEV_GUI_APP_PATH"));
            StaticHttpHandler staticHttpHandler = new StaticHttpHandler(props.getProp("DEV_GUI_APP_PATH"));
            staticHttpHandler.setFileCacheEnabled(false);
            server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/gui");
        } else {
            log.info("Setting in-jar document root");
            CLStaticHttpHandler staticHttpHandler = new CLStaticHttpHandler(ClassLoader.getSystemClassLoader(), "/webapp/");
            server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/gui");
        }

        log.info("Web Services initialization completed.");

        // Set up web services logger
        if (Boolean.parseBoolean(props.getProp("WS_FINE_LOGGING"))) {
            Logger l = Logging.registerLogger("org.glassfish.grizzly.http.server", Level.ALL);
        }

        return server;
    }

    public void stop() {
        server.shutdownNow();
    }

}
