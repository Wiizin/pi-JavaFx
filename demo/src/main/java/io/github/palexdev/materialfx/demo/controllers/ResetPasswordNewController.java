package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.materialfx.demo.services.UserService;
import javafx.fxml.FXML;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class ResetPasswordNewController {

    public javafx.scene.layout.Pane Pane;
    @FXML
    private MFXPasswordField newPasswordField;

    @FXML
    private MFXPasswordField confirmPasswordField;

    private String resetCode;

    @FXML
    private MFXButton savePasswordButton;
    private final UserService userService = new UserService();

    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    @FXML
    private void onSaveNewPassword() throws IOException {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Fields cannot be empty");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match");
            return;
        }

        // Call the backend service to update the password
        boolean success = userService.updatePassword(resetCode, newPassword);

        if (success) {
            showAlert("Success", "Password has been reset successfully");
            FXMLLoader loader = new FXMLLoader(
                    Demo.class.getResource("fxml/login.fxml"));
            Parent signUpView = loader.load();

            Stage stage = (Stage) Pane.getScene().getWindow();
            Scene scene = new Scene(signUpView);
            stage.setScene(scene);
            stage.show();




        } else {
            showAlert("Error", "Failed to reset password");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }



}
