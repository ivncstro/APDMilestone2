package com.transylvania.controller;

import com.transylvania.config.SceneNavigator;
import com.transylvania.service.AdminSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.transylvania.config.LoggerUtil;


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

            // log failed login attempt
            LoggerUtil.logWarning(
                    "admin",
                    "login_attempt",
                    "admin_account",
                    tfUsername.getText() == null || tfUsername.getText().isBlank() ? "unknown" : tfUsername.getText(),
                    "Login failed: missing username or password"
            );

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Missing credentials");
            alert.setContentText("Enter both username and password to continue.");
            alert.showAndWait();
            return;
        }
        AdminSession.login("ADMIN");
        // temp log info for log attempt
        LoggerUtil.logInfo(
                cbRole.getValue() == null ? "admin" : cbRole.getValue().toLowerCase(),
                "login_success",
                "admin_account",
                tfUsername.getText().trim(),
                "Login successful"
        );

        SceneNavigator.goToAdminDashboard();
    }
}
