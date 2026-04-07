package com.transylvania.model;

import jakarta.persistence.*;

@Entity
@Table(name = "loyalty_member")
public class LoyaltyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String loyaltyNumber;

    private int points;
    private int redeemedPoints;

    @OneToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;
    public LoyaltyMember() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLoyaltyNumber() { return loyaltyNumber; }
    public void setLoyaltyNumber(String loyaltyNumber) { this.loyaltyNumber = loyaltyNumber; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public int getRedeemedPoints() { return redeemedPoints; }
    public void setRedeemedPoints(int redeemedPoints) { this.redeemedPoints = redeemedPoints; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }

    public int getAvailablePoints() {
        return points - redeemedPoints;
    }
}