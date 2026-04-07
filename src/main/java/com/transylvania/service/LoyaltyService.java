package com.transylvania.service;

import com.transylvania.model.Guest;
import com.transylvania.model.LoyaltyMember;
import com.transylvania.repository.LoyaltyRepository;
import java.util.List;
import java.util.UUID;

public class LoyaltyService {

    private final LoyaltyRepository repo;
    public static final int POINTS_PER_DOLLAR = 10;

    public LoyaltyService() {
        this.repo = new LoyaltyRepository();
    }

    public LoyaltyMember enroll(Guest guest) {
        LoyaltyMember m = new LoyaltyMember();
        m.setGuest(guest);
        m.setLoyaltyNumber("LOY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        m.setPoints(0);
        m.setRedeemedPoints(0);
        repo.save(m);
        return m;
    }
    public LoyaltyMember findById(Long id) {
        return repo.findById(id);
    }

    public void addPoints(Long memberId, double amountPaid) {
        LoyaltyMember member = repo.findById(memberId);
        if (member != null) {
            int earned = (int) (amountPaid * POINTS_PER_DOLLAR);
            member.setPoints(member.getPoints() + earned);
            repo.save(member);
            System.out.println("Added " + earned + " points to member " + memberId);
        } else {
            System.err.println("Member not found: " + memberId);
        }
    }

    public double redeemPoints(Long memberId, int pointsToRedeem, double maxDiscountValue) {
        LoyaltyMember member = repo.findById(memberId);
        if (member == null) return 0;

        int available = member.getPoints() - member.getRedeemedPoints();
        int redeem = Math.min(pointsToRedeem, available);
        if (redeem <= 0) return 0;

        double discountValue = (double) redeem / POINTS_PER_DOLLAR;
        if (discountValue > maxDiscountValue) {
            discountValue = maxDiscountValue;
            redeem = (int) (discountValue * POINTS_PER_DOLLAR);
        }
        member.setRedeemedPoints(member.getRedeemedPoints() + redeem);
        repo.save(member);

        System.out.println("Redeemed " + redeem + " points. New redeemed total: " + member.getRedeemedPoints());
        return discountValue;
    }

    public LoyaltyMember findByGuest(Guest guest) {
        return repo.findByGuestId(guest.getGuestId());
    }

    public List<LoyaltyMember> findAll() {
        return repo.findAll();
    }

    public List<LoyaltyMember> search(String keyword) {
        return repo.search(keyword);
    }
}