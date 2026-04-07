package com.transylvania.controller;

import com.transylvania.config.SceneNavigator;
import com.transylvania.model.Reservation;
import com.transylvania.model.Room;
import com.transylvania.service.ReservationService;
import com.transylvania.service.ReservationService.AdminReservationRequest;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import com.transylvania.service.ReportService;
import javafx.stage.FileChooser;
import java.io.File;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController {

    private final ReservationService reservationService = new ReservationService();
    private final ObservableList<ReservationRow> reservations = FXCollections.observableArrayList();
    private final ReportService reportService = new ReportService();

    @FXML private TextField searchField;
    @FXML private DatePicker checkInFromPicker;
    @FXML private DatePicker checkInToPicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TableView<ReservationRow> reservationTable;
    @FXML private TableColumn<ReservationRow, Long> reservationIdColumn;
    @FXML private TableColumn<ReservationRow, String> guestColumn;
    @FXML private TableColumn<ReservationRow, String> phoneColumn;
    @FXML private TableColumn<ReservationRow, LocalDate> checkInColumn;
    @FXML private TableColumn<ReservationRow, LocalDate> checkOutColumn;
    @FXML private TableColumn<ReservationRow, String> roomsColumn;
    @FXML private TableColumn<ReservationRow, String> totalColumn;
    @FXML private TableColumn<ReservationRow, String> balanceColumn;
    @FXML private TableColumn<ReservationRow, String> statusColumn;
    @FXML private TableColumn<ReservationRow, String> discountColumn;
    @FXML private TableColumn<ReservationRow, String> paymentMethodColumn;
    @FXML private TableColumn<ReservationRow, String> loyaltyColumn;

    @FXML
    private void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList("All", "BOOKED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"));
        statusCombo.getSelectionModel().selectFirst();

        reservationIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().reservationId()));
        guestColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().guestName()));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().phone()));
        checkInColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().checkInDate()));
        checkOutColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().checkOutDate()));
        roomsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().roomLabel()));
        totalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().total()));
        balanceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().balanceDue()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));
        discountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().discount()));
        paymentMethodColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().paymentMethod()));
        loyaltyColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().loyaltyMember()));

        reservationTable.setItems(reservations);
        refreshReservations();
    }

    @FXML
    private void handleSearch() {
        refreshReservations();
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        checkInFromPicker.setValue(null);
        checkInToPicker.setValue(null);
        statusCombo.getSelectionModel().selectFirst();
        refreshReservations();
    }

    @FXML
    private void handleNewReservation() {
        showReservationDialog(null);
    }

    @FXML
    private void handleEditReservation() {
        ReservationRow selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No reservation selected", "Select a reservation to edit.");
            return;
        }
        showReservationDialog(selected.source());
    }

    @FXML
    private void handleDeleteReservation() {
        ReservationRow selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No reservation selected", "Select a reservation to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Delete reservation");
        confirm.setContentText("Delete reservation #" + selected.reservationId() + " for " + selected.guestName() + "?");
        if (confirm.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }

        try {
            reservationService.deleteReservation(selected.reservationId());
            refreshReservations();
        } catch (Exception e) {
            showError("Delete failed", e.getMessage());
        }
    }

    @FXML
    private void handleExportRevenueReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Revenue Report");
        fileChooser.setInitialFileName("revenue_report.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(reservationTable.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            reportService.exportRevenueReportCsv(file.getAbsolutePath());
            showInfo("Export successful", "Revenue report exported successfully.");
        } catch (Exception e) {
            showError("Export failed", e.getMessage());
        }
    }

    @FXML
    private void handleExportOccupancyReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Occupancy Report");
        fileChooser.setInitialFileName("occupancy_report.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(reservationTable.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            reportService.exportOccupancyReportCsv(file.getAbsolutePath());
            showInfo("Export successful", "Occupancy report exported successfully.");
        } catch (Exception e) {
            showError("Export failed", e.getMessage());
        }
    }

    @FXML private void goToWaitlist() { SceneNavigator.goToAdminWaitlist(); }
    @FXML private void goToLoyalty() { SceneNavigator.goToAdminLoyalty(); }
    @FXML private void goToReports() { SceneNavigator.goToAdminReports(); }
    @FXML private void goToLog() { SceneNavigator.goToAdminLog(); }
    @FXML private void goToPayment() { SceneNavigator.goToAdminPayment(); }
    @FXML private void goToDiscounts() { SceneNavigator.goToAdminDiscounts(); }
    @FXML private void goToFeedback() { SceneNavigator.goToAdminFeedback(); }
    @FXML private void logout() { SceneNavigator.goToKioskMain(); }
    @FXML private void goBack() { SceneNavigator.goToKioskMain(); }

    private void refreshReservations() {
        List<ReservationRow> rows = reservationService.searchReservations(
                        searchField.getText(),
                        normalizeStatus(statusCombo.getValue()),
                        checkInFromPicker.getValue(),
                        checkInToPicker.getValue())
                .stream()
                .map(this::toRow)
                .toList();
        reservations.setAll(rows);
    }

    private String normalizeStatus(String selectedStatus) {
        return selectedStatus == null || "All".equalsIgnoreCase(selectedStatus) ? null : selectedStatus;
    }

    private ReservationRow toRow(Reservation reservation) {
        double total = reservationService.calculateReservationTotal(reservation);
        String roomLabel = "Room " + reservation.getRoom().getRoomNumber() + " - " + reservation.getRoom().getRoomType().getTypeName();
        return new ReservationRow(
                reservation,
                reservation.getReservationId(),
                reservation.getGuest().getFirstName() + " " + reservation.getGuest().getLastName(),
                reservation.getGuest().getPhone(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                roomLabel,
                formatCurrency(total),
                formatCurrency(total),
                reservation.getStatus(),
                "N/A",
                "Front Desk",
                "No"
        );
    }

    private void showReservationDialog(Reservation existing) {
        Dialog<AdminReservationRequest> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "New Reservation" : "Edit Reservation");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField firstNameField = new TextField(existing == null ? "" : existing.getGuest().getFirstName());
        TextField lastNameField = new TextField(existing == null ? "" : existing.getGuest().getLastName());
        TextField emailField = new TextField(existing == null ? "" : existing.getGuest().getEmail());
        TextField phoneField = new TextField(existing == null ? "" : existing.getGuest().getPhone());
        DatePicker checkInPicker = new DatePicker(existing == null ? LocalDate.now().plusDays(1) : existing.getCheckInDate());
        DatePicker checkOutPicker = new DatePicker(existing == null ? LocalDate.now().plusDays(2) : existing.getCheckOutDate());
        Spinner<Integer> adultsSpinner = new Spinner<>(1, 10, existing == null ? 1 : existing.getAdults());
        Spinner<Integer> childrenSpinner = new Spinner<>(0, 10, existing == null ? 0 : existing.getChildren());
        ComboBox<Room> roomCombo = new ComboBox<>();
        ComboBox<String> statusField = new ComboBox<>(FXCollections.observableArrayList("BOOKED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"));
        statusField.getSelectionModel().select(existing == null ? "BOOKED" : existing.getStatus());

        roomCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Room room) {
                if (room == null) {
                    return "";
                }
                return "Room " + room.getRoomNumber() + " - " + room.getRoomType().getTypeName() + " (cap " + room.getRoomType().getCapacity() + ")";
            }

            @Override
            public Room fromString(String string) {
                return null;
            }
        });

        Runnable reloadRooms = () -> {
            Room previousSelection = roomCombo.getValue();
            int totalGuests = adultsSpinner.getValue() + childrenSpinner.getValue();
            LocalDate checkIn = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();
            List<Room> options = (checkIn != null && checkOut != null && checkOut.isAfter(checkIn))
                    ? reservationService.findAvailableRoomsForAdmin(checkIn, checkOut, totalGuests, existing == null ? null : existing.getReservationId())
                    : reservationService.findAllRooms();
            roomCombo.setItems(FXCollections.observableArrayList(options));


            if (previousSelection != null) {
                roomCombo.getItems().stream()
                        .filter(room -> room.getRoomId().equals(previousSelection.getRoomId()))
                        .findFirst()
                        .ifPresent(roomCombo::setValue);
            } else if (existing != null) {
                roomCombo.getItems().stream()
                        .filter(room -> room.getRoomId().equals(existing.getRoom().getRoomId()))
                        .findFirst()
                        .ifPresent(roomCombo::setValue);
            } else if (!roomCombo.getItems().isEmpty()) {
                roomCombo.getSelectionModel().selectFirst();
            }
        };

        checkInPicker.valueProperty().addListener((obs, oldValue, newValue) -> reloadRooms.run());
        checkOutPicker.valueProperty().addListener((obs, oldValue, newValue) -> reloadRooms.run());
        adultsSpinner.valueProperty().addListener((obs, oldValue, newValue) -> reloadRooms.run());
        childrenSpinner.valueProperty().addListener((obs, oldValue, newValue) -> reloadRooms.run());
        reloadRooms.run();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("First Name"), firstNameField);
        grid.addRow(1, new Label("Last Name"), lastNameField);
        grid.addRow(2, new Label("Email"), emailField);
        grid.addRow(3, new Label("Phone"), phoneField);
        grid.addRow(4, new Label("Check-in"), checkInPicker);
        grid.addRow(5, new Label("Check-out"), checkOutPicker);
        grid.addRow(6, new Label("Adults"), adultsSpinner);
        grid.addRow(7, new Label("Children"), childrenSpinner);
        grid.addRow(8, new Label("Room"), roomCombo);
        grid.addRow(9, new Label("Status"), statusField);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }

            Room selectedRoom = roomCombo.getValue();
            return new AdminReservationRequest(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    checkInPicker.getValue(),
                    checkOutPicker.getValue(),
                    adultsSpinner.getValue(),
                    childrenSpinner.getValue(),
                    selectedRoom == null ? null : selectedRoom.getRoomId(),
                    statusField.getValue()
            );
        });

        Optional<AdminReservationRequest> result = dialog.showAndWait();
        result.ifPresent(request -> {
            try {
                if (existing == null) {
                    reservationService.createReservation(request);
                } else {
                    reservationService.updateReservation(existing.getReservationId(), request);
                }
                refreshReservations();
            } catch (Exception e) {
                showError(existing == null ? "Create failed" : "Update failed", e.getMessage());
                showReservationDialog(existing);
            }
        });
    }

    private String formatCurrency(double amount) {
        return String.format("$%.2f", amount);
    }

    private void showWarning(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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

    private void showInfo(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record ReservationRow(
            Reservation source,
            Long reservationId,
            String guestName,
            String phone,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String roomLabel,
            String total,
            String balanceDue,
            String status,
            String discount,
            String paymentMethod,
            String loyaltyMember
    ) {}
}
