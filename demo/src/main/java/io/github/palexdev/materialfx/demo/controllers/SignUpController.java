package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.services.UserService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SignUpController {
    @FXML
    private MFXTextField firstNameField;
    @FXML
    private MFXTextField lastNameField;
    @FXML
    private MFXTextField emailField;
    @FXML
    private MFXPasswordField passwordField;
    @FXML
    private MFXPasswordField confirmPasswordField;
    @FXML
    private MFXTextField phoneNumberField;
    @FXML
    private MFXDatePicker dateOfBirthPicker;
    @FXML
    private MFXButton signUpButton;

    private UserService userService;
    @FXML
    private MFXComboBox roleComboBox;
    @FXML
    private Hyperlink loginHyperlink;

    public void initialize() {
        userService = new UserService();
        signUpButton.setOnAction(event -> handleSignUp());
        roleComboBox.setItems(FXCollections.observableArrayList("PLAYER", "ORGANIZER"));
        loginHyperlink.setOnAction(event -> handleHyperlink());
    }

    private void handleSignUp() {
        clearErrors();

        if (!validateFields()) {
            return;
        }

        try {
            User user = createUser();
            userService.create(user);
            showSuccessAndNavigateToLogin();
        } catch (Exception e) {
            handleRegistrationError(e);
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (firstNameField.getText().trim().isEmpty()) {
            showError(firstNameField, "First name is required");
            isValid = false;
        }

        if (lastNameField.getText().trim().isEmpty()) {
            showError(lastNameField, "Last name is required");
            isValid = false;
        }

        if (!isValidEmail(emailField.getText())) {
            showError(emailField, "Invalid email format");
            isValid = false;
        }

        if (passwordField.getText().length() < 6) {
            showError(passwordField, "Password must be at least 6 characters");
            isValid = false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError(confirmPasswordField, "Passwords do not match");
            isValid = false;
        }

        if (!isValidPhoneNumber(phoneNumberField.getText())) {
            showError(phoneNumberField, "Invalid phone number");
            isValid = false;
        }


        if (dateOfBirthPicker.getValue() == null) {
            showError(dateOfBirthPicker, "Date of birth is required");
            isValid = false;
        }
        if (roleComboBox.getValue() == null) {
            showError(roleComboBox, "Role selection is required");
            isValid = false;
        }

        return isValid;
    }

    private User createUser() {
        User user = new User();
        user.setFirstname(firstNameField.getText().trim());
        user.setLastName(lastNameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setPassword(passwordField.getText());
        user.setRole((String) roleComboBox.getValue());
        user.setPhoneNumber(phoneNumberField.getText().trim());
        user.setDateOfBirth(dateOfBirthPicker.getValue());
        user.setProfilePicture("default.png");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }


    private void clearErrors() {
        // Clear any error styling
        firstNameField.setStyle("-fx-border-color: #F8891A;");
        lastNameField.setStyle("-fx-border-color: #F8891A;");
        emailField.setStyle("-fx-border-color: #F8891A;");
        passwordField.setStyle("-fx-border-color: #F8891A;");
        confirmPasswordField.setStyle("-fx-border-color: #F8891A;");
        phoneNumberField.setStyle("-fx-border-color: #F8891A;");
        dateOfBirthPicker.setStyle("-fx-border-color: #F8891A;");
        roleComboBox.setStyle("-fx-border-color: #F8891A;");
    }

    private void showError(Object field, String message) {
        if (field instanceof MFXTextField) {
            ((MFXTextField) field).setStyle("-fx-border-color: red;");
        } else if (field instanceof MFXPasswordField) {
            ((MFXPasswordField) field).setStyle("-fx-border-color: red;");
        } else if (field instanceof MFXDatePicker) {
            ((MFXDatePicker) field).setStyle("-fx-border-color: red;");
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAndNavigateToLogin() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful");
        alert.setHeaderText(null);
        alert.setContentText("Your account has been created successfully!");
        alert.showAndWait();

        handleLogin();
    }

    private void handleRegistrationError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Registration Error");
        alert.setHeaderText(null);
        alert.setContentText("An error occurred during registration. Please try again.");
        e.printStackTrace();
        alert.showAndWait();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }

    private boolean isValidPhoneNumber(String phone) {
        String phoneRegex = "^[0-9]{8}$";  // Assuming 8-digit phone numbers
        return phone != null && phone.matches(phoneRegex);
    }



    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader =  new FXMLLoader(Demo.class.getResource("fxml/login.fxml"));;
            Parent loginView = loader.load();

            Stage stage = (Stage) signUpButton.getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {

        }
    }

    public void handleHyperlink() {
        try {
            FXMLLoader loader =  new FXMLLoader(Demo.class.getResource("fxml/login.fxml"));;
            Parent loginView = loader.load();

            Stage stage = (Stage) signUpButton.getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {

        }
    }
}