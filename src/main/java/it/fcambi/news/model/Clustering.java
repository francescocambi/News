package it.fcambi.news.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Francesco on 06/11/15.
 */
@Entity
public class Clustering {

    @Id
    @Column(length = 100)
    protected String name;
    protected String description;

    @OneToMany(mappedBy = "clustering", cascade = {CascadeType.REMOVE})
    @JsonBackReference
    protected List<News> clusters = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    protected Date created;

    public Clustering() {
        this.created = new Date();
    }

    public Clustering(String name) {
        this.name = name;
        this.created = new Date();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreated() {
        return created;
    }

    public List<News> getClusters() {
        return clusters;
    }
}
