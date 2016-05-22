package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.model.TFDictionary;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Francesco on 14/05/16.
 */
@Path("/dictionaries")
public class TFDictionaryService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
//    @RolesAllowed({"user", "admin", "guest"})
    public List<TFDictionary> getDictionaries() {
        EntityManager em = Application.createEntityManager();

        List<TFDictionary> dicts = em.createQuery("select d from TFDictionary d", TFDictionary.class).getResultList();

        em.close();

        return dicts;
    }
}
