package it.fcambi.news.ws.resources;

import it.fcambi.news.relevance.NewsRelevance;
import it.fcambi.news.ws.resources.dto.ChartPoint;
import it.fcambi.news.ws.resources.dto.LifetimeInfoDTO;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 28/12/15.
 */
@Path("/news-lifetime")
//@RolesAllowed({"user", "admin"})
public class NewsLifetimeService extends NewsRelevancesManager {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewsLifetimeInfo(@QueryParam("clustering") String clusteringName) {

        List<NewsRelevance> relevances;
        try {
            relevances = retrieveOrGenerateRelevances(clusteringName);
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity("\""+e.getMessage()+"\"").build();
        }

        class NewsLifetimePair {
            NewsRelevance news;
            long lifetime;

            public NewsLifetimePair(NewsRelevance news, long lifetime) {
                this.news = news;
                this.lifetime = lifetime;
            }
        }

        List<NewsLifetimePair> lifetimes = relevances.stream().map(r -> new NewsLifetimePair(r, r.getLifetime()))
                .sorted((a,b) -> Long.compare(a.lifetime, b.lifetime)).collect(Collectors.toList());

        double average = lifetimes.stream().mapToLong(x -> x.lifetime).average().orElse(-1);
        long min = lifetimes.stream().min((a,b) -> Long.compare(a.lifetime, b.lifetime)).get().lifetime;
        long max = lifetimes.stream().max((a,b) -> Long.compare(a.lifetime, b.lifetime)).get().lifetime;
        double interquartileMean = lifetimes.subList(Math.round(0.1F*lifetimes.size()), Math.round(0.9F*lifetimes.size()))
                .stream().mapToLong(x -> x.lifetime).average().orElse(-1);

        double median;
        if (lifetimes.size() % 2 == 0)
            median = ( lifetimes.get( (lifetimes.size()/2)-1 ).lifetime + lifetimes.get( (lifetimes.size()/2) ).lifetime ) / 2;
        else
            median = ( lifetimes.get( (lifetimes.size()-1)/2 ).lifetime );

        List<ChartPoint<String, Long>> distribution = lifetimes.stream()
                .map(p -> new ChartPoint<String, Long>(p.news.getNews().getDescription(), p.lifetime))
                .collect(Collectors.toList());

        LifetimeInfoDTO info = new LifetimeInfoDTO();
        info.setAverage(average);
        info.setMax(max);
        info.setMin(min);
        info.setInterquartileMean(interquartileMean);
        info.setMedian(median);

        info.setDistribution(distribution);

        return Response.status(200).entity(info).build();
    }

}
