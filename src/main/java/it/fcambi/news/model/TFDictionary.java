package it.fcambi.news.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.fcambi.news.data.FrequenciesWordVector;

import javax.persistence.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Francesco on 27/10/15.
 */
@Entity
@Table(name = "tfdictionary")
public class TFDictionary {

    @Id
    @Column(length = 40)
    protected String description;

    protected long numOfDocuments = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(length = 60)
    @CollectionTable(name = "tfdictionary_terms")
    @JsonIgnore
    protected Map<String, Integer> terms = new ConcurrentHashMap<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getNumOfDocuments() {
        return numOfDocuments;
    }

    public void setNumOfDocuments(long numOfDocuments) {
        this.numOfDocuments = numOfDocuments;
    }

    public Map<String, Integer> getTerms() {
        return terms;
    }

    public long getNumOfDocumentsWithWord(String word) {
        return terms.getOrDefault(word, 1);
    }

    public void addDocument(FrequenciesWordVector d) {
        // Update dictionary with words in d
        d.getWords().forEach(word -> terms.put(word, terms.getOrDefault(word, 0)+1));
        numOfDocuments++;
    }

    public void enableParallelism() {
        if (!(terms instanceof ConcurrentHashMap))
            terms = new ConcurrentHashMap<>(terms);
    }
}
