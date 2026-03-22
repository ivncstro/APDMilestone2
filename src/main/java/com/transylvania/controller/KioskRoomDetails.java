package com.transylvania.controller;

import com.transylvania.model.Room;
import com.transylvania.repository.RoomRepository;
import com.transylvania.config.BookingRequest;
import com.transylvania.config.SceneNavigator;
import com.transylvania.config.SceneNavigator.BookingAware;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.util.List;

public class KioskRoomDetails implements BookingAware {

    @FXML private Label            guestSummary;
    @FXML private Spinner<Integer> singleRoomSpinner;
    @FXML private Spinner<Integer> doubleRoomSpinner;
    @FXML private Spinner<Integer> deluxeRoomSpinner;
    @FXML private Spinner<Integer> penthouseSpinner;

    private BookingRequest  bookingRequest;
    private List<Room>      availableRooms;
    private String          suggestedTypeName;

    private final RoomRepository roomRepo = new RoomRepository();

    @Override
    public void setBookingRequest(BookingRequest request) {
        this.bookingRequest = request;
        initSpinners();
        loadAvailableRooms();
        showSuggestion();
    }

    private void initSpinners() {
        for (Spinner<Integer> s : List.of(singleRoomSpinner, doubleRoomSpinner,
                deluxeRoomSpinner, penthouseSpinner)) {
            s.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));
            s.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal > 0) clearOthers(s);
            });
        }
        if (bookingRequest.getSelectedRoom() != null) {
            String typeName = bookingRequest.getSelectedRoom()
                    .getRoomType().getTypeName().toLowerCase();
            spinnerFor(typeName).getValueFactory().setValue(1);
        }
    }

    private void loadAvailableRooms() {
        availableRooms = roomRepo.findAvailableRooms(
                bookingRequest.getCheckIn(),
                bookingRequest.getCheckOut(),
                bookingRequest.getTotalGuests());
    }

    private void showSuggestion() {
        int total = bookingRequest.getTotalGuests();
        // suggests smallest room for guest
        suggestedTypeName = availableRooms.stream()
                .filter(r -> r.getRoomType().getCapacity() >= total)
                .map(r -> r.getRoomType().getTypeName())
                .findFirst()
                .orElse("Double");
    }
    @FXML
    private void onAcceptSuggestion() {
        spinnerFor(suggestedTypeName.toLowerCase()).getValueFactory().setValue(1);
    }

    @FXML
    private void onChooseOwn() {
        clearAll();
    }

    @FXML
    private void onNext() {
        String selectedType = selectedTypeName();
        if (selectedType == null) {
            alert("Please select a room type to continue.");
            return;
        }
        Room room = availableRooms.stream()
                .filter(r -> r.getRoomType().getTypeName()
                        .equalsIgnoreCase(selectedType))
                .findFirst()
                .orElse(null);

        if (room == null) {
            alert("Sorry, no " + selectedType + " rooms are available for those dates. "
                    + "Please choose another type.");
            return;
        }

        bookingRequest.setSelectedRoom(room);
        SceneNavigator.goToDetails();
    }

    @FXML private void onBack()   { SceneNavigator.goToGuestCount(); }
    @FXML private void onCancel() { SceneNavigator.goToKioskMain(); }

    //helpers
    private String selectedTypeName() {
        if (singleRoomSpinner.getValue()    > 0) return "Single";
        if (doubleRoomSpinner.getValue()    > 0) return "Double";
        if (deluxeRoomSpinner.getValue()    > 0) return "Deluxe";
        if (penthouseSpinner.getValue()     > 0) return "Penthouse";
        return null;
    }

    private Spinner<Integer> spinnerFor(String typeLower) {
        return switch (typeLower) {
            case "single"     -> singleRoomSpinner;
            case "deluxe"     -> deluxeRoomSpinner;
            case "penthouse"  -> penthouseSpinner;
            default           -> doubleRoomSpinner;
        };
    }

    private void clearOthers(Spinner<Integer> keep) {
        for (Spinner<Integer> s : List.of(singleRoomSpinner, doubleRoomSpinner,
                deluxeRoomSpinner, penthouseSpinner)) {
            if (s != keep) s.getValueFactory().setValue(0);
        }
    }

    private void clearAll() {
        for (Spinner<Integer> s : List.of(singleRoomSpinner, doubleRoomSpinner,
                deluxeRoomSpinner, penthouseSpinner)) {
            s.getValueFactory().setValue(0);
        }
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}