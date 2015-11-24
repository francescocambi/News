package it.fcambi.news.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Francesco on 12/10/15.
 */
@Entity
public class FrontPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar timestamp;

    private Newspaper newspaper;

    @ManyToMany(fetch=FetchType.EAGER)
    @OrderColumn
    @JsonManagedReference
    private List<Article> articles;

    public FrontPage() {
        this.timestamp = Calendar.getInstance();
    }

    public long getId() {
        return id;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public Newspaper getNewspaper() {
        return newspaper;
    }

    public void setNewspaper(Newspaper newspaper) {
        this.newspaper = newspaper;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public int orderOf(Article a) {
        return this.articles.indexOf(a);
    }

    public boolean equals(FrontPage fp) {
        return this.id == fp.getId();
    }
}
