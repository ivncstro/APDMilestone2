package com.transylvania.controller;

import com.transylvania.config.SceneNavigator;
import com.transylvania.model.Discount;
import com.transylvania.repository.DiscountRepository;
import com.transylvania.service.AdminSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;

public class AdminDiscountsController {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField valueField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private CheckBox singleCheck, doubleCheck, deluxeCheck, penthouseCheck;
    @FXML private TextArea descriptionArea;
    @FXML private TableView<Discount> discountTable;
    @FXML private TableColumn<Discount, String> nameColumn;
    @FXML private TableColumn<Discount, String> valueColumn;
    @FXML private TableColumn<Discount, LocalDate> startColumn;
    @FXML private TableColumn<Discount, LocalDate> endColumn;
    @FXML private TableColumn<Discount, String> roomsColumn;
    @FXML private TableColumn<Discount, String> statusColumn;
    @FXML private TableColumn<Discount, String> createdByColumn;
    @FXML private TableColumn<Discount, Void> actionsColumn;

    private final DiscountRepository discountRepository = new DiscountRepository();

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("Percentage", "Fixed Amount"));
        typeCombo.getSelectionModel().selectFirst();

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getValue() + (cell.getValue().getType().equals("Percentage") ? "%" : "$")));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        roomsColumn.setCellValueFactory(new PropertyValueFactory<>("applicableRoomTypes"));
        statusColumn.setCellValueFactory(cell -> {
            LocalDate now = LocalDate.now();
            Discount d = cell.getValue();
            String status = (d.getStartDate() != null && d.getStartDate().isAfter(now)) ? "Upcoming" :
                    (d.getEndDate() != null && d.getEndDate().isBefore(now)) ? "Expired" : "Active";
            return new SimpleStringProperty(status);
        });
        createdByColumn.setCellValueFactory(new PropertyValueFactory<>("createdByRole"));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setOnAction(e -> {
                    Discount d = getTableView().getItems().get(getIndex());
                    deleteDiscount(d);
                });
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        loadDiscounts();
    }

    private void loadDiscounts() {
        List<Discount> discounts = discountRepository.findAll();
        discountTable.setItems(FXCollections.observableArrayList(discounts));
    }

    @FXML
    private void saveDiscount() {
        try {
            Discount discount = new Discount();
            discount.setName(nameField.getText());
            discount.setType(typeCombo.getValue().equals("Percentage") ? "PERCENTAGE" : "FIXED");
            discount.setValue(Double.parseDouble(valueField.getText()));
            discount.setStartDate(startDatePicker.getValue());
            discount.setEndDate(endDatePicker.getValue());

            StringBuilder rooms = new StringBuilder();
            if (singleCheck.isSelected()) rooms.append("Single,");
            if (doubleCheck.isSelected()) rooms.append("Double,");
            if (deluxeCheck.isSelected()) rooms.append("Deluxe,");
            if (penthouseCheck.isSelected()) rooms.append("Penthouse,");
            if (rooms.length() > 0) rooms.setLength(rooms.length() - 1);
            discount.setApplicableRoomTypes(rooms.toString());

            discount.setDescription(descriptionArea.getText());
            discount.setCreatedByRole(AdminSession.getRole());

            discountRepository.save(discount);
            clearForm();
            loadDiscounts();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Discount saved.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void deleteDiscount(Discount discount) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Delete discount '" + discount.getName() + "'?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            discountRepository.deleteById(discount.getId());
            loadDiscounts();
        }
    }

    private void clearForm() {
        nameField.clear();
        typeCombo.getSelectionModel().selectFirst();
        valueField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        singleCheck.setSelected(false);
        doubleCheck.setSelected(false);
        deluxeCheck.setSelected(false);
        penthouseCheck.setSelected(false);
        descriptionArea.clear();
    }

    @FXML private void cancel() { clearForm(); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
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
    @FXML private void goToFeedback() { SceneNavigator.goToAdminFeedback(); }
    @FXML private void logout() { SceneNavigator.goToAdminLogin(); }
}