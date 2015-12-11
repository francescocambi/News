//package it.fcambi.news;
//
//import it.fcambi.news.clustering.HighestMeanOverThresholdMatcher;
//import it.fcambi.news.clustering.MatchMapGenerator;
//import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
//import it.fcambi.news.clustering.Matcher;
//import it.fcambi.news.data.TFIDFWordVectorFactory;
//import it.fcambi.news.filters.NoiseWordsTextFilter;
//import it.fcambi.news.filters.StemmerTextFilter;
//import it.fcambi.news.metrics.CosineSimilarity;
//import it.fcambi.news.metrics.Metric;
//import it.fcambi.news.model.*;
//
//import javax.persistence.EntityManager;
//import java.text.NumberFormat;
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
///**
// * Created by Francesco on 29/10/15.
// */
//public class IncrementalClustering {
//
//    public static void main(String[] args) {
//
//        PersistenceManager persistenceManager = new PersistenceManager("it.fcambi.news.jpa.local");
//        EntityManager em = persistenceManager.createEntityManager();
//
//        Clustering clustering = new Clustering();
//
//        List<Article> articles = em.createQuery("select a from Article a where key(a.news) = 'manual'",
//                Article.class)
//                .getResultList();
//
//        List<Article> classifiedArticles = new ArrayList<>();
//
//        Metric metric = new CosineSimilarity();
//        TFDictionary dictionary = em.find(TFDictionary.class, "italian_stemmed");
//
//        MatchMapGeneratorConfiguration conf = new MatchMapGeneratorConfiguration()
//                .addMetric(metric)
//                .addTextFilter(new NoiseWordsTextFilter())
//                .addTextFilter(new StemmerTextFilter())
//                .setWordVectorFactory(new TFIDFWordVectorFactory(dictionary));
////                .setStringToTextFunction(MatchMapGeneratorConfiguration.onlyAlphanumericSpaceSeparated);
//        MatchMapGenerator generator = new MatchMapGenerator(conf);
//
//        NumberFormat percent = NumberFormat.getPercentInstance();
//        percent.setMaximumFractionDigits(2);
//
//        for (int i=0; i<articles.size()-1; i++) {
//
//            Map<Article, List<MatchingArticle>> map = generator.generateMap(articles.subList(i, i+1), classifiedArticles);
//
//            Matcher matcher = new HighestMeanOverThresholdMatcher(metric, 0.47, clustering);
//            Map<Article, MatchingNews> bestMatch = matcher.findBestMatch(map);
//
//            bestMatch.keySet().forEach(article -> {
//                if (bestMatch.get(article) != null) {
//                    article.setNews(clustering, bestMatch.get(article).getNews());
//                } else {
//                    article.setNews(clustering, new News(clustering));
//                    article.getNews(clustering).setDescription(article.getTitle());
//                    article.getNews(clustering).setArticles(new ArrayList<>());
//                }
//                article.getNews(clustering).getArticles().add(article);
//                classifiedArticles.add(article);
//            });
//
//            System.out.println("Clustering status "+percent.format((double)i/(articles.size()-1)));
//
//        }
//
//        Set<News> generatedClustersRes = new HashSet<>();
//        classifiedArticles.forEach(article -> generatedClustersRes.add(article.getNews(clustering)));
//
////        List<News> generatedClustersRes = em.createQuery("select n from News n where n.clustering.name='auto_test'", News.class).getResultList();
//
//        List<News> expectedClustersRes = em.createQuery("select n from News n where n.clustering.name = 'manual'", News.class).getResultList();
//
//        System.out.println("Pre-filter generated # "+generatedClustersRes.size());
//        System.out.println("Pre-filter expected  # "+expectedClustersRes.size());
//
//        //Exclude articles not in front page top 5
//        List<News> generatedClusters = generatedClustersRes.stream().map(news -> {
//            List<Article> as = news.getArticles().stream().filter(article -> {
//                //Filter all articles with order >= 5
//                return article.getFrontPages().stream().filter(fp -> fp.orderOf(article) < 11).collect(Collectors.counting()) >0;
//            }).collect(Collectors.toList());
//            news.setArticles(as);
//
//            return news;
//        }).filter(news -> news.size() > 0).collect(Collectors.toList());
//
//        List<News> expectedClusters = expectedClustersRes.stream().map(news -> {
//            List<Article> as = news.getArticles().stream().filter(article -> {
//                return article.getFrontPages().stream().filter(fp -> fp.orderOf(article) < 11).collect(Collectors.counting()) >0;
//            }).collect(Collectors.toList());
//            news.setArticles(as);
//
//            return news;
//        }).filter(news -> news.size() > 0).collect(Collectors.toList());
//
//
//        if (generatedClusters.size() == 0)
//            throw new IllegalStateException("Empty generatedClusters collection");
//
//        Collection<News> rows;
//        Collection<News> cols;
//        if (generatedClusters.size() >= expectedClusters.size()) {
//            //Now check congruency between predicted and effective graph
//            rows = generatedClusters;
//            cols = expectedClusters;
//        } else {
//            //Check congruency between effective and predicted graph
//            rows = expectedClusters;
//            cols = generatedClusters;
//        }
//
//        double[] distribution = rows.stream().mapToDouble(row -> {
//
//            return cols.stream().map(col -> {
//
//                //Compute jaccard
//                long intersection = row.getArticles().stream().filter(a -> col.getArticles().contains(a)).collect(Collectors.counting());
//                long union = Stream.of(row, col).flatMap(l -> l.getArticles().stream()).collect(Collectors.toSet()).size();
//
//                return (double)intersection / union;
//
//            }).max(Double::compare).get();
//
//        }).toArray();
//
//        //Compute precision recall
//
//        class IRStats {
//            public double precision;
//            public double recall;
//
//            public IRStats(double precision, double recall) {
//                this.precision = precision;
//                this.recall = recall;
//            }
//
//            public double getPrecision() {
//                return precision;
//            }
//
//            public double getRecall() {
//                return recall;
//            }
//        }
//
//        List<IRStats> irstats = expectedClusters.stream().map(exp -> {
//
//            return generatedClusters.stream().map(gen -> {
//
//                long intersection = exp.getArticles().stream().filter(a -> gen.getArticles().contains(a)).collect(Collectors.counting());
//                return new IRStats((double)intersection/gen.size(), (double)intersection/exp.size());
//
//            }).max((a, b) -> Double.compare(a.recall, b.recall)).get();
//
//        }).collect(Collectors.toList());
//
//        DoubleSummaryStatistics precisionStats = irstats.stream().mapToDouble(IRStats::getPrecision).summaryStatistics();
//        DoubleSummaryStatistics recallStats = irstats.stream().mapToDouble(IRStats::getRecall).summaryStatistics();
//
//        double fmeasure = (2.0*(precisionStats.getAverage()*recallStats.getAverage()))/(precisionStats.getAverage()+recallStats.getAverage());
//
//        //TODO Compute prec rec std dev
//
//        System.out.println("Precision >> AVG: "+precisionStats.getAverage()+" ["+precisionStats.getMin()+" ; "+precisionStats.getMax()+"]");
//        System.out.println("Recall >> AVG: "+recallStats.getAverage()+" ["+recallStats.getMin()+" ; "+recallStats.getMax()+"]");
//        System.out.println("F-MEASURE >> "+fmeasure);
//
////        Path filePath = Paths.get("jaccardDistribution.csv");
////        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
////            writer.write(Arrays.stream(distribution).sorted()
////                            .mapToObj(String::valueOf)
////                            .collect(Collectors.joining("\n"))
////            );
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//
//        DoubleSummaryStatistics stats = Arrays.stream(distribution).summaryStatistics();
//
//        //Compute standard deviation
//        double squaredOffsetSum = Arrays.stream(distribution).map(x -> Math.pow(x-stats.getAverage(), 2)).sum();
//        double stdDeviation = Math.sqrt(squaredOffsetSum/stats.getCount());
//
//        System.out.println("Average Jacc: "+stats.getAverage());
//        System.out.println("Jacc Standard Deviation: "+stdDeviation);
//        System.out.println("Min "+stats.getMin()+"\tMax "+stats.getMax());
//        System.out.println("# of clusters obtained "+stats.getCount());
//        System.out.println("# of clusters expected "+expectedClusters.size());
//
//        em.close();
//        persistenceManager.close();
//
//    }
//
//}
