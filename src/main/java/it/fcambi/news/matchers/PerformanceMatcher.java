package it.fcambi.news.matchers;

import it.fcambi.news.data.WordVector;
import it.fcambi.news.filters.NoiseWordsVectorFilter;
import it.fcambi.news.filters.StandardizeStringFilter;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.Article;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by Francesco on 16/10/15.
 */
public class PerformanceMatcher {

    // Array of n articles
    Article[] index;

    // nxn Matrix representing distances from each pair of articles
    double[][][] distances;

    public void setArticlesSet(Collection<Article> c) {

        index = c.toArray(new Article[c.size()]);

    }

    public void computeDistances(List<Metric> metrics) {

        // Compute distances for each pair of articles

        distances = new double[index.length][index.length][metrics.size()];

        NoiseWordsVectorFilter noiseFilter = new NoiseWordsVectorFilter();

        for (int i=0; i < index.length; i++) {
            final int idx = i;

            // Test title+description of i within the body of j
            WordVector w = new WordVector();
            w.addStringFilter(new StandardizeStringFilter());
            w.setSourceText(index[i].getTitle()+" "+index[i].getDescription(), "[ ]+");
            noiseFilter.filter(w);

            int[] sourceFrequencies = w.getWordsFrequencyIn(index[i].getBody(), "[ ]+");

            IntStream.range(0, index.length).parallel().forEach(j -> {
                int[] matchingFrequencies = w.getWordsFrequencyIn(index[j].getBody(), "[ ]+");

                for (int k=0; k < metrics.size(); k++) {
                    distances[idx][j][k] = metrics.get(k).compute(sourceFrequencies, matchingFrequencies);
                }
            });
        }

    }

    public double[][][] getDistances() {
        return distances;
    }

    public Article[] getIndex() {
        return index;
    }
}
