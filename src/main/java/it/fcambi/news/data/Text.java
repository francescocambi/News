package it.fcambi.news.data;

import it.fcambi.news.filters.TextFilter;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Francesco on 26/10/15.
 */
public class Text {

    private List<String> words;

    public static Collector<String, List<String>, Text> collector() {
        return new Collector<String, List<String>, Text>() {
            @Override
            public Supplier<List<String>> supplier() {
                return ArrayList<String>::new;
            }

            @Override
            public BiConsumer<List<String>, String> accumulator() {
                return List<String>::add;
            }

            @Override
            public BinaryOperator<List<String>> combiner() {
                return (l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                };
            }

            @Override
            public Function<List<String>, Text> finisher() {
                return (words) -> {
                    Text t = new Text();
                    t.words = words;
                    return t;
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return EnumSet.of(Characteristics.CONCURRENT);
            }
        };
    }

    private Text() {
        words = new ArrayList<>();
    }

    public Text(String s, String splitRegex) {
        words = Arrays.asList(s.split(splitRegex));
    }

    public Text(Text...textsToMerge) {
        words = new ArrayList<>();
        for (Text text : textsToMerge) {
            words.addAll(text.words());
        }
    }

    public Text applyFilter(TextFilter f) {
        words = f.filter(words);
        return this;
    }

    public List<String> wordsSubset(Predicate<String> condition) {
        return words.stream().filter(condition).collect(Collectors.toList());
    }

    public List<String> words() {
        return words;
    }

    public Stream<String> stream() {
        return words.stream();
    }

    @Override
    public String toString() {
        return words.stream().collect(Collectors.joining(" "));
    }
}