package it.fcambi.news;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by Francesco on 17/10/15.
 */
public class PersistenceManager {

    private static EntityManagerFactory emFactory;

    public PersistenceManager(String persistenceUnitName) {
        emFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emFactory;
    }

    public EntityManager createEntityManager() {
        return emFactory.createEntityManager();
    }

    public void close() {
        emFactory.close();
    }
}
