package com.transylvania.service;

import com.transylvania.model.AddOn;

public class AddOnCostDecorator implements CostComponent {

    private CostComponent component;
    private AddOn addOn;
    private int quantity;
    private long nights;

    public AddOnCostDecorator(CostComponent component, AddOn addOn, int quantity, long nights) {
        this.component = component;
        this.addOn = addOn;
        this.quantity = quantity;
        this.nights = nights;
    }

    @Override
    public double getCost() {
        double cost = addOn.getPrice() * quantity;

        if ("PER_NIGHT".equalsIgnoreCase(addOn.getPricingType())) {
            cost *= nights;
        }

        return component.getCost() + cost;
    }
}