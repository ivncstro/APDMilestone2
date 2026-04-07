package com.transylvania.controller;

import com.transylvania.config.SceneNavigator;
import com.transylvania.model.LoyaltyMember;
import com.transylvania.service.LoyaltyService;
import com.transylvania.service.ReservationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.time.LocalDate;
import java.util.List;

public class AdminLoyaltyController {

    @FXML private TableView<LoyaltyRow> loyaltyTable;
    @FXML private TableColumn<LoyaltyRow, String> guestNameColumn;
    @FXML private TableColumn<LoyaltyRow, String> loyaltyNumberColumn;
    @FXML private TableColumn<LoyaltyRow, Integer> pointsColumn;
    @FXML private TableColumn<LoyaltyRow, Integer> redeemedColumn;
    @FXML private TableColumn<LoyaltyRow, Void> actionColumn;
    @FXML private TextField searchField;

    private final LoyaltyService loyaltyService = new LoyaltyService();
    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        guestNameColumn.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        loyaltyNumberColumn.setCellValueFactory(new PropertyValueFactory<>("loyaltyNumber"));
        pointsColumn.setCellValueFactory(new PropertyValueFactory<>("points"));
        redeemedColumn.setCellValueFactory(new PropertyValueFactory<>("redeemed"));

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Label statusLabel = new Label();
            private final Button adjustBtn = new Button("Adjust Points");
            private final HBox container = new HBox(10, statusLabel, adjustBtn);
            {
                adjustBtn.setOnAction(e -> {
                    LoyaltyRow row = getTableView().getItems().get(getIndex());
                    adjustPoints(row);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                LoyaltyRow row = getTableView().getItems().get(getIndex());
                statusLabel.setText(row.isCheckedIn() ? "✓ Checked In" : "✗ Not Checked In");
                statusLabel.setStyle(row.isCheckedIn() ? "-fx-text-fill: #2ecc71;" : "-fx-text-fill: #e74c3c;");
                setGraphic(container);
            }
        });

        refreshTable();
    }

    private void refreshTable() {
        List<LoyaltyMember> members = loyaltyService.findAll();
        loyaltyTable.setItems(FXCollections.observableArrayList(
                members.stream().map(this::toRow).toList()
        ));
    }

    private LoyaltyRow toRow(LoyaltyMember m) {
        boolean checkedIn = reservationService.searchReservations(null, "CHECKED_IN", null, null)
                .stream().anyMatch(r -> r.getGuest().getGuestId().equals(m.getGuest().getGuestId())
                        && r.getCheckOutDate().isAfter(LocalDate.now()));
        return new LoyaltyRow(m.getId(), m.getGuest().getFirstName() + " " + m.getGuest().getLastName(),
                m.getLoyaltyNumber(), m.getPoints(), m.getRedeemedPoints(), m.getAvailablePoints(), checkedIn);
    }

    private void adjustPoints(LoyaltyRow row) {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Adjust Points");
        dialog.setHeaderText("Guest: " + row.getGuestName());
        dialog.setContentText("Enter points to add (+ points) or redeem (- points):");
        dialog.showAndWait().ifPresent(input -> {
            try {
                int delta = Integer.parseInt(input);
                if (delta > 0) {
                    double equivalentAmount = (double) delta / LoyaltyService.POINTS_PER_DOLLAR;
                    loyaltyService.addPoints(row.getMemberId(), equivalentAmount);
                    showAlert("Success", delta + " points added.");
                } else if (delta < 0) {
                    int pointsToRedeem = -delta;
                    double discount = loyaltyService.redeemPoints(row.getMemberId(), pointsToRedeem, 500.0);
                    showAlert("Redeemed", "Discount: $" + String.format("%.2f", discount));
                }
                refreshTable();
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid number");
            }
        });
    }

    @FXML
    private void onSearch() {
        String kw = searchField.getText().trim();
        List<LoyaltyMember> members = kw.isEmpty() ? loyaltyService.findAll() : loyaltyService.search(kw);
        loyaltyTable.setItems(FXCollections.observableArrayList(
                members.stream().map(this::toRow).toList()
        ));
    }

    @FXML private void onAdvancedSearch() { onSearch(); }

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

    public static class LoyaltyRow {
        private final Long memberId;
        private final String guestName;
        private final String loyaltyNumber;
        private final int points;
        private final int redeemed;
        private final int balance;
        private final boolean checkedIn;

        public LoyaltyRow(Long memberId, String guestName, String loyaltyNumber,
                          int points, int redeemed, int balance, boolean checkedIn) {
            this.memberId = memberId;
            this.guestName = guestName;
            this.loyaltyNumber = loyaltyNumber;
            this.points = points;
            this.redeemed = redeemed;
            this.balance = balance;
            this.checkedIn = checkedIn;
        }

        public Long getMemberId() { return memberId; }
        public String getGuestName() { return guestName; }
        public String getLoyaltyNumber() { return loyaltyNumber; }
        public int getPoints() { return points; }
        public int getRedeemed() { return redeemed; }
        public int getBalance() { return balance; }
        public boolean isCheckedIn() { return checkedIn; }
    }
}