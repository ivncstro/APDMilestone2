package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.AddOn;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class AddOnRepository {

    public void save(AddOn addOn) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(addOn);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}