package com.transylvania.service;

import com.transylvania.model.Room;
import java.util.ArrayList;
import java.util.List;

public class RoomAvailabilityNotifier {
    private static RoomAvailabilityNotifier instance;
    private final List<RoomAvailabilityObserver> roomObservers = new ArrayList<>();
    private final List<WaitlistMatchObserver> waitlistObservers = new ArrayList<>();

    private RoomAvailabilityNotifier() {}

    public static RoomAvailabilityNotifier getInstance() {
        if (instance == null) instance = new RoomAvailabilityNotifier();
        return instance;
    }

    public void addRoomObserver(RoomAvailabilityObserver obs) { roomObservers.add(obs); }
    public void removeRoomObserver(RoomAvailabilityObserver obs) { roomObservers.remove(obs); }
    public void notifyRoomAvailable(Room room) {
        for (RoomAvailabilityObserver obs : roomObservers) obs.onRoomAvailable(room);
    }

    public void addWaitlistObserver(WaitlistMatchObserver obs) { waitlistObservers.add(obs); }
    public void removeWaitlistObserver(WaitlistMatchObserver obs) { waitlistObservers.remove(obs); }
    public void notifyWaitlistMatch(String message) {
        for (WaitlistMatchObserver obs : waitlistObservers) obs.onWaitlistMatch(message);
    }

    public void addObserver(WaitlistService waitlistService) {
    }

    public interface WaitlistMatchObserver {
        void onWaitlistMatch(String message);
    }
}