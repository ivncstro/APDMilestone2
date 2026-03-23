package com.transylvania.service;

import com.transylvania.config.BookingRequest;

public class StandardPricing implements PricingStrategy{
    @Override
    public double calculateRoomTotal(BookingRequest request) {
        if (request.getSelectedRoom() == null || request.getNights() == 0){
            return 0.0;
        }
        double basePrice = request.getSelectedRoom().getRoomType().getBasePrice();
        long nights = request.getNights();
        return basePrice * nights;

    }



}
