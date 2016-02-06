package it.fcambi.news.model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Francesco on 06/02/16.
 */
@Entity
@Table(name = "noise_words_list")
public class NoiseWordsList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;

    protected String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "noise_words_list_words")
    protected List<String> words;

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }
}
