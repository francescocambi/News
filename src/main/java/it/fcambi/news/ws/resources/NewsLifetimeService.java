package it.fcambi.news.ws.resources;

import it.fcambi.news.relevance.NewsRelevance;
import it.fcambi.news.ws.resources.dto.LifetimeInfoDTO;
import it.fcambi.news.ws.resources.dto.NewsLifetimeDTO;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 28/12/15.
 */
@Path("/news-lifetime")
@RolesAllowed({"user", "admin", "guest"})
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

        LifetimeInfoDTO info = new LifetimeInfoDTO();

        // Works on relevance lifetime
        List<NewsLifetimePair> lifetimes = relevances.stream().map(r -> new NewsLifetimePair(r, r.getLifetime()))
                .sorted((a,b) -> Long.compare(a.lifetime, b.lifetime)).collect(Collectors.toList());

        double average = lifetimes.stream().mapToLong(x -> x.lifetime).average().orElse(-1);
        long min = lifetimes.stream().min((a,b) -> Long.compare(a.lifetime, b.lifetime)).get().lifetime;
        long max = lifetimes.stream().max((a,b) -> Long.compare(a.lifetime, b.lifetime)).get().lifetime;
        double interdecileMean = lifetimes.subList(Math.round(0.1F*lifetimes.size()), Math.round(0.9F*lifetimes.size()))
                .stream().mapToLong(x -> x.lifetime).average().orElse(-1);

        double median;
        if (lifetimes.size() % 2 == 0)
            median = ( lifetimes.get( (lifetimes.size()/2)-1 ).lifetime + lifetimes.get( (lifetimes.size()/2) ).lifetime ) / 2;
        else
            median = ( lifetimes.get( (lifetimes.size()-1)/2 ).lifetime );

        //Works on time range
        List<NewsLifetimePair> timeRangeLifetimes = relevances.stream().map(r -> new NewsLifetimePair(r, r.getTimeRange()))
                .sorted((a,b) -> Long.compare(a.lifetime, b.lifetime)).collect(Collectors.toList());

        double timeRangeAvg = timeRangeLifetimes.stream().mapToLong(x -> x.lifetime).average().orElse(-1);
        long timeRangeMin = timeRangeLifetimes.stream().min((a,b) -> Long.compare(a.lifetime, b.lifetime)).get().lifetime;
        long timeRangeMax = timeRangeLifetimes.stream().max((a,b) -> Long.compare(a.lifetime, b.lifetime)).get().lifetime;
        double timeRangeInterdecileMean = timeRangeLifetimes.subList(Math.round(0.1F*timeRangeLifetimes.size()), Math.round(0.9F*timeRangeLifetimes.size()))
                .stream().mapToLong(x -> x.lifetime).average().orElse(-1);

        double timeRangeMedian;
        if (timeRangeLifetimes.size() % 2 == 0)
            timeRangeMedian = ( timeRangeLifetimes.get( (timeRangeLifetimes.size()/2)-1 ).lifetime
                    + timeRangeLifetimes.get( (timeRangeLifetimes.size()/2) ).lifetime ) / 2;
        else
            timeRangeMedian = ( timeRangeLifetimes.get( (timeRangeLifetimes.size()-1)/2 ).lifetime );

        List<NewsLifetimeDTO> lifetimeDTOs = relevances.stream().map(newsRelevance -> new NewsLifetimeDTO(newsRelevance.getNews(), newsRelevance.getLifetime(), newsRelevance.getTimeRange()))
                .collect(Collectors.toList());

        info.setLifetimes(lifetimeDTOs);

        // TODO Valutare calcolo aggregati a partire da lifetimeDTOs

//        info.getDistributions().entrySet().forEach(point -> {
//            if (point.getValue().get("relevanceLifetime") > point.getValue().get("timeRange"))
//                throw new IllegalStateException("Relevance lifetime > time range");
//        });

        info.setRelevanceLifetimeAvg(average);
        info.setRelevanceLifetimeMax(max);
        info.setRelevanceLifetimeMin(min);
        info.setRelevanceLifetimeMedian(median);
        info.setRelevanceLifetimeInterdecileMean(interdecileMean);

        info.setTimeRangeAvg(timeRangeAvg);
        info.setTimeRangeMax(timeRangeMax);
        info.setTimeRangeMin(timeRangeMin);
        info.setTimeRangeMedian(timeRangeMedian);
        info.setTimeRangeInterdecileMean(timeRangeInterdecileMean);

        return Response.status(200).entity(info).build();
    }

}
