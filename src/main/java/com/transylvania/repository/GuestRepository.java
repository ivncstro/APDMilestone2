package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.Guest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

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

    // looks for existing email from guest
    public Optional<Guest> findByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Guest> results = em.createQuery(
                            "SELECT g FROM Guest g WHERE g.email = :email", Guest.class)
                    .setParameter("email", email)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            em.close();
        }
    }
}