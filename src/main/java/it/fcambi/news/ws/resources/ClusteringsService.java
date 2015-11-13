package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.model.Clustering;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Francesco on 13/11/15.
 */
@Path("/clusterings")
public class ClusteringsService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Clustering> getClusterings() {

        EntityManager em = Application.getEntityManager();
        List<Clustering> clusterings;

        try {
            clusterings = em.createQuery("select c from Clustering c", Clustering.class).getResultList();
        } catch (NoResultException e) {
            clusterings = null;
        }

        em.close();

        return clusterings;
    }

}
