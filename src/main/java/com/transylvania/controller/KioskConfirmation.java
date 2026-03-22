package com.transylvania.controller;

import com.transylvania.model.AddOn;
import com.transylvania.config.BookingRequest;
import com.transylvania.config.SceneNavigator;
import com.transylvania.config.SceneNavigator.BookingAware;
import com.transylvania.config.SceneNavigator.ConfirmationAware;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.stream.Collectors;

public class KioskConfirmation implements BookingAware, ConfirmationAware {

    @FXML private Label confirmationCodeLabel;
    @FXML private Label guestNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label checkInLabel;
    @FXML private Label checkOutLabel;
    @FXML private Label roomLabel;
    @FXML private Label addonsLabel;
    @FXML private Label totalLabel;

    private BookingRequest bookingRequest;
    private String         confirmationCode;

    @Override
    public void setBookingRequest(BookingRequest request) {
        this.bookingRequest = request;
        tryPopulate();
    }

    @Override
    public void setConfirmationCode(String code) {
        this.confirmationCode = code;
        tryPopulate();
    }

    private void tryPopulate() {
        if (bookingRequest == null || confirmationCode == null) return;

        confirmationCodeLabel.setText(confirmationCode);
        guestNameLabel.setText(bookingRequest.getGuestFullName());
        emailLabel.setText(bookingRequest.getEmail());
        checkInLabel.setText(bookingRequest.getCheckIn().toString());
        checkOutLabel.setText(bookingRequest.getCheckOut().toString());
        roomLabel.setText("Room " + bookingRequest.getSelectedRoom().getRoomNumber()
                + "  –  " + bookingRequest.getSelectedRoom().getRoomType().getTypeName());

        String addons = bookingRequest.getSelectedAddOns().isEmpty()
                ? "None"
                : bookingRequest.getSelectedAddOns().keySet().stream()
                .map(AddOn::getName)
                .collect(Collectors.joining(", "));
        addonsLabel.setText(addons);

        double tax = bookingRequest.getGrandTotal() * 0.13;
        totalLabel.setText("$" + String.format("%.2f", bookingRequest.getGrandTotal() + tax));
    }

    @FXML
    private void onDone() {
        SceneNavigator.goToKioskMain();
    }
}
