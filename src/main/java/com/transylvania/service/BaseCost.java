package com.transylvania.service;

public class BaseCost implements CostComponent {
    private double baseCost;

    public BaseCost(double baseCost) {
        this.baseCost = baseCost;
    }

    @Override
    public double getCost() {
        return baseCost;
    }
}