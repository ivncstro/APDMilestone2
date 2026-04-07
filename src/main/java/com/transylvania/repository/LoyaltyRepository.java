package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.LoyaltyMember;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.util.List;

public class LoyaltyRepository {

    public void save(LoyaltyMember member) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (member.getId() == null) {
                em.persist(member);
            } else {
                em.merge(member);
            }
            tx.commit();
        } finally {
            em.close();
        }
    }

    public LoyaltyMember findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(LoyaltyMember.class, id);
        } finally {
            em.close();
        }
    }

    public List<LoyaltyMember> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT l FROM LoyaltyMember l JOIN FETCH l.guest", LoyaltyMember.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<LoyaltyMember> search(String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
        String pattern = "%" + keyword.toLowerCase() + "%";
        try {
            return em.createQuery(
                            "SELECT l FROM LoyaltyMember l JOIN l.guest g " +
                                    "WHERE LOWER(g.firstName) LIKE :pattern OR LOWER(g.lastName) LIKE :pattern " +
                                    "OR LOWER(g.phone) LIKE :pattern OR LOWER(g.email) LIKE :pattern " +
                                    "OR LOWER(l.loyaltyNumber) LIKE :pattern", LoyaltyMember.class)
                    .setParameter("pattern", pattern)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    public LoyaltyMember findByGuestId(Long guestId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT l FROM LoyaltyMember l WHERE l.guest.guestId = :guestId", LoyaltyMember.class)
                    .setParameter("guestId", guestId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}