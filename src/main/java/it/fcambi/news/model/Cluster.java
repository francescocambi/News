package it.fcambi.news.model;

import java.util.List;

/**
 * Created by Francesco on 22/02/16.
 */
public interface Cluster {

    Centroid getCentroid(Clustering clustering);

    boolean hasChild();

    List<Cluster> getChildren();

    Cluster getParent(Clustering clustering);

}
