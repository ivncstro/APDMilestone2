package com.transylvania.service;

import com.transylvania.config.BookingRequest;
import com.transylvania.config.JpaUtil;
import com.transylvania.model.*;
import com.transylvania.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReservationService {

    public void saveTestReservation() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Guest guest = new Guest("Jane", "Smith", "jane_service@email.com", "4444444444");
            em.persist(guest);

            RoomType roomType = RoomFactory.createRoomType("Double",4,180);
            em.persist(roomType);

            Room room = RoomFactory.createroom(102,"AVAILABLE",roomType);
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

    //confirm booking screen
    public String confirmBooking(BookingRequest request) {
        GuestRepository guestRepo = new GuestRepository();
        ReservationRepository resRepo   = new ReservationRepository();

        Guest guest = guestRepo.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    Guest g = new Guest(
                            request.getFirstName(),
                            request.getLastName(),
                            request.getEmail(),
                            request.getPhone());
                    guestRepo.save(g);
                    return g;
                });

        Reservation reservation = new Reservation(
                request.getCheckIn(),
                request.getCheckOut(),
                "BOOKED",
                request.getAdults(),
                request.getChildren(),
                guest,
                request.getSelectedRoom()
        );

        List<ReservationAddOn> addOnRows = new ArrayList<>();
        for (Map.Entry<AddOn, Integer> entry : request.getSelectedAddOns().entrySet()) {
            addOnRows.add(new ReservationAddOn(reservation, entry.getKey(), entry.getValue()));
        }

        Reservation saved = resRepo.saveWithAddOns(reservation, addOnRows);

        //confirmation code
        return String.format("RES-%06d", saved.getReservationId());
    }

}