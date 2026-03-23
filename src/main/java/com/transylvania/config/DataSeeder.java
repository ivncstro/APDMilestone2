package com.transylvania.config;

import com.transylvania.model.AddOn;
import com.transylvania.model.Room;
import com.transylvania.model.RoomFactory;
import com.transylvania.model.RoomType;
import com.transylvania.repository.AddOnRepository;
import com.transylvania.repository.RoomRepository;
import com.transylvania.repository.RoomTypeRepository;

import java.util.List;

public class DataSeeder {

    public static void seed() {
        RoomRepository roomRepo = new RoomRepository();
        RoomTypeRepository roomTypeRepo = new RoomTypeRepository();
        AddOnRepository addOnRepo = new AddOnRepository();

        if (roomRepo.findAll().isEmpty()) {
            List<Room> rooms = RoomFactory.createRooms();

            for (Room r : rooms) {
                RoomType type = r.getRoomType();
                roomTypeRepo.save(type);
                roomRepo.save(r);
            }
        }

        if (addOnRepo.findAll().isEmpty()) {
            addOnRepo.save(new AddOn("Wi-Fi", 10.0, "PER_NIGHT"));
            addOnRepo.save(new AddOn("Breakfast", 15.0, "PER_NIGHT"));
            addOnRepo.save(new AddOn("Parking", 20.0, "PER_NIGHT"));
            addOnRepo.save(new AddOn("Spa", 50.0, "PER_STAY"));
        }
    }
}