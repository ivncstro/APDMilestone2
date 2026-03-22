package com.transylvania.controller;

import com.transylvania.model.AddOn;
import com.transylvania.repository.AddOnRepository;
import com.transylvania.config.BookingRequest;
import com.transylvania.config.SceneNavigator;
import com.transylvania.config.SceneNavigator.BookingAware;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

import java.util.List;
import java.util.Map;

public class KioskAddon implements BookingAware {

    @FXML private CheckBox wifiCheckBox;
    @FXML private CheckBox breakfastCheckBox;
    @FXML private CheckBox parkingCheckBox;
    @FXML private CheckBox spaCheckBox;

    private BookingRequest bookingRequest;
    private Map<CheckBox, AddOn> checkBoxToAddOn;

    private final AddOnRepository addOnRepo = new AddOnRepository();

    @Override
    public void setBookingRequest(BookingRequest request) {
        this.bookingRequest = request;
        wireAddOns();
        restoreSelections();
    }

    private void wireAddOns() {
        List<AddOn> dbAddOns = addOnRepo.findAll();
        checkBoxToAddOn = new java.util.LinkedHashMap<>();

        for (CheckBox cb : List.of(wifiCheckBox, breakfastCheckBox, parkingCheckBox, spaCheckBox)) {
            String keyword = cbKeyword(cb);
            AddOn match = dbAddOns.stream()
                    .filter(a -> a.getName().toLowerCase().contains(keyword))
                    .findFirst()
                    .orElse(null);
            if (match != null) {
                checkBoxToAddOn.put(cb, match);
            }
        }
    }

    private void restoreSelections() {
        checkBoxToAddOn.forEach((cb, addOn) ->
                cb.setSelected(bookingRequest.hasAddOn(addOn)));
    }

    @FXML
    private void onNext() {
        bookingRequest.getSelectedAddOns().clear();
        checkBoxToAddOn.forEach((cb, addOn) -> {
            if (cb.isSelected()) {
                bookingRequest.setAddOnQuantity(addOn, 1);
            }
        });
        SceneNavigator.goToTotal();
    }

    @FXML private void onBack()   { SceneNavigator.goToDetails(); }
    @FXML private void onCancel() { SceneNavigator.goToKioskMain(); }

    private String cbKeyword(CheckBox cb) {
        if (cb == wifiCheckBox)      return "wi-fi";
        if (cb == breakfastCheckBox) return "breakfast";
        if (cb == parkingCheckBox)   return "parking";
        if (cb == spaCheckBox)       return "spa";
        return "";
    }
}