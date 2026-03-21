package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class RoomRepository {

    public void save(Room room) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(room);
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