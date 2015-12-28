package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.fpclustering.FrontPagesClustering;
import it.fcambi.news.fpclustering.FrontPagesTimestampGroup;
import it.fcambi.news.model.Clustering;
import it.fcambi.news.model.FrontPage;
import it.fcambi.news.relevance.NewsRelevance;
import it.fcambi.news.relevance.OrderBasedRelevance;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Francesco on 28/12/15.
 */
public class NewsRelevancesManager {
    protected static ConcurrentMap<String, List<NewsRelevance>> cache = new ConcurrentHashMap<>();

    protected List<NewsRelevance> retrieveOrGenerateRelevances(String clusteringName) throws IllegalArgumentException {
        List<NewsRelevance> relevances;
        if (!cache.containsKey(clusteringName)) {

            EntityManager em = Application.createEntityManager();
            Clustering clustering;
            if (clusteringName != null && clusteringName.length() > 0) {
                clustering = em.find(Clustering.class, clusteringName);
                if (clustering == null)
                    throw new IllegalArgumentException("No clustering found for name specified.");
            } else
                throw new IllegalArgumentException("You must provide a clustering to identify news.");

            relevances = getRelevances(em, clustering, null, null);

            cache.put(clusteringName, relevances);

            em.close();
        } else {
            relevances = cache.get(clusteringName);
        }
        return relevances;
    }

    protected List<NewsRelevance> getRelevances(EntityManager em, Clustering clustering, Date from, Date to) {
        String query = "select fp from FrontPage fp where 1=1";
        if (from != null)
            query += " and fp.timestamp >= :fromts";
        if (to != null)
            query += " and fp.timestamp <= :tots";
        TypedQuery<FrontPage> frontPagesQuery = em.createQuery(query, FrontPage.class);
        if (from != null)
            frontPagesQuery.setParameter("fromts", from);
        if (to != null)
            frontPagesQuery.setParameter("tots", to);
        List<FrontPage> frontPages = frontPagesQuery.getResultList();


        FrontPagesClustering pageClustering = new FrontPagesClustering();
        List<FrontPagesTimestampGroup> pagesByTime = pageClustering.groupFrontPagesByTimestamp(frontPages);

        OrderBasedRelevance relevanceCalculator = new OrderBasedRelevance(clustering);
        relevanceCalculator.setFrontPagesGroups(pagesByTime);
        relevanceCalculator.computeRelevances();

        return relevanceCalculator.getRelevances();
    }
}
