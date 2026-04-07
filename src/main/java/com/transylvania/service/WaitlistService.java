package com.transylvania.service;

import com.transylvania.model.Room;
import com.transylvania.model.RoomType;
import com.transylvania.model.Waitlist;
import com.transylvania.repository.WaitlistRepository;

import java.util.List;
import java.util.stream.Collectors;

public class WaitlistService implements RoomAvailabilityObserver {

    private final WaitlistRepository waitlistRepo = new WaitlistRepository();

    public WaitlistService() {
        RoomAvailabilityNotifier.getInstance().addObserver(this);
    }

    public Waitlist addToWaitlist(Waitlist entry) {
        entry.setStatus("PENDING");
        return waitlistRepo.save(entry);
    }

    public List<Waitlist> getAllPending() {
        return waitlistRepo.findAllPending();
    }

    public void convertToReservation(Waitlist entry) {
        entry.setStatus("CONVERTED");
        waitlistRepo.save(entry);
    }

    @Override
    public void onRoomAvailable(Room room) {
        String availableRoomType = room.getRoomType().getTypeName();
        List<Waitlist> matching = getAllPending().stream()
                .filter(w -> w.getRoomType().equalsIgnoreCase(availableRoomType))
                .collect(Collectors.toList());

        if (!matching.isEmpty()) {
            String message = "Room " + room.getRoomNumber() + " (" + availableRoomType + ") is now available! "
                    + matching.size() + " waitlist entries can be converted.";
            RoomAvailabilityNotifier.getInstance().notifyWaitlistMatch(message);
        }
    }
}