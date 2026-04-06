package com.transylvania.controller;

import com.transylvania.config.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AdminLoginController {

    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private ComboBox<String> cbRole;

    @FXML
    private void initialize() {
        cbRole.setItems(FXCollections.observableArrayList("Admin", "Manager"));
        cbRole.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleLogin() {
        if (tfUsername.getText() == null || tfUsername.getText().isBlank()
                || pfPassword.getText() == null || pfPassword.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Missing credentials");
            alert.setContentText("Enter both username and password to continue.");
            alert.showAndWait();
            return;
        }

        SceneNavigator.goToAdminDashboard();
    }
}
