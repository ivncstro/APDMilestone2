package com.transylvania.service;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.Guest;
import com.transylvania.model.Reservation;
import com.transylvania.model.Room;
import com.transylvania.model.RoomType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;

public class ReservationService {

    public void saveTestReservation() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Guest guest = new Guest("Jane", "Smith", "jane_service@email.com", "4444444444");
            em.persist(guest);

            RoomType roomType = new RoomType("Double", 4, 180.0);
            em.persist(roomType);

            Room room = new Room(102, "AVAILABLE", roomType);
            em.persist(room);

            Reservation reservation = new Reservation(
                    LocalDate.now(),
                    LocalDate.now().plusDays(2),
                    "BOOKED",
                    2,
                    0,
                    guest,
                    room
            );
            em.persist(reservation);

            tx.commit();
            System.out.println("Reservation saved from service");

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