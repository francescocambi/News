package it.fcambi.news.model;

import javax.persistence.*;

/**
 * Created by Francesco on 13/10/15.
 */
@Entity
public class ArticleHtmlSource {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(columnDefinition = "TEXT")
    private String html;

    public long getId() {
        return id;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
