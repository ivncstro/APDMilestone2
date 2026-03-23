package com.transylvania.config;

import com.transylvania.factory.RoomFactory;
import com.transylvania.model.Room;
import com.transylvania.repository.RoomRepository;

import java.util.List;

public class DataSeeder {

    public static void seed() {
        RoomRepository roomRepo = new RoomRepository();

        if (roomRepo.findAll().isEmpty()) {

            List<Room> rooms = RoomFactory.createRooms();

            for (Room r : rooms) {
                roomRepo.save(r);
            }
        }
    }
}