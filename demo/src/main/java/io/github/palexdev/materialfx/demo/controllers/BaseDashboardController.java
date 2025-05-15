package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.demo.model.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public abstract class BaseDashboardController implements UserSession.SessionListener {

    protected void initialize() {
        // Register for session expiration notifications
        UserSession.getInstance().addSessionListener(this);

        // Verify session on initialization
        if (!UserSession.getInstance().isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Verify required privileges
        if (!checkRequiredPrivileges()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied",
                    "You don't have the required privileges for this page.");
            navigateToLogin();
        }
    }

    // To be implemented by specific dashboard controllers
    protected abstract boolean checkRequiredPrivileges();

    @Override
    public void onSessionExpired() {
        showAlert(Alert.AlertType.WARNING, "Session Expired",
                "Your session has expired. Please log in again.");
        navigateToLogin();
    }

    protected void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml"));
            Parent loginView = loader.load();
            Stage stage = (Stage) getSceneRoot().getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error returning to login screen.");
        }
    }

    protected void showAlert(Alert.AlertType alertType, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // To be implemented by specific dashboard controllers
    protected abstract Parent getSceneRoot();
}
