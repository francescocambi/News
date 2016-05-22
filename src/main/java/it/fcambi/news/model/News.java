package it.fcambi.news.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.fcambi.news.Pair;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 03/11/15.
 */
@Entity
@Table(name = "news")
public class News implements Cluster {

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

//    @OneToOne(mappedBy = "news", fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @Transient
    protected Centroid centroid;

    @ManyToOne
    @JsonIgnore
    protected MetaNews metanews;

    private boolean listUpdated = false;

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
        this.listUpdated = true;
    }

    public void addArticle(Article a) {
        this.articles.add(a);
        this.listUpdated = true;
    }

    @Override
    public Centroid getCentroid(Clustering clustering) {
        if (this.listUpdated) {
            this.listUpdated = false;
            // ReCompute centroid
            // Words union
            Set<String> words = this.articles.parallelStream()
                    .flatMap(a -> {
                        Centroid c = a.getCentroid(this.clustering);
                        if (c == null) {
                            System.err.println(a.getCentroidsMap().toString());
                            throw new IllegalStateException("Article "+a.getId()+" has null centroid.");
                        } else {
                            return c.getWords().stream();
                        }
                    }).collect(Collectors.toSet());
            if (this.centroid == null)
                this.centroid = new Centroid();
            // Compute average weight for each word and build centroid values map
            this.centroid.setComponents(words.stream().map(word -> new Pair<String, Double>(word, this.articles.stream()
                        .mapToDouble(a -> a.getCentroid(this.clustering).getValue(word))
                        .summaryStatistics().getAverage())
            ).collect(Collectors.toConcurrentMap(Pair::getKey, Pair::getValue)));
        }
        return this.centroid;
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

    public MetaNews getMetanews() {
        return metanews;
    }

    public void setMetanews(MetaNews metanews) {
        this.metanews = metanews;
    }

    @Override
    public boolean hasChild() {
        return true;
    }

    @Override
    @JsonIgnore
    public List<Cluster> getChildren() {
        return this.articles.stream().map(a -> (Cluster) a).collect(Collectors.toList());
    }

    @Override
    public Cluster getParent(Clustering clustering) {
        return (Cluster) this.metanews;
    }
}
