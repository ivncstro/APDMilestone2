package com.transylvania.controller;

import com.transylvania.config.LoggerUtil;
import com.transylvania.config.SceneNavigator;
import com.transylvania.config.SecurityUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AdminLoginController {

    private static final String ADMIN_USERNAME = "admin";

    // password: admin [refer to App main file to get hash]
    private static final String ADMIN_HASH =
            "$2a$10$f4roGsJDcz2AgMTX/shR1.pYGK40PgyVq2q3B/53fhgslruegQmKG";

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
        String username = tfUsername.getText() == null ? "" : tfUsername.getText().trim();
        String password = pfPassword.getText() == null ? "" : pfPassword.getText().trim();

        if (username.isBlank() || password.isBlank()) {
            LoggerUtil.logWarning(
                    "admin",
                    "login_attempt",
                    "admin_account",
                    username.isBlank() ? "unknown" : username,
                    "Login failed: missing username or password"
            );

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Missing credentials");
            alert.setContentText("Enter both username and password to continue.");
            alert.showAndWait();
            return;
        }

        boolean validUser = ADMIN_USERNAME.equals(username);
        boolean validPassword = SecurityUtil.checkPassword(password, ADMIN_HASH);

        if (!validUser || !validPassword) {
            LoggerUtil.logWarning(
                    "admin",
                    "login_failed",
                    "admin_account",
                    username,
                    "Login failed: invalid credentials"
            );

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Login Failed");
            alert.setContentText("Invalid username or password.");
            alert.showAndWait();
            return;
        }

        LoggerUtil.logInfo(
                cbRole.getValue() == null ? "admin" : cbRole.getValue().toLowerCase(),
                "login_success",
                "admin_account",
                username,
                "Login successful"
        );

        SceneNavigator.goToAdminDashboard();
    }
}