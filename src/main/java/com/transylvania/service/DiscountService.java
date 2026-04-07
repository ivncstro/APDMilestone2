package com.transylvania.service;

import com.transylvania.model.Reservation;

public class DiscountService {

    private final ReservationService reservationService = new ReservationService();

    public double applyDiscount(Reservation reservation, double discountPercent) {
        if (discountPercent < 0) {
            throw new IllegalArgumentException("Discount cannot be negative.");
        }
        double maxCap = AdminSession.getMaxDiscountPercent();
        if (discountPercent > maxCap) {
            throw new IllegalArgumentException(
                    String.format("Max discount allowed is %.0f%%. You tried %.0f%%.", maxCap, discountPercent));
        }
        double original = reservationService.calculateReservationTotal(reservation);
        double discounted = original * (1 - discountPercent / 100.0);
        reservation.setDiscountPercent(discountPercent);
        reservationService.saveOrUpdate(reservation);
        return discounted;
    }

    public double calculateAfterDiscount(Reservation reservation, double discountPercent) {
        double original = new ReservationService().calculateReservationTotal(reservation);
        return original - (original * discountPercent / 100.0);
    }

    public void deleteById(Long id) {

    }
}