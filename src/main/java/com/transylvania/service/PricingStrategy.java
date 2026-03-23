package com.transylvania.service;

import com.transylvania.config.BookingRequest;

public interface PricingStrategy {
    double calculateRoomTotal(BookingRequest request);
}
