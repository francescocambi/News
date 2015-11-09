package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.data.NewsVector;
import it.fcambi.news.metrics.permutations.BasicDistance;
import it.fcambi.news.model.FrontPage;
import it.fcambi.news.model.Newspaper;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 12/10/15.
 */
@Path("/front-pages")
@RolesAllowed({"user", "admin"})
public class FrontPagesService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List getAll() {
        EntityManager em = Application.getEntityManager();
        List pages = em.createQuery("select p.id, p.timestamp, p.newspaper from FrontPage p")
                .getResultList();
        em.close();
        return pages;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public FrontPage getById(@PathParam("id") long id) {
        EntityManager em = Application.getEntityManager();
        FrontPage page = em.find(FrontPage.class, id);
        em.close();
        return page;
    }

    @GET
    @Path("/stats/changes-time")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Newspaper, Double> getChangesOnTime() {
        EntityManager em = Application.getEntityManager();
        List<FrontPage> pages = em.createQuery("select p from FrontPage p", FrontPage.class).getResultList();

        //Separates front pages for each newspaper
        Map<Newspaper, List<FrontPage>> pagesByNewspaper = pages.stream().collect(Collectors.groupingBy(FrontPage::getNewspaper));

        Map<Newspaper, Double> changesByNewspaper = new HashMap<>();
        //Work on front pages lists
        pagesByNewspaper.entrySet().forEach(entry -> {
            List<FrontPage> ps = entry.getValue();
            //Sort by timestamp
            ps.sort(Comparator.comparing(FrontPage::getTimestamp));

            //Count changes between each scan
            List<NewsVector> newsVectors = ps.stream().map(NewsVector::new).collect(Collectors.toList());
            BasicDistance d = new BasicDistance();
            double changes = 0;
            for (int i=1; i<newsVectors.size(); i++) {
                changes += d.compute(newsVectors.get(i-1).getNewsIds(), newsVectors.get(i).getNewsIds());
            }

            //Computes mean
            double mean = changes / (newsVectors.size()-1);

            changesByNewspaper.put(entry.getKey(), mean);
        });

        em.close();

        return changesByNewspaper;
    }


    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Newspaper, Map<Date, Double>> someMethodName() {

        Map<Newspaper, Map<Date, Double>> stats = new HashMap<>();

        EntityManager em = Application.getEntityManager();
        List<FrontPage> pages = em.createQuery("select p from FrontPage p", FrontPage.class).getResultList();

        // Create Newspaper - FrontPage map
        Map<Newspaper, List<FrontPage>> pagesByNewspaper = pages.stream().collect(Collectors.groupingBy(FrontPage::getNewspaper));

        // Iterates over frontpages list of each newspaper
        pagesByNewspaper.entrySet().stream().forEach(e -> {
            stats.put(e.getKey(), new HashMap<>());

            //Compute distance between pages at each timestamp gap
            BasicDistance d = new BasicDistance();
            List<FrontPage> l = e.getValue();
            for (int i=1; i<l.size(); i++) {
                double distance = d.compute(
                        new NewsVector(l.get(i-1)).getNewsIds(),
                        new NewsVector(l.get(i)).getNewsIds());
                stats.get(e.getKey()).put(l.get(i).getTimestamp(), distance);
            }

        });

        return stats;

    }



}
