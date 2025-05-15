package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.demo.Demo;

import io.github.palexdev.materialfx.demo.model.Organizer;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.GoogleAuthService;
import io.github.palexdev.materialfx.demo.services.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class GoogleRegistrationController implements Initializable {
    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label emailLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private MFXComboBox<String> roleComboBox;
    @FXML
    private MFXTextField phoneNumberField;
    @FXML
    private DatePicker dateOfBirthPicker;
    @FXML
    private VBox organizerFields;
    @FXML
    private MFXTextField coachingLicenseField;
    @FXML
    private MFXButton registerButton;
    @FXML
    private MFXButton cancelButton;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get Google user info from session
        GoogleAuthService.UserInfo userInfo = UserSession.getInstance().getTempUserInfo();


        if (userInfo == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No Google user information found.");
            navigateToLogin();
            return;
        }

        // Populate fields with Google info
        emailLabel.setText(userInfo.getEmail());
        nameLabel.setText(userInfo.getName());

        // Setup role combo box
        roleComboBox.getItems().addAll("Player", "Organizer");
        roleComboBox.selectFirst();

        // Show/hide organizer fields based on role selection
        roleComboBox.setOnAction(event -> {
            String selectedRole = roleComboBox.getValue();
            organizerFields.setVisible("Organizer".equals(selectedRole));
            organizerFields.setManaged("Organizer".equals(selectedRole));
        });

        // Set today's date as default
        dateOfBirthPicker.setValue(LocalDate.now());

        // Initially hide organizer fields
        organizerFields.setVisible(false);
        organizerFields.setManaged(false);

        // Setup button actions
        registerButton.setOnAction(event -> handleRegistration());
        cancelButton.setOnAction(event -> navigateToLogin());
    }

    private void handleRegistration() {
        // Validate input fields
        if (!validateInputs()) {
            return;
        }

        try {
            UserService userService = new UserService();

            // Get Google user info from session
            GoogleAuthService.UserInfo userInfo = UserSession.getInstance().getTempUserInfo();

            // Check if email is already registered
            if (!userService.isEmailUnique(userInfo.getEmail())) {
                showAlert(Alert.AlertType.ERROR, "Registration Error",
                        "This email is already registered. Please log in instead.");
                return;
            }

            // Get role selection
            String selectedRole = roleComboBox.getValue().toLowerCase();

            // Create appropriate user object
            User user;
            if ("organizer".equals(selectedRole)) {
                Organizer organizer = new Organizer();
                organizer.setCoachingLicense(coachingLicenseField.getText());
                organizer.setActive(false); // Needs admin approval
                user = organizer;
            } else {
                user = new User();
                user.setActive(true); // Players are active by default
            }

            // Set common user properties
            // Instead of splitting name, use firstName and lastName from userInfo
            user.setFirstname(userInfo.getFirstName());
            user.setLastName(userInfo.getLastName());
            user.setEmail(userInfo.getEmail());
            user.setPassword(null); // No password for Google users
            user.setRole(selectedRole);
            user.setPhoneNumber(phoneNumberField.getText());
            user.setDateOfBirth(dateOfBirthPicker.getValue());
            user.setProfilePicture(userInfo.getPictureUrl());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setIdteam(0);

            // Save user to database
            userService.create(user);

            // Show success message
            if ("organizer".equals(selectedRole)) {
                showAlert(Alert.AlertType.INFORMATION, "Registration Successful",
                        "Your organizer account has been created but needs admin approval. You'll be notified when approved.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Registration Successful",
                        "Your account has been created successfully. You can now log in.");
            }

            // Clear temporary user info from session
            UserSession.getInstance().clearTempUserInfo();

            // Navigate to login page
            navigateToLogin();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Registration Error",
                    "An error occurred during registration: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        // Check date of birth is selected and not in the future
        if (dateOfBirthPicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select your date of birth.");
            return false;
        }

        if (dateOfBirthPicker.getValue().isAfter(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Date of birth cannot be in the future.");
            return false;
        }

        // If organizer role is selected, check coaching license
        if ("Organizer".equals(roleComboBox.getValue())) {
            String license = coachingLicenseField.getText();
            if (license == null || license.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter your coaching license.");
                return false;
            }
        }

        return true;
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(Demo.class.getResource("fxml/login.fxml"));
            Parent loginView = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to login page.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
