package it.fcambi.news.fpclustering;

import it.fcambi.news.model.FrontPage;

import java.util.List;
import java.util.Vector;

/**
 * Created by Francesco on 20/11/15.
 */
public class FrontPagesClustering {

    /**
     *
     * @param pages Sorted (by timestamp asc) list of front pages to group
     * @return List of generated groups
     */
    public List<FrontPagesTimestampGroup> groupFrontPagesByTimestamp(List<FrontPage> pages) {
        if (pages.size() == 0)
            return null;

        //Group frontpages by timestamp (created in a 5 minutes range)
        List<FrontPagesTimestampGroup> groupedPages = new Vector<>();
        FrontPagesTimestampGroup firstGroup = new FrontPagesTimestampGroup();
        firstGroup.setTimestamp(pages.get(0).getTimestamp());
        firstGroup.addFrontPage(pages.get(0));
        groupedPages.add(firstGroup);

        for (int i = 1; i < pages.size(); i++) {
            long progressionTime = groupedPages.get(groupedPages.size()-1).getTimestamp().getTimeInMillis();

            if (pages.get(i).getTimestamp().getTimeInMillis() - progressionTime > 300000) {
                //Creates a new time range class for this front page
                FrontPagesTimestampGroup group = new FrontPagesTimestampGroup();
                group.setTimestamp(pages.get(i).getTimestamp());
                groupedPages.add(group);
            }
            groupedPages.get(groupedPages.size()-1).addFrontPage(pages.get(i));
        }

        return groupedPages;
    }

}
