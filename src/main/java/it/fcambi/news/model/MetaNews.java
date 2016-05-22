package it.fcambi.news.model;

import it.fcambi.news.Pair;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 23/02/16.
 */
@Entity
@Table(name = "metanews")
public class MetaNews implements Cluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;

    @ManyToOne
    protected Clustering clustering;

//    @OneToOne(cascade = {CascadeType.ALL})
    @Transient
    protected Centroid centroid;

    @OneToMany(mappedBy = "metanews", cascade = {CascadeType.ALL})
    protected List<News> news = new ArrayList<>();

    private boolean listUpdated;

    public MetaNews() {
    }

    public MetaNews(Clustering clustering) {
        this.clustering = clustering;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<News> getNews() {
        return news;
    }

    public void setNews(List<News> news) {
        this.listUpdated = true;
        this.news = news;
    }

    public void addNews(News n) {
        this.listUpdated = true;
        this.news.add(n);
    }

    @Override
    public Centroid getCentroid(Clustering clustering) {
        if (this.listUpdated) {
            this.listUpdated = false;
            // ReCompute centroid
            // Words union
            Set<String> words = this.news.parallelStream()
                    .flatMap(n -> n.getCentroid(this.clustering).getWords().stream())
                    .collect(Collectors.toSet());
            if (this.centroid == null)
                this.centroid = new Centroid();
            // Compute average weight for each word and build centroid values map
            this.centroid.setComponents(words.stream().map(word -> new Pair<String, Double>(word, this.news.stream()
                    .mapToDouble(n -> n.getCentroid(this.clustering).getValue(word))
                    .summaryStatistics().getAverage())
            ).collect(Collectors.toConcurrentMap(Pair::getKey, Pair::getValue)));
        }
        return this.centroid;
    }

    @Override
    public boolean hasChild() {
        return true;
    }

    @Override
    public List<Cluster> getChildren() {
        return this.news.stream().map(n -> (Cluster) n).collect(Collectors.toList());
    }

    @Override
    public Cluster getParent(Clustering clustering) {
        return null;
    }
}
