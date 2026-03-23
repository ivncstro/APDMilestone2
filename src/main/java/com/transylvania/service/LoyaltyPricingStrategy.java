package com.transylvania.service;

import com.transylvania.config.BookingRequest;

public class LoyaltyPricingStrategy implements PricingStrategy{
    private static double loyaltyDiscountMulti = 1.2;

    @Override
    public double calculateRoomTotal(BookingRequest request) {
        if (request.getSelectedRoom() == null || request.getNights() == 0) {
            return 0.0;
        }

        double basePrice = request.getSelectedRoom().getRoomType().getBasePrice();
        long nights = request.getNights();
        double subtotal = basePrice * nights;
        return subtotal * loyaltyDiscountMulti;
    }
}
