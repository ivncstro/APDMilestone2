package com.transylvania.controller;

import com.transylvania.config.BookingRequest;
import com.transylvania.config.SceneNavigator;
import com.transylvania.config.SceneNavigator.BookingAware;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class KioskDetails implements BookingAware {

    @FXML private TextField guestName1;
    @FXML private TextField guestName;
    @FXML private TextField guestEmail;
    @FXML private TextField guestPhone;
    @FXML private TextField guestAddress;
    @FXML private TextField guestCountry;
    @FXML private TextField guestCity;
    @FXML private CheckBox  loyaltyEnroll;

    private BookingRequest bookingRequest;

    @Override
    public void setBookingRequest(BookingRequest request) {
        this.bookingRequest = request;

        if (request.getFirstName() != null) guestName1.setText(request.getFirstName());
        if (request.getLastName()  != null) guestName.setText(request.getLastName());
        if (request.getEmail()     != null) guestEmail.setText(request.getEmail());
        if (request.getPhone()     != null) guestPhone.setText(request.getPhone());
    }

    @FXML
    private void onNext() {
        if (!validate()) return;

        bookingRequest.setFirstName(guestName1.getText().trim());
        bookingRequest.setLastName(guestName.getText().trim());
        bookingRequest.setEmail(guestEmail.getText().trim());
        bookingRequest.setPhone(guestPhone.getText().trim());
        bookingRequest.setLoyaltyEnroll(loyaltyEnroll.isSelected());

        SceneNavigator.goToAddons();
    }

    @FXML private void onBack()   { SceneNavigator.goToRoomDetails(); }
    @FXML private void onCancel() { SceneNavigator.goToKioskMain(); }

    private boolean validate() {
        if (guestName1.getText().trim().isEmpty()) {
            return alert("Please enter your first name.");
        }
        if (guestName.getText().trim().isEmpty()) {
            return alert("Please enter your last name.");
        }
        String email = guestEmail.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            return alert("Please enter a valid email address.");
        }
        if (guestPhone.getText().trim().isEmpty()) {
            return alert("Please enter a phone number.");
        }
        return true;
    }

    private boolean alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
        return false;
    }
}