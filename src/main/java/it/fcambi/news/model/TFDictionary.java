package it.fcambi.news.model;

import it.fcambi.news.data.FrequenciesWordVector;

import javax.persistence.*;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Francesco on 27/10/15.
 */
@Entity
public class TFDictionary {

    @Id
    @Column(length = 40)
    protected String description;

    protected long numOfDocuments = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(length = 60)
    protected Map<String, Long> terms = new Hashtable<>();

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

    public Map<String, Long> getTerms() {
        return terms;
    }

    public synchronized long getNumOfDocumentsWithWord(String word) {
        return terms.getOrDefault(word, 1L);
    }

    public void addDocument(FrequenciesWordVector d) {
        // Update dictionary with words in d
        d.getWords().forEach(word -> terms.put(word, terms.getOrDefault(word, 0L)+1L));
        numOfDocuments++;
    }
}
