package it.fcambi.news.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 22/02/16.
 */
//@Entity
//@Table(name = "centroid")
public class Centroid {

    public static ArticleCentroidBuilder builder() {
        return new ArticleCentroidBuilder();
    }

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;

//    @OneToOne
    protected News news;

//    @ElementCollection
//    @MapKeyColumn(length = 100)
    protected Map<String, Double> components = new ConcurrentHashMap<>();

    public long getId() {
        return id;
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public Map<String, Double> getComponents() {
        return components;
    }

    public void setComponents(Map<String, Double> components) {
        this.components = components;
    }

    public void addValue(String word, double value) {
        this.components.put(word, value);
    }

    public double getValue(String word) {
        return this.components.getOrDefault(word, 0.0);
    }

    public Set<String> getWords() {
        return this.components.keySet();
    }

    public List<String> wordsUnion(Centroid... centroids) {
        return Arrays.asList(centroids).stream()
                .flatMap(c -> c.getWords().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Double> getValuesFor(List<String> words) {
        return words.stream().map(w -> this.components.getOrDefault(w, 0.0))
                .collect(Collectors.toList());
    }
}