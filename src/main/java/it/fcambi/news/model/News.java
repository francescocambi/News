package it.fcambi.news.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Francesco on 03/11/15.
 */
@Entity
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;

    @Column(columnDefinition = "TEXT")
    protected String description;

    @ManyToOne
    @JsonManagedReference
    @NotNull
    protected Clustering clustering;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "news")
    @JsonBackReference
    protected List<Article> articles = new ArrayList<>();

    public static News createForArticle(Article a, Clustering clustering) {
        News n = new News(clustering);
        n.description = a.getTitle();
        n.addArticle(a);
        return n;
    }

    protected News() {};

    public News(Clustering clustering) {
        this.clustering = clustering;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public void addArticle(Article a) {
        this.articles.add(a);
    }

    public boolean equals(News n) {
        return n.getId() == this.id;
    }

    @JsonIgnore
    public Clustering getClustering() {
        return clustering;
    }

    @JsonProperty
    public int size() {
        return this.getArticles().size();
    }

    @JsonProperty
    public String clusteringName() {
        return this.clustering.getName();
    }

}
