package com.transylvania.controller;

import com.transylvania.model.AddOn;
import com.transylvania.service.ReservationService;
import com.transylvania.config.BookingRequest;
import com.transylvania.config.SceneNavigator;
import com.transylvania.config.SceneNavigator.BookingAware;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.util.Map;
import java.util.StringJoiner;

public class KioskTotal implements BookingAware {

    private static final double TAX_RATE = 0.13; // 13 %

    @FXML private TextArea roomsSummaryArea;
    @FXML private TextArea addonsSummaryArea;
    @FXML private Label    roomChargesLabel;
    @FXML private Label    addonsLabel;
    @FXML private Label    taxLabel;
    @FXML private Label    totalLabel;
    @FXML private Label    loyaltyNumberLabel;

    private BookingRequest         bookingRequest;
    private final ReservationService service = new ReservationService();

    @Override
    public void setBookingRequest(BookingRequest request) {
        this.bookingRequest = request;
        populate();
    }

    private void populate() {
        // rooms
        String roomLine = "Room " + bookingRequest.getSelectedRoom().getRoomNumber()
                + "  –  " + bookingRequest.getSelectedRoom().getRoomType().getTypeName()
                + "\nDates: " + bookingRequest.getCheckIn() + " → " + bookingRequest.getCheckOut()
                + "  (" + bookingRequest.getNights() + " nights)"
                + "\nGuests: " + bookingRequest.getAdults() + " adult(s), "
                + bookingRequest.getChildren() + " child(ren)"
                + "\n$" + String.format("%.2f", bookingRequest.getSelectedRoom()
                .getRoomType().getBasePrice()) + " / night";
        roomsSummaryArea.setText(roomLine);
        roomsSummaryArea.setEditable(false);

        // add ob
        StringJoiner sj = new StringJoiner("\n");
        if (bookingRequest.getSelectedAddOns().isEmpty()) {
            sj.add("None selected");
        } else {
            for (Map.Entry<AddOn, Integer> e : bookingRequest.getSelectedAddOns().entrySet()) {
                boolean perNight = "PER_NIGHT".equalsIgnoreCase(e.getKey().getPricingType());
                double line = e.getKey().getPrice() * e.getValue()
                        * (perNight ? bookingRequest.getNights() : 1);
                sj.add(e.getKey().getName() + " x" + e.getValue()
                        + "  →  $" + String.format("%.2f", line));
            }
        }
        addonsSummaryArea.setText(sj.toString());
        addonsSummaryArea.setEditable(false);

        //cost
        double roomCharges = bookingRequest.getRoomTotal();
        double addonsTotal  = bookingRequest.getAddOnsTotal();
        double subtotal    = roomCharges + addonsTotal;
        double tax         = subtotal * TAX_RATE;
        double grand       = subtotal + tax;

        roomChargesLabel.setText("$" + String.format("%.2f", roomCharges));
        addonsLabel.setText("$"      + String.format("%.2f", addonsTotal));
        taxLabel.setText("$"         + String.format("%.2f", tax));
        totalLabel.setText("$"       + String.format("%.2f", grand));


        loyaltyNumberLabel.setText(bookingRequest.isLoyaltyEnroll() ? "Pending enrolment" : "—");
    }

    @FXML
    private void onConfirm() {
        try {
            String code = service.confirmBooking(bookingRequest);
            SceneNavigator.goToConfirmation(code);
        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText("Booking failed");
            a.setContentText("Something went wrong saving your booking. Please try again.");
            a.showAndWait();
        }
    }

    @FXML private void onBack()   { SceneNavigator.goToAddons(); }
    @FXML private void onCancel() { SceneNavigator.goToKioskMain(); }
}