package it.fcambi.news.ws.auth;

import it.fcambi.news.model.auth.User;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.security.Principal;

/**
 * Created by Francesco on 07/10/15.
 */
public class Authorizer implements SecurityContext {

    private User user;
    private UriInfo uriInfo;

    public Authorizer(final User user, UriInfo uriInfo) {
        this.user = user;
        this.uriInfo = uriInfo;
    }

    @Override
    public Principal getUserPrincipal() {
        return this.user;
    }

    @Override
    public boolean isUserInRole(String role) {
        return user.getRole().equals(role);
    }

    @Override
    public boolean isSecure() {
        return "https".equals(uriInfo.getRequestUri().getScheme());
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.FORM_AUTH;
    }
}
