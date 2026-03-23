package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

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

    //looks for available rooms
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int totalGuests) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Room r " +
                                    "WHERE r.status = 'AVAILABLE' " +
                                    "  AND r.roomType.capacity >= :guests " +
                                    "  AND r.roomId NOT IN (" +
                                    "      SELECT res.room.roomId FROM Reservation res " +
                                    "      WHERE res.status <> 'CANCELLED' " +
                                    "        AND NOT (res.checkOutDate <= :checkIn " +
                                    "             OR  res.checkInDate  >= :checkOut)" +
                                    "  ) " +
                                    "ORDER BY r.roomType.basePrice ASC",
                            Room.class)
                    .setParameter("guests",   totalGuests)
                    .setParameter("checkIn",  checkIn)
                    .setParameter("checkOut", checkOut)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    // added to check if database has the rooms
    public List<Room> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT r FROM Room r", Room.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }
}