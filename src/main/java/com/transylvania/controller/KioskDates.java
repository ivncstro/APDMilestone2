package com.transylvania.controller;

import com.transylvania.config.BookingRequest;
import com.transylvania.config.SceneNavigator;
import com.transylvania.config.SceneNavigator.BookingAware;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;

public class KioskDates implements BookingAware {

    @FXML private DatePicker checkInDate;
    @FXML private DatePicker checkOutDate;

    private BookingRequest bookingRequest;

    @Override
    public void setBookingRequest(BookingRequest request) {
        this.bookingRequest = request;
        //restores if going back
        if (request.getCheckIn()  != null) checkInDate.setValue(request.getCheckIn());
        if (request.getCheckOut() != null) checkOutDate.setValue(request.getCheckOut());
    }

    @FXML
    private void onFindRooms() {
        if (!validate()) return;
        bookingRequest.setCheckIn(checkInDate.getValue());
        bookingRequest.setCheckOut(checkOutDate.getValue());
        SceneNavigator.goToGuestCount();
    }

    @FXML
    private void onBack() {
        SceneNavigator.goToKioskMain();
    }

    @FXML
    private void onCancel() {
        SceneNavigator.goToKioskMain();
    }

    private boolean validate() {
        LocalDate in  = checkInDate.getValue();
        LocalDate out = checkOutDate.getValue();
        if (in == null || out == null) {
            alert("Please select both a check-in and check-out date.");
            return false;
        }
        if (in.isBefore(LocalDate.now())) {
            alert("Check-in date cannot be in the past.");
            return false;
        }
        if (!out.isAfter(in)) {
            alert("Check-out must be at least one night after check-in.");
            return false;
        }
            return true;
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
