package it.fcambi.news.ws.resources;

import it.fcambi.news.model.Newspaper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Francesco on 14/05/16.
 */
@Path("/newspapers")
public class NewspapersService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
//    @RolesAllowed({"user", "admin", "guest"})
    public Newspaper[] getNewspapers() {

        return Newspaper.values();

    }

}
