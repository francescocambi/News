package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.data.NewsVector;
import it.fcambi.news.metrics.permutations.BasicDistance;
import it.fcambi.news.metrics.permutations.KendallTau;
import it.fcambi.news.metrics.permutations.PermutationsMetric;
import it.fcambi.news.model.Clustering;
import it.fcambi.news.model.FrontPage;
import it.fcambi.news.model.Newspaper;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 12/10/15.
 */
@Path("/front-pages")
//@RolesAllowed({"user", "admin"})
public class FrontPagesService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List getAll() {
        EntityManager em = Application.createEntityManager();
        List pages = em.createQuery("select p.id, p.timestamp, p.newspaper from FrontPage p")
                .getResultList();
        em.close();
        return pages;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public FrontPage getById(@PathParam("id") long id) {
        EntityManager em = Application.createEntityManager();
        FrontPage page = em.find(FrontPage.class, id);
        em.close();
        return page;
    }

    @GET
    @Path("/stats/changes-time")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Newspaper, Double> getChangesOnTime() {
        EntityManager em = Application.createEntityManager();
        List<FrontPage> pages = em.createQuery("select p from FrontPage p join p.articles a where key(a.news)='manual'", FrontPage.class)
                .getResultList();

        Clustering manual = em.find(Clustering.class, "manual");

        //Separates front pages for each newspaper
        Map<Newspaper, List<FrontPage>> pagesByNewspaper = pages.stream().collect(Collectors.groupingBy(FrontPage::getNewspaper));

        Map<Newspaper, Double> changesByNewspaper = new HashMap<>();
        //Work on front pages lists
        pagesByNewspaper.entrySet().forEach(entry -> {
            List<FrontPage> ps = entry.getValue();
            //Sort by timestamp
            ps.sort(Comparator.comparing(FrontPage::getTimestamp));

            //Count changes between each scan
            List<NewsVector> newsVectors = ps.stream().map(fp -> new NewsVector(fp, manual)).collect(Collectors.toList());
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
    public Map<Newspaper, Map<String, List>> someMethodName() {

        Map<Newspaper, Map<String, List>> stats = new HashMap<>();

        EntityManager em = Application.createEntityManager();

        List<FrontPage> pages = em.createQuery("select p from FrontPage p where p.timestamp <= '2015-10-17T09:02'", FrontPage.class)
                .getResultList();

        // Create Newspaper - FrontPage map
        Map<Newspaper, List<FrontPage>> pagesByNewspaper = pages.stream().collect(Collectors.groupingBy(FrontPage::getNewspaper));

        // Iterates over frontpages list of each newspaper
        String[] clusterings = { "manual", "auto_test" };
        pagesByNewspaper.entrySet().stream().forEach(e -> {
            stats.put(e.getKey(), new HashMap<>());
            stats.get(e.getKey()).put("dates", new Vector<>());

            List<FrontPage> l = e.getValue();

            for (int i = 1; i<l.size(); i++ ) {
                stats.get(e.getKey()).get("dates").add(l.get(i).getTimestamp());
            }

            for (String clusteringName : clusterings) {
                stats.get(e.getKey()).put(clusteringName, new Vector<>());

                Clustering clustering = em.find(Clustering.class, clusteringName);

                //Compute distance between pages at each timestamp gap
                PermutationsMetric d = new KendallTau();
                for (int i=1; i<l.size(); i++) {
                    double distance = d.compute(
                            new NewsVector(l.get(i-1), clustering).getNewsIds(),
                            new NewsVector(l.get(i), clustering).getNewsIds());
                    stats.get(e.getKey()).get(clusteringName).add(distance);
                }

            }

        });

        em.close();

        return stats;

    }



}
