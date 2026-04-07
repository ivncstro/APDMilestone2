package com.transylvania.model;

public enum UserRole {
    ADMIN, MANAGER;

    public double getMaxDiscountPercent() {
        return this == ADMIN ? 15.0 : 30.0;
    }
}