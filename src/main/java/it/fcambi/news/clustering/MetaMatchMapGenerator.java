package it.fcambi.news.clustering;

import it.fcambi.news.data.Text;
import it.fcambi.news.data.WordVector;
import it.fcambi.news.model.Cluster;
import it.fcambi.news.model.Clustering;
import it.fcambi.news.model.MatchingCluster;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 22/02/16.
 */
public class MetaMatchMapGenerator extends MatchMapGenerator {

    protected Clustering clustering;

    public MetaMatchMapGenerator(Clustering clustering, MatchMapGeneratorConfiguration config) {
        super(config);
        this.clustering = clustering;
    }

    public MetaMatchMapGenerator(Clustering clustering, MatchMapGeneratorConfiguration config, Map<Long, Text> bodyCache, Map<Long, Text> keywordCache) {
        super(config, bodyCache, keywordCache);
        this.clustering = clustering;
    }

    public List<MatchingCluster> generateClustersMatchMap(Cluster cluster, Collection<Cluster> knownClusters) {

        List<MatchingCluster> matchingClusters = knownClusters.parallelStream()
                .filter(match -> !config.getIgnorePairPredicate().test(cluster, match))
                .map(match -> {

                    WordVector w = config.getWordVectorFactory().createNewVector();
                    w.setWords(
                            cluster.getCentroid(this.clustering).wordsUnion(match.getCentroid(this.clustering))
                    );
                    w.setValues(
                            cluster.getCentroid(this.clustering).getValuesFor(w.getWords())
                    );

                    WordVector v = config.getWordVectorFactory().createNewVector();
                    v.setWords(w.getWords());
                    v.setValues(
                            match.getCentroid(this.clustering).getValuesFor(w.getWords())
                    );

                    MatchingCluster a = new MatchingCluster();
                    a.setCluster(match);

                    config.getMetrics().forEach(metric ->
                            a.addSimilarity(metric.getName(), metric.compute(w.toArray(), v.toArray())));

                    return a;
                }).collect(Collectors.toList());

        progress.incrementAndGet();

        return matchingClusters;
    }
}
