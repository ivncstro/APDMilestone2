package com.transylvania.service;

public class AdminSession {
    private static String currentRole = "ADMIN";

    public static void setRole(String role) {
        currentRole = role;
    }

    public static String getRole() {
        return currentRole;
    }

    public static double getMaxDiscountPercent() {
        if ("MANAGER".equals(currentRole)) return 30.0;
        return 15.0;
    }

    public static void login(String admin) {
    }
}