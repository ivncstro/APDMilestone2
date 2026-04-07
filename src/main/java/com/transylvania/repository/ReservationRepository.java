package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.Reservation;
import com.transylvania.model.ReservationAddOn;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    //keeps reserveration and add-ons from guest
    public Reservation saveWithAddOns(Reservation reservation, List<ReservationAddOn> addOns) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(reservation);
            em.flush(); // get the generated ID before persisting add-ons
            for (ReservationAddOn rao : addOns) {
                rao.setReservation(reservation);
                em.persist(rao);
            }
            tx.commit();
            return reservation;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Failed to save reservation", e);
        } finally {
            em.close();
        }
    }

    public List<Reservation> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Reservation r " +
                                    "JOIN FETCH r.guest " +
                                    "JOIN FETCH r.room room " +
                                    "JOIN FETCH room.roomType " +
                                    "ORDER BY r.checkInDate DESC",
                            Reservation.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<ReservationAddOn> findAddOnsForReservation(Long reservationId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT ra FROM ReservationAddOn ra " +
                                    "WHERE ra.reservation.reservationId = :id",
                            ReservationAddOn.class)
                    .setParameter("id", reservationId)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public Optional<Reservation> findById(Long reservationId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Reservation> results = em.createQuery(
                            "SELECT r FROM Reservation r " +
                                    "JOIN FETCH r.guest " +
                                    "JOIN FETCH r.room room " +
                                    "JOIN FETCH room.roomType " +
                                    "WHERE r.reservationId = :reservationId",
                            Reservation.class)
                    .setParameter("reservationId", reservationId)
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

    public List<Reservation> search(String guestTerm, String status, LocalDate checkInFrom, LocalDate checkInTo) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String normalizedTerm = guestTerm == null ? "" : guestTerm.trim().toLowerCase();
            String normalizedStatus = status == null ? "" : status.trim().toUpperCase();

            return em.createQuery(
                            "SELECT r FROM Reservation r " +
                                    "JOIN FETCH r.guest g " +
                                    "JOIN FETCH r.room room " +
                                    "JOIN FETCH room.roomType " +
                                    "WHERE (:guestTerm = '' OR " +
                                    "       LOWER(g.firstName) LIKE :guestPattern OR " +
                                    "       LOWER(g.lastName) LIKE :guestPattern OR " +
                                    "       LOWER(g.email) LIKE :guestPattern OR " +
                                    "       LOWER(g.phone) LIKE :guestPattern OR " +
                                    "       LOWER(CONCAT(g.firstName, ' ', g.lastName)) LIKE :guestPattern) " +
                                    "  AND (:status = '' OR UPPER(r.status) = :status) " +
                                    "  AND (:checkInFrom IS NULL OR r.checkInDate >= :checkInFrom) " +
                                    "  AND (:checkInTo IS NULL OR r.checkInDate <= :checkInTo) " +
                                    "ORDER BY r.checkInDate DESC, r.reservationId DESC",
                            Reservation.class)
                    .setParameter("guestTerm", normalizedTerm)
                    .setParameter("guestPattern", "%" + normalizedTerm + "%")
                    .setParameter("status", normalizedStatus)
                    .setParameter("checkInFrom", checkInFrom)
                    .setParameter("checkInTo", checkInTo)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public Reservation saveOrUpdate(Reservation reservation) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Reservation managed = reservation.getReservationId() == null ? reservation : em.merge(reservation);
            if (reservation.getReservationId() == null) {
                em.persist(managed);
            }
            tx.commit();
            return managed;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to save reservation", e);
        } finally {
            em.close();
        }
    }

    public void deleteById(Long reservationId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM Feedback f WHERE f.reservation.reservationId = :reservationId")
                    .setParameter("reservationId", reservationId)
                    .executeUpdate();
            em.createQuery("DELETE FROM ReservationAddOn ra WHERE ra.reservation.reservationId = :reservationId")
                    .setParameter("reservationId", reservationId)
                    .executeUpdate();
            em.createQuery("DELETE FROM Reservation r WHERE r.reservationId = :reservationId")
                    .setParameter("reservationId", reservationId)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete reservation", e);
        } finally {
            em.close();
        }
    }

    public Optional<Reservation> findLatestCheckedOutByPhoneWithoutFeedback(String phone) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Reservation> results = em.createQuery(
                            "SELECT r FROM Reservation r " +
                                    "JOIN FETCH r.guest g " +
                                    "JOIN FETCH r.room room " +
                                    "JOIN FETCH room.roomType " +
                                    "WHERE g.phone = :phone " +
                                    "  AND UPPER(r.status) = 'CHECKED_OUT' " +
                                    "  AND r.checkOutDate <= :today " +
                                    "  AND NOT EXISTS (" +
                                    "      SELECT 1 FROM Feedback f WHERE f.reservation.reservationId = r.reservationId" +
                                    "  ) " +
                                    "ORDER BY r.checkOutDate DESC, r.reservationId DESC",
                            Reservation.class)
                    .setParameter("phone", phone)
                    .setParameter("today", LocalDate.now())
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
