package it.fcambi.news.clustering;

import it.fcambi.news.data.FrequenciesWordVectorFactory;
import it.fcambi.news.data.Text;
import it.fcambi.news.data.WordVectorFactory;
import it.fcambi.news.filters.TextFilter;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.News;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 30/10/15.
 */
public class MatchMapGeneratorConfiguration {

    private List<TextFilter> textFilters = new ArrayList<>();
    private Collection<Metric> metrics = new HashSet<>();
    private Function<String, Text> stringToTextFn;
    private BiFunction<Article, Text, String> keywordSelectionFn;
    private BiPredicate<Article, Article> ignorePairPredicate;
    private WordVectorFactory wordVectorFactory;

    public static BiFunction<Article, Text, String> headlineAndCapitalsKeywords = (article, body) -> {

        String capitals = body.words().stream()
                .filter(w -> w.length() > 0 && Character.isUpperCase(w.charAt(0)))
                .collect(Collectors.joining(" "));

        return article.getTitle()+" "+article.getDescription()+" "+capitals;

    };

    public static BiFunction<Article, Text, String> headlineKeywords = (article, body) ->
            article.getTitle()+" "+article.getDescription();

    public static Function<String, Text> onlyAlphaSpaceSepared = s -> new Text(s.replaceAll("[^\\p{Alpha}\\p{Space}]", " "), "\\p{Space}+");

    public static BiPredicate<Article, Article> ignoreReflectiveMatch = (a,b) -> a.equals(b);

    public MatchMapGeneratorConfiguration setKeywordSelectionFunction(BiFunction<Article, Text, String> k) {
        this.keywordSelectionFn = k;
        return this;
    }

    public MatchMapGeneratorConfiguration setStringToTextFunction(Function<String, Text> fn) {
        this.stringToTextFn = fn;
        return this;
    }

    public MatchMapGeneratorConfiguration addTextFilter(TextFilter tf) {
        this.textFilters.add(tf);
        return this;
    }

    public void removeTextFilter(TextFilter tf) {
        this.textFilters.remove(tf);
    }

    public MatchMapGeneratorConfiguration addMetric(Metric m) {
        this.metrics.add(m);
        return this;
    }

    public void removeMetric(Metric m) {
        this.metrics.remove(m);
    }

    public MatchMapGeneratorConfiguration setIgnorePairPredicate(BiPredicate<Article, Article> p) {
        this.ignorePairPredicate = p;
        return this;
    }

    public MatchMapGeneratorConfiguration setWordVectorFactory(WordVectorFactory f) {
        this.wordVectorFactory = f;
        return this;
    }

    public List<TextFilter> getTextFilters() {
        return textFilters;
    }

    public Collection<Metric> getMetrics() {
        if (metrics.size() == 0)
            metrics.add(new CosineSimilarity());
        return metrics;
    }

    public Function<String, Text> getStringToTextFn() {
        if (stringToTextFn == null)
            stringToTextFn = MatchMapGeneratorConfiguration.onlyAlphaSpaceSepared;
        return stringToTextFn;
    }

    public BiFunction<Article, Text, String> getKeywordSelectionFn() {
        if (keywordSelectionFn == null)
            keywordSelectionFn = MatchMapGeneratorConfiguration.headlineAndCapitalsKeywords;
        return keywordSelectionFn;
    }

    public BiPredicate<Article, Article> getIgnorePairPredicate() {
        if (ignorePairPredicate == null)
            ignorePairPredicate = MatchMapGeneratorConfiguration.ignoreReflectiveMatch;
        return ignorePairPredicate;
    }

    public WordVectorFactory getWordVectorFactory() {
        if (wordVectorFactory == null)
            wordVectorFactory = new FrequenciesWordVectorFactory();
        return wordVectorFactory;
    }

}
