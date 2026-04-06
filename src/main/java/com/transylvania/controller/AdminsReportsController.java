package com.transylvania.controller;

import com.transylvania.model.Reservation;
import com.transylvania.repository.ReservationRepository;
import com.transylvania.service.ReservationService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import com.transylvania.config.SceneNavigator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminsReportsController {

    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final ReservationService reservationService = new ReservationService();

    @FXML private TableView<RevenueRow> revenueTable;
    @FXML private TableColumn<RevenueRow, LocalDate> revenueDateColumn;
    @FXML private TableColumn<RevenueRow, Integer> revenueReservationsColumn;
    @FXML private TableColumn<RevenueRow, String> revenueSubtotalColumn;
    @FXML private TableColumn<RevenueRow, String> revenueTaxColumn;
    @FXML private TableColumn<RevenueRow, String> revenueDiscountsColumn;
    @FXML private TableColumn<RevenueRow, String> revenueTotalColumn;

    @FXML private TableView<OccupancyRow> occupancyTable;
    @FXML private TableColumn<OccupancyRow, LocalDate> occupancyDateColumn;
    @FXML private TableColumn<OccupancyRow, Integer> roomsAvailableColumn;
    @FXML private TableColumn<OccupancyRow, Integer> roomsOccupiedColumn;
    @FXML private TableColumn<OccupancyRow, String> occupancyPercentColumn;

    @FXML
    private void goToDashboard() {
        SceneNavigator.goToAdminDashboard();
    }

    @FXML
    private void goToWaitlist() {
        SceneNavigator.goToAdminWaitlist();
    }

    @FXML
    private void goToLoyalty() {
        SceneNavigator.goToAdminLoyalty();
    }

    @FXML
    private void goToReports() {
        SceneNavigator.goToAdminReports();
    }

    @FXML
    private void goToLog() {
        SceneNavigator.goToAdminLog();
    }

    @FXML
    private void goToPayment() {
        SceneNavigator.goToAdminPayment();
    }

    @FXML
    private void goToDiscounts() {
        SceneNavigator.goToAdminDiscounts();
    }

    @FXML
    private void goToFeedback() {
        SceneNavigator.goToAdminFeedback();
    }

    @FXML
    private void logout() {
        SceneNavigator.goToAdminLogin();
    }

    @FXML
    private void initialize() {
        revenueDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().date()));
        revenueReservationsColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().reservations()));
        revenueSubtotalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().subtotal()));
        revenueTaxColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().tax()));
        revenueDiscountsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().discounts()));
        revenueTotalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().total()));

        occupancyDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().date()));
        roomsAvailableColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().roomsAvailable()));
        roomsOccupiedColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().roomsOccupied()));
        occupancyPercentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().occupancyPercent()));

        loadReports();
    }

    private void loadReports() {
        List<Reservation> reservations = reservationRepository.findAll();

        Map<LocalDate, RevenueAccumulator> revenueMap = new LinkedHashMap<>();
        Map<LocalDate, OccupancyAccumulator> occupancyMap = new LinkedHashMap<>();

        for (Reservation reservation : reservations) {
            LocalDate date = reservation.getCheckInDate();
            double totalWithTax = reservationService.calculateReservationTotal(reservation);
            double subtotal = totalWithTax / 1.13;
            double tax = totalWithTax - subtotal;

            revenueMap.putIfAbsent(date, new RevenueAccumulator());
            RevenueAccumulator revenue = revenueMap.get(date);
            revenue.reservations += 1;
            revenue.subtotal += subtotal;
            revenue.tax += tax;
            revenue.total += totalWithTax;

            occupancyMap.putIfAbsent(date, new OccupancyAccumulator());
            OccupancyAccumulator occupancy = occupancyMap.get(date);
            occupancy.roomsOccupied += 1;
        }

        final int TOTAL_ROOMS = 8; // change this if your hotel has a different total

        var revenueRows = revenueMap.entrySet().stream()
                .map(entry -> new RevenueRow(
                        entry.getKey(),
                        entry.getValue().reservations,
                        formatCurrency(entry.getValue().subtotal),
                        formatCurrency(entry.getValue().tax),
                        formatCurrency(0.0),
                        formatCurrency(entry.getValue().total)
                ))
                .toList();

        var occupancyRows = occupancyMap.entrySet().stream()
                .map(entry -> {
                    int occupied = entry.getValue().roomsOccupied;
                    int available = Math.max(0, TOTAL_ROOMS - occupied);
                    double percent = TOTAL_ROOMS == 0 ? 0.0 : (occupied * 100.0 / TOTAL_ROOMS);

                    return new OccupancyRow(
                            entry.getKey(),
                            available,
                            occupied,
                            String.format("%.2f%%", percent)
                    );
                })
                .toList();

        revenueTable.setItems(FXCollections.observableArrayList(revenueRows));
        occupancyTable.setItems(FXCollections.observableArrayList(occupancyRows));
    }

    @FXML
    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Report");
        fileChooser.setInitialFileName("reports.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(revenueTable.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("REVENUE REPORT\n");
            writer.write("Date,Reservations,Subtotal,Tax,Discounts,Total\n");
            for (RevenueRow row : revenueTable.getItems()) {
                writer.write(String.format("%s,%d,%s,%s,%s,%s%n",
                        row.date(),
                        row.reservations(),
                        row.subtotal(),
                        row.tax(),
                        row.discounts(),
                        row.total()));
            }

            writer.write("\n");
            writer.write("OCCUPANCY REPORT\n");
            writer.write("Date,Rooms Available,Rooms Occupied,Occupancy %\n");
            for (OccupancyRow row : occupancyTable.getItems()) {
                writer.write(String.format("%s,%d,%d,%s%n",
                        row.date(),
                        row.roomsAvailable(),
                        row.roomsOccupied(),
                        row.occupancyPercent()));
            }

            showInfo("Export successful", "CSV report exported successfully.");
        } catch (IOException e) {
            showError("Export failed", "Could not export CSV report.");
        }
    }

    @FXML
    private void handleExportTxt() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save TXT Report");
        fileChooser.setInitialFileName("reports.txt");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showSaveDialog(revenueTable.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("REVENUE REPORT\n");
            writer.write("==============================\n");
            for (RevenueRow row : revenueTable.getItems()) {
                writer.write("Date: " + row.date() + "\n");
                writer.write("Reservations: " + row.reservations() + "\n");
                writer.write("Subtotal: " + row.subtotal() + "\n");
                writer.write("Tax: " + row.tax() + "\n");
                writer.write("Discounts: " + row.discounts() + "\n");
                writer.write("Total: " + row.total() + "\n\n");
            }

            writer.write("OCCUPANCY REPORT\n");
            writer.write("==============================\n");
            for (OccupancyRow row : occupancyTable.getItems()) {
                writer.write("Date: " + row.date() + "\n");
                writer.write("Rooms Available: " + row.roomsAvailable() + "\n");
                writer.write("Rooms Occupied: " + row.roomsOccupied() + "\n");
                writer.write("Occupancy %: " + row.occupancyPercent() + "\n\n");
            }

            showInfo("Export successful", "TXT report exported successfully.");
        } catch (IOException e) {
            showError("Export failed", "Could not export TXT report.");
        }
    }

    private String formatCurrency(double amount) {
        return String.format("$%.2f", amount);
    }

    private void showInfo(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class RevenueAccumulator {
        int reservations = 0;
        double subtotal = 0.0;
        double tax = 0.0;
        double total = 0.0;
    }

    private static class OccupancyAccumulator {
        int roomsOccupied = 0;
    }

    private record RevenueRow(
            LocalDate date,
            int reservations,
            String subtotal,
            String tax,
            String discounts,
            String total
    ) {}

    private record OccupancyRow(
            LocalDate date,
            int roomsAvailable,
            int roomsOccupied,
            String occupancyPercent
    ) {}
}