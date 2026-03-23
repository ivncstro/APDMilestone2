package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.RoomType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class RoomTypeRepository {

    public void save(RoomType type) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(type);
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