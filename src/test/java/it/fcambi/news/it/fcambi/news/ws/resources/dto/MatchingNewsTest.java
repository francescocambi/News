package it.fcambi.news.it.fcambi.news.ws.resources.dto;

import it.fcambi.news.ws.resources.dto.MatchingArticleDTO;
import it.fcambi.news.ws.resources.dto.MatchingNews;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Francesco on 19/10/15.
 */
public class MatchingNewsTest {

    @Test
    public void testMeanSimilarities() {

        MatchingNews news = new MatchingNews();

        MatchingArticleDTO art = new MatchingArticleDTO();
        art.putSimilarity("cosine", 0.8);
        art.putSimilarity("jaccard", 0.4);
        news.addMatchingArticle(art);

        art = new MatchingArticleDTO();
        art.putSimilarity("cosine", 0.7);
        art.putSimilarity("jaccard", 0);
        news.addMatchingArticle(art);

        art = new MatchingArticleDTO();
        art.putSimilarity("cosine", 0.4);
        art.putSimilarity("jaccard", 1);
        news.addMatchingArticle(art);

        double cosineMean = (0.8+0.7+0.4)/3;
        double jaccMean = (0.4+0+1)/3;

        Map<String, Double> means = news.getMeanSimilarities();
        assertTrue("Means contains key cosine", means.containsKey("cosine"));
        assertTrue("Means contains key jaccard", means.containsKey("jaccard"));

        assertEquals("Means contains right value for cosine", cosineMean, means.get("cosine"), 0.00000001);
        assertEquals("Means contains right value for jaccard", jaccMean, means.get("jaccard"), 0.00000001);

    }

}
