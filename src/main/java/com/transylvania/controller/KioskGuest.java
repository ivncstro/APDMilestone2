package com.transylvania.controller;

import com.transylvania.config.BookingRequest;
import com.transylvania.config.SceneNavigator;
import com.transylvania.config.SceneNavigator.BookingAware;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class KioskGuest implements BookingAware {

    @FXML private Spinner<Integer> adultsSpinner;
    @FXML private Spinner<Integer> childrenSpinner;

    private BookingRequest bookingRequest;

    @Override
    public void setBookingRequest(BookingRequest request) {
        this.bookingRequest = request;
        adultsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10,
                        request.getAdults() > 0 ? request.getAdults() : 2));
        childrenSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10,
                        request.getChildren()));
    }

    @FXML
    private void onNext() {
        bookingRequest.setAdults(adultsSpinner.getValue());
        bookingRequest.setChildren(childrenSpinner.getValue());
        SceneNavigator.goToRoomDetails();
    }

    @FXML
    private void onBack() {
        SceneNavigator.goToDates();
    }
}
