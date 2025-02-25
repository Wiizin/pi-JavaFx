package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.materialfx.demo.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class ResetPasswordController {
    @javafx.fxml.FXML
    private MFXButton submitButton;
    @javafx.fxml.FXML
    private Pane rootPane;
    @javafx.fxml.FXML
    private MFXTextField emailTextField;
    private UserService userService = new UserService();

    @javafx.fxml.FXML
    public void onSubmitEmail(ActionEvent actionEvent) throws IOException {
        String email = emailTextField.getText().trim();

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Email field cannot be empty!");
            return;
        }

        boolean success = userService.requestPasswordReset(email);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "A reset code has been sent to your email.");

            // Load the code verification screen
            FXMLLoader loader = new FXMLLoader(Demo.class.getResource("fxml/reset_password_code.fxml"));
            Parent resetCodeView = loader.load();

            // Get the controller and pass the email
            ResetPasswordCodeController codeController = loader.getController();
            codeController.setUserEmail(email);  // Add this method to your code controller

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(resetCodeView);
            stage.setScene(scene);
            stage.show();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to send reset code. Please check your email.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @javafx.fxml.FXML
    public void onBackToLoginClicked(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Demo.class.getResource("fxml/login.fxml"));
            Parent signUpView = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(signUpView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to load signup page.");
        }

    }



    }

