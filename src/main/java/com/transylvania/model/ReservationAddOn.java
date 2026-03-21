package com.transylvania.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reservation_add_on")
public class ReservationAddOn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_add_on_id")
    private Long reservationAddOnId;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "add_on_id", nullable = false)
    private AddOn addOn;

    public ReservationAddOn() {
    }

    public ReservationAddOn(Reservation reservation, AddOn addOn, int quantity) {
        this.reservation = reservation;
        this.addOn = addOn;
        this.quantity = quantity;
    }

    public Long getReservationAddOnId() {
        return reservationAddOnId;
    }

    public void setReservationAddOnId(Long reservationAddOnId) {
        this.reservationAddOnId = reservationAddOnId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public AddOn getAddOn() {
        return addOn;
    }

    public void setAddOn(AddOn addOn) {
        this.addOn = addOn;
    }
}