package it.fcambi.news.ws.resources;

import it.fcambi.news.model.Language;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Francesco on 15/05/16.
 */
@Path("/languages")
public class LanguagesService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin", "guest"})
    public Language[] getLanguages() {
        return Language.values();
    }

}
