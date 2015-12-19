package it.fcambi.news.data;

import it.fcambi.news.model.TFDictionary;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Francesco on 27/10/15.
 */
public class TFIDFVectorTest {

    static TFDictionary dictionary;

    static String sourceText;
    static Double[] sourceFreq;

    static String evalText;
    static Double[] evalFreq;

    static Double[] sourceWeights;
    static Double[] evalWeights;

    @BeforeClass
    public static void setUp() {

        dictionary = new TFDictionary();
        dictionary.getTerms().put("indic", 72);
        dictionary.getTerms().put("indifferent", 6);
        dictionary.getTerms().put("indiscrezion", 17);
        dictionary.getTerms().put("indicizz", 3);
        dictionary.setNumOfDocuments(90);

        sourceText = "a poche ore dalla sentenza indic a poche ore dalla indifferent sul indic";
        evalText = "a si apre un dalla indicizz indicizz maroni indic a";

        Double[] s = {2.0, 2.0, 2.0, 2.0, 1.0, 2.0, 1.0, 1.0};
        sourceFreq = s;

        Double[] t = {2.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0};
        evalFreq = t;

        Double[] weights = new Double[8];
        weights[0] = (2.0/13)*Math.log(90);
        weights[1] = weights[0];
        weights[2] = weights[0];
        weights[3] = weights[0];
        weights[4] = (1.0/13)*Math.log(90);
        weights[5] = (2.0/13)*Math.log(90.0/72);
        weights[6] = (1.0/13)*Math.log(90.0/6);
        weights[7] = weights[4];
        sourceWeights = weights;

        evalWeights = new Double[8];
        evalWeights[0] = (2.0/10)*Math.log(90);
        evalWeights[1] = 0.0;
        evalWeights[2] = 0.0;
        evalWeights[3] = (1.0/10)*Math.log(90);
        evalWeights[4] = 0.0;
        evalWeights[5] = (1/10.0)*Math.log(90/72.0);
        evalWeights[6] = 0.0;
        evalWeights[7] = 0.0;

    }

    @Test
    public void testSourceWeights() {
        Text source = new Text(sourceText, "[ ]+");

        TFIDFWordVector v = new TFIDFWordVector(dictionary);
        v.setFrom(source);
        Double[] s = new Double[8];
        v.getFrequencies().toArray(s);

        assertArrayEquals("Source Frequencies array", sourceFreq, s);

        Double[] t = new Double[8];
        v.getValues().toArray(t);

        assertArrayEquals("Source Weights array", sourceWeights, t);

    }

    @Test
    public void testgetWordsWeightFor() {

        Text source = new Text(sourceText, "[ ]+");
        Text eval = new Text(evalText, "[ ]+");

        TFIDFWordVector w = new TFIDFWordVector(dictionary);
        w.setFrom(source);

        TFIDFWordVector v = new TFIDFWordVector(dictionary);
        List<String> words = new ArrayList<>();
        words.addAll(w.getWords());
        v.setWords(words);

        v.setValuesFrom(eval);
        Double[] wf = v.getFrequencies().toArray(new Double[8]);
        assertArrayEquals("Eval frequencies array", evalFreq, wf);

        Double[] ww = v.getValues().toArray(new Double[8]);
        assertArrayEquals("Eval weights array", evalWeights, ww);

    }
}
