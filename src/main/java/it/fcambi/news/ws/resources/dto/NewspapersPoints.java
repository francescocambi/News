package it.fcambi.news.ws.resources.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import it.fcambi.news.model.Newspaper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Francesco on 17/11/15.
 */
public class NewspapersPoints {
    @JsonIgnore
    double[][] points;
    @JsonIgnore
    Newspaper[] newspapers;

    public NewspapersPoints(double[][] points, Newspaper[] newspapers) {
        this.points = points;
        this.newspapers = newspapers;
    }

    @JsonValue
    public Map<Newspaper, double[]> points() {
        Map<Newspaper, double[]> p = new HashMap<>();
        for (int i = 0; i < points.length; i++)
            p.put(newspapers[i], points[i]);

        return p;
    }
}