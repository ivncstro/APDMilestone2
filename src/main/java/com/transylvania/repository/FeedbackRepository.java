package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.Feedback;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FeedbackRepository {

    public Feedback save(Feedback feedback) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(feedback);
            tx.commit();
            return feedback;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to save feedback", e);
        } finally {
            em.close();
        }
    }

    public boolean existsByReservationId(Long reservationId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(f) FROM Feedback f WHERE f.reservation.reservationId = :reservationId",
                            Long.class)
                    .setParameter("reservationId", reservationId)
                    .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    public List<Feedback> search(String guestName, Integer rating, String sentimentTag, LocalDate fromDate, LocalDate toDate) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String normalizedName = guestName == null ? "" : guestName.trim().toLowerCase();
            String normalizedTag = sentimentTag == null ? "" : sentimentTag.trim().toUpperCase();

            return em.createQuery(
                            "SELECT f FROM Feedback f " +
                                    "JOIN FETCH f.guest g " +
                                    "JOIN FETCH f.reservation r " +
                                    "WHERE (:guestName = '' OR " +
                                    "       LOWER(g.firstName) LIKE :guestPattern OR " +
                                    "       LOWER(g.lastName) LIKE :guestPattern OR " +
                                    "       LOWER(CONCAT(g.firstName, ' ', g.lastName)) LIKE :guestPattern) " +
                                    "  AND (:rating IS NULL OR f.rating = :rating) " +
                                    "  AND (:sentimentTag = '' OR UPPER(f.sentimentTag) = :sentimentTag) " +
                                    "  AND (:fromDate IS NULL OR f.submittedDate >= :fromDate) " +
                                    "  AND (:toDate IS NULL OR f.submittedDate <= :toDate) " +
                                    "ORDER BY f.submittedDate DESC, f.feedbackId DESC",
                            Feedback.class)
                    .setParameter("guestName", normalizedName)
                    .setParameter("guestPattern", "%" + normalizedName + "%")
                    .setParameter("rating", rating)
                    .setParameter("sentimentTag", normalizedTag)
                    .setParameter("fromDate", fromDate)
                    .setParameter("toDate", toDate)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<Feedback> findAll() {
        return search(null, null, null, null, null);
    }

    public Optional<Feedback> findByReservationId(Long reservationId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Feedback> results = em.createQuery(
                            "SELECT f FROM Feedback f " +
                                    "JOIN FETCH f.guest " +
                                    "JOIN FETCH f.reservation " +
                                    "WHERE f.reservation.reservationId = :reservationId",
                            Feedback.class)
                    .setParameter("reservationId", reservationId)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        } finally {
            em.close();
        }
    }
}
