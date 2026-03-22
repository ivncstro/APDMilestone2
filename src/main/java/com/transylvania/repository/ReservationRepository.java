package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.Reservation;
import com.transylvania.model.ReservationAddOn;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Collections;
import java.util.List;

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
}