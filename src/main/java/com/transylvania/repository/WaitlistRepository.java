package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.Waitlist;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class WaitlistRepository {

    public Waitlist save(Waitlist entry) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (entry.getId() == null) {
                em.persist(entry);
            } else {
                entry = em.merge(entry);
            }
            tx.commit();
            return entry;
        } finally {
            em.close();
        }
    }

    public List<Waitlist> findAllPending() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT w FROM Waitlist w WHERE w.status = 'PENDING' ORDER BY w.id", Waitlist.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void deleteById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Waitlist w = em.find(Waitlist.class, id);
            if (w != null) em.remove(w);
            tx.commit();
        } finally {
            em.close();
        }
    }
}