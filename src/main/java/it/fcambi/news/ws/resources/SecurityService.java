package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.model.auth.Session;
import it.fcambi.news.model.auth.User;
import it.fcambi.news.ws.resources.dto.AuthenticationDTO;
import it.fcambi.news.ws.resources.dto.LoginRequestDTO;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by Francesco on 07/10/15.
 */
@Path("/security")
public class SecurityService {

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public SecurityContext getSecurityContext(@Context SecurityContext sc) {
//        return sc;
//    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateUser(LoginRequestDTO loginRequest) {

        User u;
        EntityManager em = Application.getEntityManager();
        try {
            u = em.createQuery("select u from User u where u.username=?1 and u.password=?2", User.class)
                .setParameter(1, loginRequest.getUsername())
                .setParameter(2, loginRequest.getPassword()).getSingleResult();
        } catch (NoResultException e) {
            return Response.status(401).build();
        }

        Session s = new Session(u);

        em.getTransaction().begin();
        em.persist(s);
        em.getTransaction().commit();

        AuthenticationDTO dto = new AuthenticationDTO();
        dto.setSessionId(""+s.getId());

        return Response.status(200).entity(dto).build();
    }

//    @GET
//    @Path("/check")
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response checkSession(@Context SecurityContext sc) {
//        if (sc.getUserPrincipal() != null)
//            return Response.status(200).build();
//        else
//            return Response.status(401).build();
//    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logout(AuthenticationDTO session, @Context SecurityContext sc) {

        if (sc.getUserPrincipal() == null) {
            return Response.status(401).build();
        }

        Session s;
        EntityManager em = Application.getEntityManager();
        try {
            s = em.find(Session.class, Long.parseLong(session.getSessionId()));
        } catch (NoResultException e) {
            return Response.status(401).build();
        }
        s.logout();
        em.getTransaction().begin();
        em.merge(s);
        em.getTransaction().commit();

        return Response.status(200).build();
    }



}
