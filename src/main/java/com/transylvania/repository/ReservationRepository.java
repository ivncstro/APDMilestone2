package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.Reservation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class ReservationRepository {

    public void save(Reservation reservation) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(reservation);
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