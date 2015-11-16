package it.fcambi.news.clustering;

import it.fcambi.news.data.Text;
import it.fcambi.news.data.WordVector;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.MatchingArticle;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MatchMapGenerator {

    private Map<Article, Text> headlineCache = new ConcurrentHashMap<>();
    private Map<Article, Text> bodyCache = new ConcurrentHashMap<>();

    private MatchMapGeneratorConfiguration config;

    private AtomicInteger progress;
    private int toMatchArticlesSize;

    public MatchMapGenerator(MatchMapGeneratorConfiguration config) {
        this.config = config;
        progress = new AtomicInteger();
    }

    /**
     * @param articlesToMatch          Set of articles to match
     * @param knownArticles           Set of previously clustered articles
     * @return Map that bind each article with a list of possible matchings
     */
    public Map<Article, List<MatchingArticle>> generateMap(Collection<Article> articlesToMatch, Collection<Article> knownArticles) {
        progress.set(0);
        toMatchArticlesSize = articlesToMatch.size();

        // Source Article -> Similarities with all articles
        Map<Article, List<MatchingArticle>> matchMap = new ConcurrentHashMap<>(articlesToMatch.size());

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
            progress.incrementAndGet();

        });

        return matchMap;
    }

    private Text getTextAndApplyFilters(String s) {
        Text t = config.getStringToTextFn().apply(s);
        config.getTextFilters().forEach(t::applyFilter);
        return t;
    }

    public double getProgress() {
        return (double)this.progress.get()/toMatchArticlesSize;
    }
}