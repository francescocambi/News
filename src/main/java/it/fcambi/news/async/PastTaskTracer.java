package it.fcambi.news.async;

import it.fcambi.news.Application;
import it.fcambi.news.Logging;
import it.fcambi.news.model.tasks.PastTask;

import javax.persistence.EntityManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Francesco on 04/11/15.
 */
public class PastTaskTracer implements TaskCompletedObserver {

    private static Logger log = Logging.registerLogger("");

    @Override
    public void taskCompleted(Task t) {
        EntityManager em = Application.getEntityManager();

        PastTask p = PastTask.createFrom(t);

        try {
            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Can't persist PastTask.", e);
        }

        em.close();
    }
}
