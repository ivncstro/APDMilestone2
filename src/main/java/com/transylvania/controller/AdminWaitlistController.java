package com.transylvania.controller;

import com.transylvania.config.SceneNavigator;
import com.transylvania.model.Guest;
import com.transylvania.model.Reservation;
import com.transylvania.model.Room;
import com.transylvania.model.Waitlist;
import com.transylvania.repository.GuestRepository;
import com.transylvania.repository.RoomRepository;
import com.transylvania.service.ReservationService;
import com.transylvania.service.WaitlistService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AdminWaitlistController {

    @FXML private TextField guestNameField, phoneField;
    @FXML private ComboBox<String> roomTypeCombo;
    @FXML private DatePicker checkInPicker, checkOutPicker;
    @FXML private TableView<WaitlistRow> waitlistTable;
    @FXML private TableColumn<WaitlistRow, String> guestNameCol, phoneCol, roomTypeCol, checkInCol, checkOutCol, statusCol;
    @FXML private TableColumn<WaitlistRow, Void> actionCol;

    private final WaitlistService waitlistService = new WaitlistService();
    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        roomTypeCombo.setItems(FXCollections.observableArrayList("Single", "Double", "Deluxe", "Penthouse"));
        setupTable();
        loadWaitlist();
    }

    private void setupTable() {
        guestNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGuestName()));
        phoneCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhone()));
        roomTypeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoomType()));
        checkInCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCheckIn().toString()));
        checkOutCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCheckOut().toString()));
        statusCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));

        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button convertBtn = new Button("Convert to Reservation");
            {
                convertBtn.setOnAction(e -> {
                    WaitlistRow row = getTableView().getItems().get(getIndex());
                    convertToReservation(row);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : convertBtn);
            }
        });
    }

    private void loadWaitlist() {
        List<Waitlist> entries = waitlistService.getAllPending();
        List<WaitlistRow> rows = entries.stream()
                .filter(w -> w != null)
                .map(WaitlistRow::new)
                .collect(Collectors.toList());
        waitlistTable.setItems(FXCollections.observableArrayList(rows));
    }

    @FXML
    private void addToWaitlist() {
        String guestName = guestNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String roomType = roomTypeCombo.getValue();
        LocalDate checkIn = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();

        if (guestName.isEmpty() || phone.isEmpty() || roomType == null || checkIn == null || checkOut == null) {
            showAlert("Error", "All fields are required.");
            return;
        }
        if (!checkOut.isAfter(checkIn)) {
            showAlert("Error", "Check-out must be after check-in.");
            return;
        }

        Waitlist entry = new Waitlist();
        entry.setGuestName(guestName);
        entry.setPhone(phone);
        entry.setRoomType(roomType);
        entry.setCheckIn(checkIn);
        entry.setCheckOut(checkOut);

        Waitlist saved = waitlistService.addToWaitlist(entry);
        clearForm();
        loadWaitlist();
        showAlert("Success", "Guest added to waitlist.");
    }

    private void convertToReservation(WaitlistRow row) {
        int totalGuests = 2;
        RoomRepository roomRepo = new RoomRepository();
        List<Room> availableRooms = roomRepo.findAvailableRooms(
                row.getCheckIn(),
                row.getCheckOut(),
                totalGuests,
                null
        );

        Room matchedRoom = availableRooms.stream()
                .filter(r -> r.getRoomType().getTypeName().equalsIgnoreCase(row.getRoomType()))
                .findFirst()
                .orElse(null);

        if (matchedRoom == null) {
            showAlert("No room", "No room of type " + row.getRoomType() + " is available for those dates.");
            return;
        }
        GuestRepository guestRepo = new GuestRepository();
        Guest guest = guestRepo.findByPhone(row.getPhone()).orElse(null);

        if (guest == null) {
            guest = new Guest();
            String[] nameParts = row.getGuestName().split(" ");
            guest.setFirstName(nameParts[0]);
            guest.setLastName(nameParts.length > 1 ? nameParts[1] : "");
            guest.setPhone(row.getPhone());
            String uniqueEmail = (row.getGuestName().replace(" ", ".") + System.currentTimeMillis() + "@temp.com").toLowerCase();
            guest.setEmail(uniqueEmail);

            guest = guestRepo.saveOrUpdate(guest);
        }

        Reservation reservation = new Reservation();
        reservation.setCheckInDate(row.getCheckIn());
        reservation.setCheckOutDate(row.getCheckOut());
        reservation.setStatus("BOOKED");
        reservation.setAdults(totalGuests);
        reservation.setChildren(0);
        reservation.setGuest(guest);
        reservation.setRoom(matchedRoom);

        new ReservationService().saveOrUpdate(reservation);

        waitlistService.convertToReservation(row.getOriginal());
        loadWaitlist();
        showAlert("Converted", "Reservation created from waitlist entry.");
    }

    private void clearForm() {
        guestNameField.clear();
        phoneField.clear();
        roomTypeCombo.getSelectionModel().clearSelection();
        checkInPicker.setValue(null);
        checkOutPicker.setValue(null);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML private void goToDashboard() { SceneNavigator.goToAdminDashboard(); }
    @FXML private void goToWaitlist() { SceneNavigator.goToAdminWaitlist(); }
    @FXML private void goToLoyalty() { SceneNavigator.goToAdminLoyalty(); }
    @FXML private void goToReports() { SceneNavigator.goToAdminReports(); }
    @FXML private void goToLog() { SceneNavigator.goToAdminLog(); }
    @FXML private void goToPayment() { SceneNavigator.goToAdminPayment(); }
    @FXML private void goToDiscounts() { SceneNavigator.goToAdminDiscounts(); }
    @FXML private void goToFeedback() { SceneNavigator.goToAdminFeedback(); }
    @FXML private void logout() { SceneNavigator.goToAdminLogin(); }

    public static class WaitlistRow {
        private final Waitlist original;

        public WaitlistRow(Waitlist w) {
            if (w == null) throw new IllegalArgumentException("Waitlist cannot be null");
            this.original = w;
        }

        public String getGuestName() { return original.getGuestName(); }
        public String getPhone() { return original.getPhone(); }
        public String getRoomType() { return original.getRoomType(); }
        public LocalDate getCheckIn() { return original.getCheckIn(); }
        public LocalDate getCheckOut() { return original.getCheckOut(); }
        public String getStatus() { return original.getStatus(); }
        public Waitlist getOriginal() { return original; }
    }
}