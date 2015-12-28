package it.fcambi.news.ws.resources;

import it.fcambi.news.relevance.NewsRelevance;
import it.fcambi.news.ws.resources.dto.NewsWithRelevanceDto;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 27/12/15.
 */
@Path("/news-relevances")
@RolesAllowed({"user", "admin"})
public class NewsRelevanceService extends NewsRelevancesManager {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRelevances(@QueryParam("clustering") String clusteringName) {

        List<NewsRelevance> relevances;
        try {
            relevances = retrieveOrGenerateRelevances(clusteringName);
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity("\""+e.getMessage()+"\"").build();
        }

        List<NewsWithRelevanceDto> dtos = relevances.stream().map(NewsWithRelevanceDto::new)
                .sorted((a,b) -> Double.compare(b.getRelevanceSum(), a.getRelevanceSum()))
                .collect(Collectors.toList());

        return Response.status(200).entity(dtos).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewsRelevance(@PathParam("id") long newsId,
                                     @QueryParam("clustering") String clusteringName) {

        List<NewsRelevance> relevances;
        try {
            relevances = retrieveOrGenerateRelevances(clusteringName);
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity("\""+e.getMessage()+"\"").build();
        }

        NewsRelevance rel = relevances.stream().filter(r -> r.getNews().getId() == newsId).findFirst().orElse(null);

        return Response.status(200).entity(rel).build();

    }

}
