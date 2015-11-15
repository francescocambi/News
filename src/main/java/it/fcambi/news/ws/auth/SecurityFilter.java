package it.fcambi.news.ws.auth;

import it.fcambi.news.Application;
import it.fcambi.news.Logging;
import it.fcambi.news.model.auth.Session;
import it.fcambi.news.model.auth.User;
import org.glassfish.jersey.server.ContainerRequest;

import javax.persistence.EntityManager;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Francesco on 07/10/15.
 */
@PreMatching
public class SecurityFilter implements ContainerRequestFilter {

    private final Logger log = Logging.registerLogger("it.fcambi.news.security");

    @Context
    UriInfo uriInfo;
    private static final String REALM = "NewsAppSecurityRealm";

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        User user = authenticate(containerRequestContext);
        if (user != null) {
            log.finer("Authenticated user "+user.getUsername());
            containerRequestContext.setSecurityContext(new Authorizer(user, uriInfo));
        }
    }

    private User authenticate(ContainerRequestContext request) {
        String authorization = request.getHeaders().getFirst(ContainerRequest.AUTHORIZATION);
        if (authorization == null)
            return null;

        //Retrieve session
        try {
            EntityManager em = Application.createEntityManager();
            Session s = em.find(Session.class, Long.parseLong(authorization));
            em.close();
            if (s.isValid())
                return s.getUser();
            else
                return null;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception trying to retrieve user session.", e);
            return null;
        }
    }
}
