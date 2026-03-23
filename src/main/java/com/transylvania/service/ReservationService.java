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