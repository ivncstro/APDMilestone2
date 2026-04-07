package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.Discount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class DiscountRepository {

    public void save(Discount discount) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (discount.getId() == null) em.persist(discount);
            else em.merge(discount);
            tx.commit();
        } finally {
            em.close();
        }
    }

    public List<Discount> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT d FROM Discount d", Discount.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void deleteById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Discount d = em.find(Discount.class, id);
            if (d != null) em.remove(d);
            tx.commit();
        } finally {
            em.close();
        }
    }
}