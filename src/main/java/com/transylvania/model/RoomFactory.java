package com.transylvania.model;

public class RoomFactory {
    public static RoomType createRoomType(String type, int capacity, double basePrice){
        return new RoomType(type,capacity,basePrice);

    }
    public static Room createroom(int roomNumber,String status, RoomType type){
        return new Room(roomNumber,status,type);
    }
}
