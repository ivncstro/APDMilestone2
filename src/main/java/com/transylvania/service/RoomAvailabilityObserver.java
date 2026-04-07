package com.transylvania.service;

import com.transylvania.model.Room;

public interface RoomAvailabilityObserver {
    void onRoomAvailable(Room room);
}