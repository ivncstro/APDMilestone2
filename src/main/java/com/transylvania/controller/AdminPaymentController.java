package com.transylvania.controller;

import com.transylvania.config.SceneNavigator;
import com.transylvania.model.*;
import com.transylvania.repository.RoomRepository;
import com.transylvania.service.*;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminPaymentController {

    @FXML private TextField guestSearchField;
    @FXML private TableView<GuestReservationRow> guestTable;
    @FXML private TableColumn<GuestReservationRow, String> guestNameColumn;
    @FXML private TableColumn<GuestReservationRow, String> guestPhoneColumn;
    @FXML private TableColumn<GuestReservationRow, Long> reservationIdColumn;
    @FXML private TableColumn<GuestReservationRow, LocalDate> checkOutColumn;
    @FXML private Label guestNameLabel;
    @FXML private Label reservationIdLabel;
    @FXML private Label roomLabel;
    @FXML private Label checkInLabel;
    @FXML private Label checkOutLabel;
    @FXML private Label statusLabel;
    @FXML private Label roomChargeLabel;
    @FXML private Label addOnsLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private TextField discountField;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private TextField amountField;
    @FXML private TextField referenceField;
    private final ReservationService reservationService = new ReservationService();
    private final DiscountService discountService = new DiscountService();
    private final LoyaltyService loyaltyService = new LoyaltyService();

    private Reservation currentReservation;
    private double currentOriginalTotal = 0.0;
    private double currentDiscountedTotal = 0.0;
    private double currentDiscountPercent = 0.0;

    private final ObservableList<GuestReservationRow> guestData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        guestNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGuestName()));
        guestPhoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));
        reservationIdColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getReservationId()).asObject());
        checkOutColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCheckOutDate()));

        guestTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) loadReservation(newVal.getReservationId());
        });

        paymentMethodCombo.setItems(FXCollections.observableArrayList("Cash", "Credit Card", "Debit Card", "Loyalty Points"));
        paymentMethodCombo.getSelectionModel().selectFirst();

        loadCheckedInGuests();
    }

    private void loadCheckedInGuests() {
        List<Reservation> checkedIn = reservationService.searchReservations(null, "CHECKED_IN", null, null);
        guestData.clear();
        for (Reservation r : checkedIn) guestData.add(new GuestReservationRow(r));
        guestTable.setItems(guestData);
    }

    @FXML
    private void searchGuests() {
        String keyword = guestSearchField.getText().trim();
        List<Reservation> results = keyword.isEmpty()
                ? reservationService.searchReservations(null, "CHECKED_IN", null, null)
                : reservationService.searchReservations(keyword, "CHECKED_IN", null, null);
        guestData.clear();
        for (Reservation r : results) guestData.add(new GuestReservationRow(r));
        guestTable.setItems(guestData);
    }

    private void loadReservation(Long reservationId) {
        currentReservation = reservationService.findById(reservationId);
        if (currentReservation == null) return;

        guestNameLabel.setText(currentReservation.getGuest().getFirstName() + " " + currentReservation.getGuest().getLastName());
        reservationIdLabel.setText(String.valueOf(currentReservation.getReservationId()));
        roomLabel.setText("Room " + currentReservation.getRoom().getRoomNumber() + " (" + currentReservation.getRoom().getRoomType().getTypeName() + ")");
        checkInLabel.setText(currentReservation.getCheckInDate().format(DateTimeFormatter.ISO_DATE));
        checkOutLabel.setText(currentReservation.getCheckOutDate().format(DateTimeFormatter.ISO_DATE));
        statusLabel.setText(currentReservation.getStatus());

        currentOriginalTotal = reservationService.calculateReservationTotal(currentReservation);
        currentDiscountPercent = currentReservation.getDiscountPercent() != null ? currentReservation.getDiscountPercent() : 0.0;
        currentDiscountedTotal = discountService.calculateAfterDiscount(currentReservation, currentDiscountPercent);
        updateBillSummary();
        discountField.setText(String.valueOf(currentDiscountPercent));
    }

    private void updateBillSummary() {
        long nights = java.time.temporal.ChronoUnit.DAYS.between(currentReservation.getCheckInDate(), currentReservation.getCheckOutDate());
        double roomTotal = currentReservation.getRoom().getRoomType().getBasePrice() * nights;
        double addOnsTotal = reservationService.calculateAddOnsTotal(currentReservation);
        double subtotal = roomTotal + addOnsTotal;
        double tax = subtotal * 0.13;
        roomChargeLabel.setText(String.format("$%.2f", roomTotal));
        addOnsLabel.setText(String.format("$%.2f", addOnsTotal));
        subtotalLabel.setText(String.format("$%.2f", subtotal));
        taxLabel.setText(String.format("$%.2f", tax));
        double afterDiscount = discountService.calculateAfterDiscount(currentReservation, currentDiscountPercent);
        totalLabel.setText(String.format("$%.2f", afterDiscount));
        currentDiscountedTotal = afterDiscount;
    }

    @FXML
    private void applyDiscount() {
        if (currentReservation == null) { showAlert("No reservation", "Select a reservation first."); return; }
        try {
            double percent = Double.parseDouble(discountField.getText().trim());
            String role = AdminSession.getRole();
            if (role == null) role = "ADMIN";
            double maxCap = "MANAGER".equals(role) ? 30.0 : 15.0;
            if (percent > maxCap) { showAlert("Discount limit exceeded", String.format("%s can apply up to %.0f%%.", role, maxCap)); return; }
            double newTotal = discountService.applyDiscount(currentReservation, percent);
            currentDiscountPercent = percent;
            currentDiscountedTotal = newTotal;
            totalLabel.setText(String.format("$%.2f", newTotal));
            showAlert("Success", String.format("%.0f%% discount applied.", percent));
        } catch (Exception e) { showAlert("Error", e.getMessage()); }
    }

    @FXML
    private void setFullAmount() {
        if (currentReservation != null) amountField.setText(String.format("%.2f", currentDiscountedTotal));
    }

    @FXML
    private void useAllPoints() {
        if (currentReservation == null) { showAlert("No reservation", "Select a reservation first."); return; }
        LoyaltyMember member = loyaltyService.findByGuest(currentReservation.getGuest());
        if (member == null) { showAlert("Not enrolled", "Guest is not a loyalty member."); return; }
        int availablePoints = member.getAvailablePoints();
        if (availablePoints <= 0) { showAlert("No points", "Guest has no available points."); return; }
        double maxDiscountValue = currentDiscountedTotal;
        double discount = loyaltyService.redeemPoints(member.getId(), availablePoints, maxDiscountValue);
        if (discount > 0) {
            currentDiscountedTotal -= discount;
            totalLabel.setText(String.format("$%.2f", currentDiscountedTotal));
            showAlert("Points Redeemed", String.format("Used %d points for $%.2f. Remaining balance: $%.2f",
                    (int)(discount * LoyaltyService.POINTS_PER_DOLLAR), discount, currentDiscountedTotal));
            amountField.clear();
        } else {
            showAlert("Redeem failed", "Could not redeem points.");
        }
    }

    @FXML
    private void recordPayment() {
        if (currentReservation == null) { showAlert("No reservation", "Select a reservation first."); return; }
        try {
            String method = paymentMethodCombo.getValue();
            double amount = 0;
            if (!"Loyalty Points".equals(method)) {
                amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
                if (amount > currentDiscountedTotal) { showAlert("Overpayment", "Amount exceeds balance."); return; }
                currentDiscountedTotal -= amount;
                totalLabel.setText(String.format("$%.2f", currentDiscountedTotal));
                showAlert("Payment recorded", String.format("%s payment of $%.2f recorded. Remaining: $%.2f", method, amount, currentDiscountedTotal));
                LoyaltyMember member = loyaltyService.findByGuest(currentReservation.getGuest());
                if (member != null) {
                    loyaltyService.addPoints(member.getId(), amount);
                    showAlert("Loyalty Points", "Earned " + (int)(amount * LoyaltyService.POINTS_PER_DOLLAR) + " points.");
                } else {
                    System.out.println("No loyalty member found for guest: " + currentReservation.getGuest().getGuestId());
                }
            } else {
                if (amountField.getText().trim().isEmpty()) {
                    useAllPoints();
                } else {
                    double pointsAmount = Double.parseDouble(amountField.getText().trim());
                    int pointsNeeded = (int)(pointsAmount * LoyaltyService.POINTS_PER_DOLLAR);
                    LoyaltyMember member = loyaltyService.findByGuest(currentReservation.getGuest());
                    if (member == null) { showAlert("Not enrolled", "Guest not a loyalty member."); return; }
                    double discount = loyaltyService.redeemPoints(member.getId(), pointsNeeded, pointsAmount);
                    if (discount < pointsAmount - 0.01) { showAlert("Insufficient points", "Not enough points."); return; }
                    currentDiscountedTotal -= discount;
                    totalLabel.setText(String.format("$%.2f", currentDiscountedTotal));
                    showAlert("Points Redeemed", String.format("Redeemed %d points for $%.2f. Remaining: $%.2f", pointsNeeded, discount, currentDiscountedTotal));
                }
            }
            amountField.clear();
            referenceField.clear();
        } catch (NumberFormatException e) { showAlert("Invalid amount", "Enter a valid number."); }
        catch (Exception e) { showAlert("Payment error", e.getMessage()); }
    }

    @FXML
    private void checkout() {
        if (currentReservation == null) { showAlert("No reservation", "Select a reservation first."); return; }
        if (currentDiscountedTotal > 0.01) {
            LoyaltyMember member = loyaltyService.findByGuest(currentReservation.getGuest());
            if (member != null && member.getAvailablePoints() > 0) {
                Alert askPoints = new Alert(Alert.AlertType.CONFIRMATION);
                askPoints.setTitle("Loyalty Points Available");
                askPoints.setHeaderText("Guest has " + member.getAvailablePoints() + " points available.");
                askPoints.setContentText("Would you like to apply all points to reduce the balance?");
                ButtonType yesBtn = new ButtonType("Yes, Use All Points");
                ButtonType noBtn = new ButtonType("No, Proceed with Remaining Balance");
                askPoints.getButtonTypes().setAll(yesBtn, noBtn, ButtonType.CANCEL);
                if (askPoints.showAndWait().orElse(ButtonType.CANCEL) == yesBtn) {
                    useAllPoints();
                }
            }
            if (currentDiscountedTotal > 0.01) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setHeaderText("Outstanding balance");
                confirm.setContentText(String.format("Remaining balance: $%.2f. Continue checkout?", currentDiscountedTotal));
                if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
            }
        }
        currentReservation.setStatus("CHECKED_OUT");
        reservationService.saveOrUpdate(currentReservation);
        Room room = currentReservation.getRoom();
        new RoomRepository().updateRoomStatus(room, "AVAILABLE");
        Alert reminder = new Alert(Alert.AlertType.INFORMATION);
        reminder.setTitle("Checkout Complete");
        reminder.setHeaderText("Guest checked out successfully.");
        reminder.setContentText("Please invite the guest to submit feedback at the kiosk.");
        reminder.show();
        currentReservation = null;
        clearReservationDetails();
        loadCheckedInGuests();
    }

    private void clearReservationDetails() {
        guestNameLabel.setText("-");
        reservationIdLabel.setText("-");
        roomLabel.setText("-");
        checkInLabel.setText("-");
        checkOutLabel.setText("-");
        statusLabel.setText("-");
        roomChargeLabel.setText("$0.00");
        addOnsLabel.setText("$0.00");
        subtotalLabel.setText("$0.00");
        taxLabel.setText("$0.00");
        totalLabel.setText("$0.00");
        discountField.clear();
        amountField.clear();
        referenceField.clear();
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

    public static class GuestReservationRow {
        private final Long reservationId;
        private final String guestName;
        private final String phone;
        private final LocalDate checkOutDate;
        public GuestReservationRow(Reservation r) {
            this.reservationId = r.getReservationId();
            this.guestName = r.getGuest().getFirstName() + " " + r.getGuest().getLastName();
            this.phone = r.getGuest().getPhone();
            this.checkOutDate = r.getCheckOutDate();
        }
        public Long getReservationId() { return reservationId; }
        public String getGuestName() { return guestName; }
        public String getPhone() { return phone; }
        public LocalDate getCheckOutDate() { return checkOutDate; }
    }
}