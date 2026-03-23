package com.transylvania.model;

import com.transylvania.model.Room;
import com.transylvania.model.RoomType;

import java.util.ArrayList;
import java.util.List;

public class RoomFactory {

    public static List<Room> createRooms() {
        List<Room> rooms = new ArrayList<>();

        RoomType single = new RoomType("Single", 2, 100);
        RoomType doubleR = new RoomType("Double", 4, 180);
        RoomType deluxe = new RoomType("Deluxe", 4, 250);
        RoomType penthouse = new RoomType("Penthouse", 6, 500);

        // create 2 of each type
        rooms.add(new Room(101, "AVAILABLE", single));
        rooms.add(new Room(102, "AVAILABLE", single));

        rooms.add(new Room(201, "AVAILABLE", doubleR));
        rooms.add(new Room(202, "AVAILABLE", doubleR));

        rooms.add(new Room(301, "AVAILABLE", deluxe));
        rooms.add(new Room(302, "AVAILABLE", deluxe));

        rooms.add(new Room(401, "AVAILABLE", penthouse));
        rooms.add(new Room(402, "AVAILABLE", penthouse));

        return rooms;
    }
}