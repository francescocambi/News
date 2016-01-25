package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.model.Clustering;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by Francesco on 13/11/15.
 */
@Path("/clusterings")
@RolesAllowed({"user", "admin"})
public class ClusteringsService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Clustering> getClusterings() {

        EntityManager em = Application.createEntityManager();
        List<Clustering> clusterings;

        try {
            clusterings = em.createQuery("select c from Clustering c", Clustering.class).getResultList();
        } catch (NoResultException e) {
            clusterings = null;
        }

        em.close();

        return clusterings;
    }

    @DELETE
    @Path("/{name}")
    public Response deleteClustering(@PathParam("name") String clusteringName) {

        EntityManager em = Application.createEntityManager();
        Clustering clustering = em.find(Clustering.class, clusteringName);

        if (clustering == null)
            return Response.status(404).entity("Clustering not found for this name.").build();

        try {
            em.getTransaction().begin();
            Query q = em.createQuery("delete from News n where n.clustering.name = :name");
            q.setParameter("name", clusteringName);
            q.executeUpdate();
            em.remove(clustering);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            return Response.status(500).entity(e.getMessage()).build();
        }

        return Response.status(200).build();

    }

}
