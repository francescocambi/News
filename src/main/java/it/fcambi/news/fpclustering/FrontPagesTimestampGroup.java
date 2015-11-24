package it.fcambi.news.fpclustering;

import it.fcambi.news.model.FrontPage;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

/**
 * Created by Francesco on 20/11/15.
 */
public class FrontPagesTimestampGroup {

    private Calendar timestamp;
    private List<FrontPage> frontPages;

    public FrontPagesTimestampGroup() {
        frontPages = new Vector<>();
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public List<FrontPage> getFrontPages() {
        return frontPages;
    }

    public void setFrontPages(List<FrontPage> frontPages) {
        this.frontPages = frontPages;
    }

    public void addFrontPage(FrontPage fp) {
        this.frontPages.add(fp);
    }

    public void removeFrontPage(FrontPage fp) {
        this.frontPages.remove(fp);
    }

    public FrontPage getFrontPage(int i) {
        return this.frontPages.get(i);
    }
}
