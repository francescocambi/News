package it.fcambi.news.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Francesco on 03/11/15.
 */
@Entity
public class PredictedNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "predictedNews")
    @JsonBackReference
    private List<Article> articles;

    public long getId() {
        return id;
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

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof News
                && ((News) obj).getId() == this.id);
    }

}
