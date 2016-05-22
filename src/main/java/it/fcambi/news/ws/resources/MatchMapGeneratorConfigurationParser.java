package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.metrics.*;
import it.fcambi.news.model.Language;
import it.fcambi.news.model.TFDictionary;

import javax.persistence.EntityManager;

/**
 * Created by Francesco on 11/11/15.
 */
public class MatchMapGeneratorConfigurationParser {

    protected MatchMapGeneratorConfiguration config;
    protected Metric metric;

    public MatchMapGeneratorConfiguration getConfig() {
        return config;
    }

    public Metric getMetric() {
        return metric;
    }

    public void parse(String metricName, boolean noiseWordsFilter, boolean stemming, boolean tfidf, String tfDictionary,
                      String languageString, String keywordExtraction) throws IllegalArgumentException {

        config = new MatchMapGeneratorConfiguration();

        Language language = Language.valueOf(languageString);
        if (noiseWordsFilter) config.addTextFilter(new NoiseWordsTextFilter(language));
        if (stemming) config.addTextFilter(new StemmerTextFilter(language));
        if (tfidf) {
            EntityManager em = Application.createEntityManager();
            TFDictionary dict = em.find(TFDictionary.class, tfDictionary);
            if (dict == null) {
                throw new IllegalArgumentException("TF dictionary does not exists.");
            }
            config.setWordVectorFactory(new TFIDFWordVectorFactory(dict));
            em.close();
        }

        switch (metricName) {
            case "cosine":
                metric = new CosineSimilarity();
                break;
            case "jaccard":
                metric = new JaccardSimilarity();
                break;
            case "combined":
                metric = new MyMetric();
                break;
            case "tanimoto":
                metric = new TanimotoSimilarity();
                break;
            default:
                throw new IllegalArgumentException("Can't parse metricName");
        }
        config.addMetric(metric);

        switch (keywordExtraction) {
            case "headline":
                config.setKeywordSelectionFunction(MatchMapGeneratorConfiguration.headlineKeywords);
                break;
            case "capitals":
                config.setKeywordSelectionFunction(MatchMapGeneratorConfiguration.headlineAndCapitalsKeywords);
                break;
            default:
                throw new IllegalArgumentException("Can't parse keyword extraction method name");
        }


    }

}
