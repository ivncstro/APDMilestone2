package com.transylvania.model;

import jakarta.persistence.*;

@Entity
@Table(name = "room_type")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id")
    private Long roomTypeId;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "base_price", nullable = false)
    private double basePrice;

    public RoomType() {
    }

    public RoomType(String typeName, int capacity, double basePrice) {
        this.typeName = typeName;
        this.capacity = capacity;
        this.basePrice = basePrice;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }
}