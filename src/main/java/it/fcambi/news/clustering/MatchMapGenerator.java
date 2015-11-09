package it.fcambi.news.clustering;

import it.fcambi.news.model.MatchingArticle;
import it.fcambi.news.data.Text;
import it.fcambi.news.data.WordVector;
import it.fcambi.news.model.Article;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MatchMapGenerator {

    private Map<Article, Text> headlineCache = new Hashtable<>();
    private Map<Article, Text> bodyCache = new Hashtable<>();

    private MatchMapGeneratorConfiguration config;

    public MatchMapGenerator(MatchMapGeneratorConfiguration config) {
        this.config = config;
    }

    /**
     * @param articlesToMatch          Set of articles to match
     * @param knownArticles           Set of previously clustered articles
     * @return Map that bind each article with a list of possible matchings
     */
    public Map<Article, List<MatchingArticle>> generateMap(Collection<Article> articlesToMatch, Collection<Article> knownArticles) {

        // Source Article -> Similarities with all articles
        Map<Article, List<MatchingArticle>> matchMap = new Hashtable<Article, List<MatchingArticle>>(articlesToMatch.size());

        articlesToMatch.parallelStream().forEach(article -> {

            // Prepare or retrieve article
            if (!bodyCache.containsKey(article)) {
                bodyCache.put(article, getTextAndApplyFilters(article.getBody()));
            }
            if (!headlineCache.containsKey(article)) {
                String keywords = config.getKeywordSelectionFn().apply(article, bodyCache.get(article));
                headlineCache.put(article, getTextAndApplyFilters(keywords));
            }

            List<MatchingArticle> matchingArticles = knownArticles.parallelStream()
                    .filter(match -> !config.getIgnorePairPredicate().test(article, match))
                    .map(match -> {

                //Prepare or retrieve matching article
                if (!bodyCache.containsKey(match))
                    bodyCache.put(match, getTextAndApplyFilters(match.getBody()));
                if (!headlineCache.containsKey(match)) {
                    String keywords = config.getKeywordSelectionFn().apply(match, bodyCache.get(match));
                    headlineCache.put(match, getTextAndApplyFilters(keywords));
                }

                WordVector w = config.getWordVectorFactory().createNewVector();
                w.setWordsFrom(headlineCache.get(article), headlineCache.get(match));
                w.setValuesFrom(bodyCache.get(article));

                WordVector v = config.getWordVectorFactory().createNewVector();
                v.setWords(w.getWords());
                v.setValuesFrom(bodyCache.get(match));

                MatchingArticle a = new MatchingArticle();
                a.setArticle(match);

                config.getMetrics().forEach(metric ->
                        a.addSimilarity(metric.getName(), metric.compute(w.toArray(), v.toArray())));

                return a;
            }).collect(Collectors.toList());

            matchMap.put(article, matchingArticles);

        });

        return matchMap;
    }

    private Text getTextAndApplyFilters(String s) {
        Text t = config.getStringToTextFn().apply(s);
        config.getTextFilters().forEach(t::applyFilter);
        return t;
    }
}