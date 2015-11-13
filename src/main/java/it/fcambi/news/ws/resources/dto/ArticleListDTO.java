package it.fcambi.news.ws.resources.dto;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.News;
import it.fcambi.news.model.Newspaper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Francesco on 13/11/15.
 */
public class ArticleListDTO {

    protected long id;
    protected String title;
    protected Newspaper source;
    protected Date created;
    protected Map<String, Long> news = new HashMap<>();

    public static ArticleListDTO createFrom(Article a) {
        ArticleListDTO o = new ArticleListDTO();
        o.id = a.getId();
        o.title = a.getTitle();
        o.source = a.getSource();
        o.created = a.getCreated();
        a.getNewsMap().entrySet().stream().forEach(e -> o.news.put(e.getKey(), e.getValue().getId()));
        return o;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Newspaper getSource() {
        return source;
    }

    public Date getCreated() {
        return created;
    }

    public Map<String, Long> getNews() {
        return news;
    }
}
