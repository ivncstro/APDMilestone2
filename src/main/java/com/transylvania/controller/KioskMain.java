package com.transylvania.controller;

import com.transylvania.config.BookingRequest;
import com.transylvania.config.DataSeeder;
import com.transylvania.config.SceneNavigator;
import com.transylvania.config.SceneNavigator.BookingAware;
import com.transylvania.config.DataSeeder;
import javafx.fxml.FXML;

public class KioskMain implements BookingAware {

    @Override
    public void setBookingRequest(BookingRequest request) {}

    @FXML
    private void onStartBooking() {
        DataSeeder.seed();
        SceneNavigator.startNewBooking();
        SceneNavigator.goToDates();
    }

    @FXML
    private void onFeedback() {
        SceneNavigator.goToFeedback();
    }

    @FXML
    private void onAdminLogin() {
        SceneNavigator.goToAdminLogin();
    }

}