package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.Room;
import com.transylvania.service.RoomAvailabilityNotifier;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
                    .setParameter("guests", totalGuests)
                    .setParameter("checkIn", checkIn)
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
            return em.createQuery(
                            "SELECT r FROM Room r " +
                                    "JOIN FETCH r.roomType " +
                                    "ORDER BY r.roomNumber",
                            Room.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public Optional<Room> findById(Long roomId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Room> results = em.createQuery(
                            "SELECT r FROM Room r " +
                                    "JOIN FETCH r.roomType " +
                                    "WHERE r.roomId = :roomId",
                            Room.class)
                    .setParameter("roomId", roomId)
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

    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int totalGuests, Long excludedReservationId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql =
                    "SELECT r FROM Room r " +
                    "JOIN FETCH r.roomType " +
                    "WHERE r.status = 'AVAILABLE' " +
                    "  AND r.roomType.capacity >= :guests " +
                    "  AND r.roomId NOT IN (" +
                    "      SELECT res.room.roomId FROM Reservation res " +
                    "      WHERE res.status <> 'CANCELLED' " +
                    "        AND (:excludedReservationId IS NULL OR res.reservationId <> :excludedReservationId) " +
                    "        AND NOT (res.checkOutDate <= :checkIn OR res.checkInDate >= :checkOut)" +
                    "  ) " +
                    "ORDER BY r.roomType.basePrice ASC, r.roomNumber ASC";

            return em.createQuery(jpql, Room.class)
                    .setParameter("guests", totalGuests)
                    .setParameter("checkIn", checkIn)
                    .setParameter("checkOut", checkOut)
                    .setParameter("excludedReservationId", excludedReservationId)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }


    public void updateRoomStatus(Room room, String newStatus) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Room managed = em.find(Room.class, room.getRoomId());
            String oldStatus = managed.getStatus();
            managed.setStatus(newStatus);
            em.merge(managed);
            tx.commit();
            if (!"AVAILABLE".equals(oldStatus) && "AVAILABLE".equals(newStatus)) {
                RoomAvailabilityNotifier.getInstance().notifyRoomAvailable(managed);
            }
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
