//package it.fcambi.news.matchers;
//
//import it.fcambi.news.data.OldWordVector;
//import it.fcambi.news.filters.StandardizeStringFilter;
//import it.fcambi.news.filters.VectorFilter;
//import it.fcambi.news.filters.NoiseWordsVectorFilter;
//import it.fcambi.news.metrics.Metric;
//import it.fcambi.news.model.Article;
//import it.fcambi.news.ws.resources.dto.MatchingArticleDTO;
//
//import java.util.LinkedList;
//import java.util.List;
//
///**
// * Created by Francesco on 30/09/15.
// */
//public class NewsMatcher {
//
//    private Article source;
//    private OldWordVector sourceOldWordVector;
//    private int[] sourceFrequencies;
//    private List<Metric> metrics;
//
//    public NewsMatcher() {
//        metrics = new LinkedList<>();
//    }
//
//    public void addMetric(Metric m) {
//        this.metrics.add(m);
//    }
//
//    public void removeMetric(Metric m) {
//        this.metrics.remove(m);
//    }
//
//    public List<Metric> getMetrics() {
//        return metrics;
//    }
//
//    public void setSourceArticle(Article a) {
//        this.source = a;
//
//        //Clean source article text and prepare word vector
//        sourceOldWordVector = new OldWordVector();
//        sourceOldWordVector.addStringFilter(new StandardizeStringFilter());
//        sourceOldWordVector.setSourceText(source.getTitle() + " " + source.getDescription(), "[ ]+");
//        VectorFilter noiseWordFilter = new NoiseWordsVectorFilter();
//        noiseWordFilter.filter(sourceOldWordVector);
//        sourceFrequencies = sourceOldWordVector.getWordsFrequencyIn(source.getBody(), "[ ]+");
//    }
//
//    public List<MatchingArticleDTO> match(List<Article> articles) {
//
//        List<MatchingArticleDTO> result = new LinkedList<>();
//        //Iterates over other articles
//        for (Article a: articles) {
//            int[] matchingFrequencies = sourceOldWordVector.getWordsFrequencyIn(a.getBody(), "[ ]+");
//            //Compute similarity and prepare map
//            MatchingArticleDTO item = new MatchingArticleDTO();
//            item.setArticle(a);
//            this.metrics.forEach((metric) -> {
//                item.putSimilarity(metric.getName(), metric.compute(sourceFrequencies, matchingFrequencies));
//            });
//            result.add(item);
//        }
//
//        return result;
//    }
//
//    public MatchingArticleDTO match(Article a) {
//
//        int[] matchingFrequencies = sourceOldWordVector.getWordsFrequencyIn(a.getBody(), "[ ]+");
//        MatchingArticleDTO matching = new MatchingArticleDTO();
//        matching.setArticle(a);
//        this.metrics.forEach(metric -> matching.putSimilarity(
//                metric.getName(),
//                metric.compute(sourceFrequencies, matchingFrequencies)
//        ));
//
//        return matching;
//    }
//
//
//}
