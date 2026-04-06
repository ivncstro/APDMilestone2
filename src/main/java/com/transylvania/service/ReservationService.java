package com.transylvania.service;

import com.transylvania.config.BookingRequest;
import com.transylvania.model.*;
import com.transylvania.repository.*;
import com.transylvania.config.LoggerUtil;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReservationService {

    private final GuestRepository guestRepo;
    private final ReservationRepository reservationRepo;
    private final RoomRepository roomRepo;

    public ReservationService() {
        this(new GuestRepository(), new ReservationRepository(), new RoomRepository());
    }

    public ReservationService(GuestRepository guestRepo,
                              ReservationRepository reservationRepo,
                              RoomRepository roomRepo) {
        this.guestRepo = guestRepo;
        this.reservationRepo = reservationRepo;
        this.roomRepo = roomRepo;
    }

    //confirm booking screen
    public String confirmBooking(BookingRequest request) {
        Guest guest = guestRepo.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    Guest g = new Guest(
                            request.getFirstName(),
                            request.getLastName(),
                            request.getEmail(),
                            request.getPhone());
                    guestRepo.save(g);
                    return g;
                });

        Reservation reservation = new Reservation(
                request.getCheckIn(),
                request.getCheckOut(),
                "BOOKED",
                request.getAdults(),
                request.getChildren(),
                guest,
                request.getSelectedRoom()
        );

        List<ReservationAddOn> addOnRows = new ArrayList<>();
        for (Map.Entry<AddOn, Integer> entry : request.getSelectedAddOns().entrySet()) {
            addOnRows.add(new ReservationAddOn(reservation, entry.getKey(), entry.getValue()));
        }

        Reservation saved = reservationRepo.saveWithAddOns(reservation, addOnRows);

        // Ivan: added log info (logging)
        LoggerUtil.logInfo(
                "guest",
                "create_reservation",
                "reservation",
                String.valueOf(saved.getReservationId()),
                "Reservation created for guest email: " + guest.getEmail()
        );

        //confirmation code
        return String.format("RES-%06d", saved.getReservationId());
    }

    public List<Reservation> searchReservations(String guestTerm, String status, LocalDate checkInFrom, LocalDate checkInTo) {
        return reservationRepo.search(guestTerm, status, checkInFrom, checkInTo);
    }

    public List<Room> findAvailableRoomsForAdmin(LocalDate checkIn, LocalDate checkOut, int totalGuests, Long excludedReservationId) {
        return roomRepo.findAvailableRooms(checkIn, checkOut, totalGuests, excludedReservationId);
    }

    public List<Room> findAllRooms() {
        return roomRepo.findAll();
    }

    public Reservation createReservation(AdminReservationRequest request) {
        validateRequest(request, null);
        Guest guest = resolveGuest(request, null);
        Room room = resolveAvailableRoom(request, null);

        Reservation reservation = new Reservation(
                request.checkInDate(),
                request.checkOutDate(),
                request.status(),
                request.adults(),
                request.children(),
                guest,
                room
        );

        Reservation saved = reservationRepo.saveOrUpdate(reservation);

        // Ivan: log for admin creation (logging)
        LoggerUtil.logInfo(
                "admin",
                "create_reservation",
                "reservation",
                String.valueOf(saved.getReservationId()),
                "Admin created reservation for guest email: " + guest.getEmail()
        );

        return saved;
    }

    public Reservation updateReservation(Long reservationId, AdminReservationRequest request) {
        Reservation existing = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));

        validateRequest(request, reservationId);
        Guest guest = resolveGuest(request, existing.getGuest());
        Room room = resolveAvailableRoom(request, reservationId);

        existing.setGuest(guest);
        existing.setRoom(room);
        existing.setCheckInDate(request.checkInDate());
        existing.setCheckOutDate(request.checkOutDate());
        existing.setAdults(request.adults());
        existing.setChildren(request.children());
        existing.setStatus(request.status());

        Reservation updated = reservationRepo.saveOrUpdate(existing);

        // Ivan: log for reservation updates (logging)
        LoggerUtil.logInfo(
                "admin",
                "update_reservation",
                "reservation",
                String.valueOf(updated.getReservationId()),
                "Admin updated reservation for guest email " + guest.getEmail()
        );

        return updated;
    }

    public void deleteReservation(Long reservationId) {
        Reservation existing = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        reservationRepo.deleteById(existing.getReservationId());

        // Ivan: log reservation deletion (logging)
        LoggerUtil.logInfo(
                "admin",
                "delete_reservation",
                "reservation",
                String.valueOf(existing.getReservationId()),
                "Admin deleted reservation for guest email " + existing.getGuest().getEmail()
        );
    }

    public double calculateReservationTotal(Reservation reservation) {
        long nights = Math.max(1, ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate()));
        double roomTotal = reservation.getRoom().getRoomType().getBasePrice() * nights;

        // Ivan: Decorator for AddOns: Dynamic: changed: !hardcode
        CostComponent cost = new BaseCost(roomTotal);

        List<ReservationAddOn> addOns =
                reservationRepo.findAddOnsForReservation(reservation.getReservationId());

        for (ReservationAddOn addOn : addOns) {
            cost = new AddOnCostDecorator(
                    cost,
                    addOn.getAddOn(),
                    addOn.getQuantity(),
                    nights
            );
        }

        double subtotal = cost.getCost();
        return subtotal + (subtotal * 0.13);
    }

    // Ivan: updates for logging
    private void validateRequest(AdminReservationRequest request, Long reservationId) {
        if (request.firstName() == null || request.firstName().isBlank()
                || request.lastName() == null || request.lastName().isBlank()) {
            LoggerUtil.logWarning(
                    "admin",
                    "validate_reservation",
                    "reservation",
                    reservationId == null ? "new" : String.valueOf(reservationId),
                    "Validation failed: guest name is required"
            );
            throw new IllegalArgumentException("Guest first and last name are required.");
        }
        if (request.email() == null || request.email().isBlank()) {
            LoggerUtil.logWarning(
                    "admin",
                    "validate_reservation",
                    "reservation",
                    reservationId == null ? "new" : String.valueOf(reservationId),
                    "Validation failed: guest email is required"
            );
            throw new IllegalArgumentException("Guest email is required.");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            LoggerUtil.logWarning(
                    "admin",
                    "validate_reservation",
                    "reservation",
                    reservationId == null ? "new" : String.valueOf(reservationId),
                    "Validation failed: guest phone is required"
            );
            throw new IllegalArgumentException("Guest phone is required.");
        }
        if (request.checkInDate() == null || request.checkOutDate() == null
                || !request.checkOutDate().isAfter(request.checkInDate())) {
            LoggerUtil.logWarning(
                    "admin",
                    "validate_reservation",
                    "reservation",
                    reservationId == null ? "new" : String.valueOf(reservationId),
                    "Validation failed: invalid check-in/check-out dates"
            );
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }
        if (request.adults() < 1) {
            throw new IllegalArgumentException("At least one adult is required.");
        }
        if (request.children() < 0) {
            throw new IllegalArgumentException("Children cannot be negative.");
        }
        if (request.roomId() == null) {
            throw new IllegalArgumentException("A room must be selected.");
        }

        Room room = roomRepo.findById(request.roomId())
                .orElseThrow(() -> new IllegalArgumentException("Selected room was not found."));
        int totalGuests = request.adults() + request.children();
        if (totalGuests > room.getRoomType().getCapacity()) {
            throw new IllegalArgumentException("Selected room does not fit the requested occupancy.");
        }

        boolean roomAvailable = roomRepo.findAvailableRooms(
                        request.checkInDate(),
                        request.checkOutDate(),
                        totalGuests,
                        reservationId)
                .stream()
                .anyMatch(candidate -> candidate.getRoomId().equals(request.roomId()));
        if (!roomAvailable) {
            throw new IllegalArgumentException("Selected room is not available for those dates.");
        }
    }

    private Guest resolveGuest(AdminReservationRequest request, Guest existingGuest) {
        Optional<Guest> byEmail = guestRepo.findByEmail(request.email());
        Optional<Guest> byPhone = guestRepo.findByPhone(request.phone());

        Guest guest = existingGuest;
        if (guest == null) {
            if (byEmail.isPresent() && byPhone.isPresent()
                    && !byEmail.get().getGuestId().equals(byPhone.get().getGuestId())) {
                throw new IllegalArgumentException("Email and phone belong to different guests.");
            }
            guest = byEmail.or(() -> byPhone).orElseGet(Guest::new);
        }

        if (byEmail.isPresent()
                && guest.getGuestId() != null
                && !byEmail.get().getGuestId().equals(guest.getGuestId())) {
            throw new IllegalArgumentException("Email is already assigned to another guest.");
        }
        if (byPhone.isPresent()
                && guest.getGuestId() != null
                && !byPhone.get().getGuestId().equals(guest.getGuestId())) {
            throw new IllegalArgumentException("Phone number is already assigned to another guest.");
        }

        guest.setFirstName(request.firstName().trim());
        guest.setLastName(request.lastName().trim());
        guest.setEmail(request.email().trim());
        guest.setPhone(request.phone().trim());
        return guestRepo.saveOrUpdate(guest);
    }

    private Room resolveAvailableRoom(AdminReservationRequest request, Long reservationId) {
        return roomRepo.findAvailableRooms(
                        request.checkInDate(),
                        request.checkOutDate(),
                        request.adults() + request.children(),
                        reservationId)
                .stream()
                .filter(room -> room.getRoomId().equals(request.roomId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Selected room is not available."));
    }

    public record AdminReservationRequest(
            String firstName,
            String lastName,
            String email,
            String phone,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int adults,
            int children,
            Long roomId,
            String status
    ) {}
}
