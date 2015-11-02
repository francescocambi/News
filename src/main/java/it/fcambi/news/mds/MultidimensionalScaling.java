package it.fcambi.news.mds;

import it.fcambi.news.model.MatchingArticle;
import it.fcambi.news.PersistenceManager;
import it.fcambi.news.clustering.MatchMapGenerator;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.metrics.EuclideanDistance;
import it.fcambi.news.model.Article;

import javax.persistence.EntityManager;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Francesco on 22/10/15.
 */
public class MultidimensionalScaling {

    public static void main(String[] args) {

        PersistenceManager persistenceManager = new PersistenceManager("it.fcambi.news.jpa.local");
        EntityManager em = persistenceManager.createEntityManager();
        List<Article> articles = em.createQuery("select a from Article a where a.news.articles.size > 2", Article.class).getResultList();

        MatchMapGeneratorConfiguration conf = new MatchMapGeneratorConfiguration()
                .addMetric(new EuclideanDistance());

        Map<Article, List<MatchingArticle>> matchMap = new MatchMapGenerator(conf).generateMap(articles, articles);

        Path distanceFilePath = Paths.get("distanceMatrix.csv");
        Path labelsFilePath = Paths.get("labelsVector.csv");
        try (BufferedWriter distanceFileWriter = Files.newBufferedWriter(distanceFilePath);
             BufferedWriter labelsFileWriter = Files.newBufferedWriter(labelsFilePath)) {

            List<Article> arts = new ArrayList<>();
            arts.addAll(matchMap.keySet());
            arts.sort((a, b) -> {
                if (a.getId() < b.getId()) return -1;
                if (a.getId() > b.getId()) return 1;
                else return 0;
            });

            String labels = arts.stream()
                            .map(a -> a.getId() + "")
                            .collect(Collectors.joining(","));

            labelsFileWriter.write(labels);
//            distanceFileWriter.write(labels);

            distanceFileWriter.write(
                    arts.stream().map(article -> {

                        matchMap.get(article).sort((a, b) -> {
                            if (a.getArticle().getId() < b.getArticle().getId()) return -1;
                            if (a.getArticle().getId() > b.getArticle().getId()) return 1;
                            else return 0;
                        });
                        return matchMap.get(article).stream()
                                .map(matchingArticle -> (matchingArticle.getSimilarity("euclidean")) + "")
                                .collect(Collectors.joining(","));

                    }).collect(Collectors.joining("\n"))
            );


        } catch (IOException e) {
            e.printStackTrace();
        }

        em.close();
        persistenceManager.close();

        System.out.println("Export Finished!");

    }

}
