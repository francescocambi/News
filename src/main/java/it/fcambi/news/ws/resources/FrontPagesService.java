package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.data.NewsVector;
import it.fcambi.news.fpclustering.FrontPagesClustering;
import it.fcambi.news.fpclustering.FrontPagesTimestampGroup;
import it.fcambi.news.fpclustering.NewspapersDistance;
import it.fcambi.news.mds.MultidimensionalScaling;
import it.fcambi.news.metrics.permutations.BasicDistance;
import it.fcambi.news.metrics.permutations.KendallTau;
import it.fcambi.news.metrics.permutations.PermutationsMetric;
import it.fcambi.news.model.Clustering;
import it.fcambi.news.model.FrontPage;
import it.fcambi.news.model.Newspaper;
import it.fcambi.news.ws.resources.dto.NewspapersPoints;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        List<FrontPage> pages = em.createQuery("select p from FrontPage p join p.articles a " +
                "where key(a.news)='manual'", FrontPage.class)
                .getResultList();

        Clustering manual = em.find(Clustering.class, "manual");

        //Separates front pages for each newspaper
        Map<Newspaper, List<FrontPage>> pagesByNewspaper = pages.stream()
                .collect(Collectors.groupingBy(FrontPage::getNewspaper));

        Map<Newspaper, Double> changesByNewspaper = new HashMap<>();
        //Work on front pages lists
        pagesByNewspaper.entrySet().forEach(entry -> {
            List<FrontPage> ps = entry.getValue();
            //Sort by timestamp
            ps.sort(Comparator.comparing(FrontPage::getTimestamp));

            //Count changes between each scan
            List<NewsVector> newsVectors = ps.stream().map(fp -> new NewsVector(fp, manual))
                    .collect(Collectors.toList());
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
    @Path("/variability")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFrontPagesVariability(@QueryParam("clusteringA") String clusteringAName,
                                             @QueryParam("clusteringB") String clusteringBName,
                                             @QueryParam("pagesFrom") String pagesFromString,
                                             @QueryParam("pagesTo") String pagesToString) {

        EntityManager em = Application.createEntityManager();
        Clustering[] clusterings = new Clustering[2];
        Date pagesFrom = null, pagesTo = null;

        //PARAMETERS VALIDATION
        try {

            if (clusteringAName == null) {
                throw new IllegalArgumentException("Must provide at least one clustering.");
            } else {
                clusterings[0] = em.find(Clustering.class, clusteringAName);
                if (clusterings[0] == null)
                    throw new IllegalArgumentException("Clustering A is not valid.");
            }

            if (clusteringBName != null) {
                clusterings[1] = em.find(Clustering.class, clusteringBName);
                if (clusterings[1] == null) {
                    throw new IllegalArgumentException("Clustering B is not valid.");
                }
            }

            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");

            if (pagesFromString != null)
                pagesFrom = format.parse(pagesFromString);
            if (pagesToString != null)
                pagesTo = format.parse(pagesToString);
            if (pagesFrom != null && pagesTo != null && !pagesFrom.before(pagesTo))
                throw new IllegalArgumentException("From date cannot be after to date.");

        } catch (IllegalArgumentException e) {
            return Response.status(400).entity("\""+e.getMessage()+"\"").build();
        } catch (ParseException e) {
            return Response.status(400).entity("\"Date string not valid.\"").build();
        }

        Map<Newspaper, Map<String, List>> stats = new HashMap<>();

        //Compose query
        String query = "select p from FrontPage p where p.articles.size > 0 " +
                "and (select count(a) from p.articles a where key(a.news) = :clusteringA";
        if (clusterings[1] != null)
            query += " and key(a.news) = :clusteringB";
        query += ") = p.articles.size";
        if (pagesFrom != null)
            query += " and p.timestamp >= :pagesFrom";
        if (pagesTo != null)
            query += " and p.timestamp <= :pagesTo";
        query += " order by p.timestamp";

        TypedQuery<FrontPage> pagesQuery = em.createQuery(query, FrontPage.class)
                .setParameter("clusteringA", clusterings[0].getName());
        if (clusterings[1] != null)
            pagesQuery.setParameter("clusteringB", clusterings[1].getName());
        if (pagesFrom != null)
            pagesQuery.setParameter("pagesFrom", new Calendar.Builder().setInstant(pagesFrom).build());
        if (pagesTo != null)
            pagesQuery.setParameter("pagesTo", new Calendar.Builder().setInstant(pagesTo).build());

        List<FrontPage> pages = pagesQuery.getResultList();

        // Create Newspaper - FrontPage map
        Map<Newspaper, List<FrontPage>> pagesByNewspaper = pages.stream()
                .collect(Collectors.groupingBy(FrontPage::getNewspaper));

        // Iterates over frontpages list of each newspaper
        pagesByNewspaper.entrySet().stream().forEach(e -> {
            stats.put(e.getKey(), new HashMap<>());
            stats.get(e.getKey()).put("dates", new Vector<>());

            List<FrontPage> l = e.getValue();

            for (int i = 1; i<l.size(); i++ ) {
                stats.get(e.getKey()).get("dates").add(l.get(i).getTimestamp());
            }

            for (Clustering clustering : clusterings) {
                if (clustering != null) {
                    stats.get(e.getKey()).put(clustering.getName(), new Vector<>());

                    //Compute distance between pages at each timestamp gap
                    PermutationsMetric d = new KendallTau();
                    for (int i = 1; i < l.size(); i++) {
                        double distance = d.compute(
                                new NewsVector(l.get(i - 1), clustering).getNewsIds(),
                                new NewsVector(l.get(i), clustering).getNewsIds());
                        stats.get(e.getKey()).get(clustering.getName()).add(distance);
                    }
                }
            }

        });

        em.close();

        //Map<Newspaper, Map<String, List>>
        return Response.status(200).entity(stats).build();

    }

    @GET
    @Path("/newspapers-distance")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewspapersDistance(@QueryParam("clustering") String clusteringName,
                                          @QueryParam("pagesFrom") String pagesFromTimestamp,
                                          @QueryParam("pagesTo") String pagesToTimestamp,
                                          @QueryParam("timeStep") int timeStep,
                                          @QueryParam("timeStepUm") String timeStepUm) {

        EntityManager em = Application.createEntityManager();

        // PARAMETERS VALIDATION
        Clustering clustering;
        Date from = null, to = null;
        int timeUm = Calendar.YEAR;
        try {
            if (clusteringName == null) {
                throw new IllegalArgumentException("Must provide clustering.");
            }

            clustering = em.find(Clustering.class, clusteringName);
            if (clustering == null) {
                throw new IllegalArgumentException("Cannot find clustering.");
            }

            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
            if (pagesFromTimestamp != null) {
                from = format.parse(pagesFromTimestamp);
            }
            if (pagesToTimestamp != null) {
                to = format.parse(pagesToTimestamp);
            }
            if (from != null && to != null && from.after(to))
                throw new IllegalArgumentException("Invalid time range.");

            if (timeStepUm != null) {
                if (timeStepUm.equals("y"))
                    timeUm = Calendar.YEAR;
                else if (timeStepUm.equals("m"))
                    timeUm = Calendar.MONTH;
                else if (timeStepUm.equals("d"))
                    timeUm = Calendar.DAY_OF_MONTH;
                else if (timeStepUm.equals("h"))
                    timeUm = Calendar.HOUR_OF_DAY;
            }

            if (timeStep <= 0)
                throw new IllegalArgumentException("Invalid time step value.");

        } catch (IllegalArgumentException e) {
            return Response.status(400).entity("\""+e.getMessage()+"\"").build();
        } catch (ParseException e) {
            return Response.status(400).entity("\"Invalid timestamp format\"").build();
        }

        String query = "select p from FrontPage p where p.articles.size > 0 " +
                "and (select count(a) from p.articles a where key(a.news) = :clustering) = p.articles.size";
        if (from != null)
            query += " and p.timestamp >= :fromts";
        if (to != null)
            query += " and p.timestamp <= :tots";
        query += " order by p.timestamp";

        TypedQuery<FrontPage> pagesQuery = em.createQuery(query, FrontPage.class)
                .setParameter("clustering", clustering.getName());
        if (from != null)
            pagesQuery.setParameter("fromts", new Calendar.Builder().setInstant(from).build());
        if (to != null)
            pagesQuery.setParameter("tots", new Calendar.Builder().setInstant(to).build());

        List<FrontPage> pages = pagesQuery.getResultList();

        if (pages.size() == 0) {
            return Response.status(400).entity("\"Empty dataset\"").build();
        }

        em.close();

        FrontPagesClustering fpc = new FrontPagesClustering();
        List<FrontPagesTimestampGroup> groups = fpc.groupFrontPagesByTimestamp(pages);

        KendallTau tau = new KendallTau(10);

        List<NewspapersDistance> distancesByTimestamp = new Vector<>();

        //Generates distances matrix between newspapers for each time range
        groups.stream().filter(g -> g.getFrontPages().size() == 6).forEachOrdered(group -> {

            group.getFrontPages().sort((a, b) -> a.getNewspaper().compareTo(b.getNewspaper()));

            double[][] distances = new double[group.getFrontPages().size()][group.getFrontPages().size()];

            for (int i = 0; i < distances.length; i++)
                for (int j = 0; j < distances[i].length; j++) {

                    NewsVector x = new NewsVector(group.getFrontPage(i), clustering);
                    NewsVector y = new NewsVector(group.getFrontPage(j), clustering);

                    if (x.getNewsIds().length <= 0 || y.getNewsIds().length <= 0) {
                        throw new IllegalArgumentException();
                    }

                    distances[i][j] = tau.compute(
                            new NewsVector(group.getFrontPage(i), clustering).getNewsIds(),
                            new NewsVector(group.getFrontPage(j), clustering).getNewsIds()
                    );
                }

            distancesByTimestamp.add(new NewspapersDistance(distances, group.getTimestamp()));
        });

        //Aggregate distances by a certain time step
        List<NewspapersDistance> distancesByTimeStep = new Vector<>();

        for (int i = 0; i < distancesByTimestamp.size(); i++) {
            // Trim start time to the begin of specified time step
            // i.e. groups[0].timestamp = 09:04:31 => startTime = 09:00:00
            Calendar start = new Calendar.Builder()
                    .setInstant(
                            distancesByTimestamp.get(i).getTimestamp().getTimeInMillis()
                    ).build();
            switch (timeUm) {
                case Calendar.YEAR:
                    start.set(Calendar.MONTH, 0);
                case Calendar.MONTH:
                    start.set(Calendar.DAY_OF_MONTH, 1);
                case Calendar.DAY_OF_MONTH:
                    start.set(Calendar.HOUR_OF_DAY, 0);
                case Calendar.HOUR_OF_DAY:
                    start.set(Calendar.MINUTE, 0);
                    start.set(Calendar.SECOND, 0);
                    start.set(Calendar.MILLISECOND, 0);
            }
            Calendar end = new Calendar.Builder().setInstant(start.getTimeInMillis()).build();
            end.add(timeUm, timeStep);

            // Sum and count elements of this group
            double[][] sum = new double[6][6];
            int count = 0;
            for (int j = i; j < distancesByTimestamp.size(); j++) {
                if (distancesByTimestamp.get(j).getTimestamp().before(end)) {
                    matrixSum(sum, distancesByTimestamp.get(j).getDistances());
                    count++;
                } else {
                    j = distancesByTimestamp.size();
                }
            }
            // count is the # of items processed by previous cycle
            // i + count goes on the 1st element of new group
            // Set i on last element of current group i+(count-1)
            // so next iteration (of outer cycle) will begin
            // on the 1st element of the new group
            i += count-1;

            //When for exits j is on the first item of next group
            // can save this group
            Calendar timestamp = new Calendar.Builder().setInstant(start.getTimeInMillis()).build();
            matrixDivision(sum, count);
            distancesByTimeStep.add(new NewspapersDistance(sum, timestamp));
        }

        //Apply Multidimensional scaling
        Map<Date, Object> pointsByTime = new LinkedHashMap<>();
        distancesByTimeStep.stream().forEach(group -> {

            double[][] points = MultidimensionalScaling.exec(group.getDistances());

            pointsByTime.put(group.getTimestamp().getTime(),
                    new NewspapersPoints(points, Newspaper.values()));

        });

        return Response.status(200).entity(pointsByTime).build();


    }

    private static void matrixSum(double[][] sum, double[][] x) {
        for (int i = 0; i < sum.length; i++) {
            for (int j = 0; j < sum[i].length; j++) {
                sum[i][j] += x[i][j];
            }
        }
    }

    private static void matrixDivision(double[][] a, double x) {
        for (int i=0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                a[i][j] /= x;
            }
        }
    }



}
