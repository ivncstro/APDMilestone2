package com.transylvania.service;

import com.transylvania.model.Reservation;
import com.transylvania.model.Room;
import com.transylvania.repository.ReservationRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReportService {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public ReportService() {
        this.reservationRepository = new ReservationRepository();
        this.reservationService = new ReservationService();
    }

    public void exportRevenueReportCsv(String filePath) {
        List<Reservation> reservations = reservationRepository.findAll();

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("Reservation ID,Guest,Check-In,Check-Out,Room,Status,Total Revenue\n");

            for (Reservation reservation : reservations) {
                double total = reservationService.calculateReservationTotal(reservation);
                String guestName = reservation.getGuest().getFirstName() + " " + reservation.getGuest().getLastName();
                String roomLabel = "Room " + reservation.getRoom().getRoomNumber() + " - " +
                        reservation.getRoom().getRoomType().getTypeName();

                writer.append(String.valueOf(reservation.getReservationId())).append(",");
                writer.append(escape(guestName)).append(",");
                writer.append(String.valueOf(reservation.getCheckInDate())).append(",");
                writer.append(String.valueOf(reservation.getCheckOutDate())).append(",");
                writer.append(escape(roomLabel)).append(",");
                writer.append(escape(reservation.getStatus())).append(",");
                writer.append(String.format("%.2f", total)).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export revenue report.", e);
        }
    }

    public void exportOccupancyReportCsv(String filePath) {
        List<Reservation> reservations = reservationRepository.findAll();

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("Reservation ID,Room,Check-In,Check-Out,Nights,Occupancy Status\n");

            for (Reservation reservation : reservations) {
                Room room = reservation.getRoom();
                long nights = Math.max(1,
                        ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate()));

                String roomLabel = "Room " + room.getRoomNumber() + " - " + room.getRoomType().getTypeName();

                writer.append(String.valueOf(reservation.getReservationId())).append(",");
                writer.append(escape(roomLabel)).append(",");
                writer.append(String.valueOf(reservation.getCheckInDate())).append(",");
                writer.append(String.valueOf(reservation.getCheckOutDate())).append(",");
                writer.append(String.valueOf(nights)).append(",");
                writer.append("Occupied").append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export occupancy report.", e);
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}