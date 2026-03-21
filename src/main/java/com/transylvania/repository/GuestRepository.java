package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.Guest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class GuestRepository {

    public void save(Guest guest) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(guest);
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