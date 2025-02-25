package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader;
import io.github.palexdev.materialfx.demo.model.Organizer;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.UserService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private MFXButton signupButton;
    @FXML
    private MFXButton loginButton;
    @FXML
    private MFXPasswordField passwordTextField;
    @FXML
    private MFXTextField userNameTextField;

    @FXML
    public void initialize() {
        // Check if there's an existing session
        if (UserSession.getInstance().isLoggedIn()) {
            handleExistingSession();
        }

        // Add session expiry listener
        UserSession.getInstance().addSessionListener(() -> {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.WARNING, "Session Expired",
                        "Your session has expired. Please login again.");
                handleLogout();
            });
        });
    }

    private void handleExistingSession() {
        String role = UserSession.getInstance().getCurrentUser().getRole();
        if ("Admin".equalsIgnoreCase(role)) {
            navigateToAdminDashboard();
        } else if ("player".equalsIgnoreCase(role)) {
            navigateToClientDashboard();
        }
    }


    @FXML
    public void OnLoginClicked(ActionEvent actionEvent) {
        String email = userNameTextField.getText();
        String password = passwordTextField.getText();

        if (!validateInput(email, password)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Please enter both email and password.");
            return;
        }

        try {
            UserService userService = new UserService();
            User user = userService.login(email, password);

            if (user != null) {
                // Check if the user's account is not active
                if (!user.isActive()) {
                    showAlert(Alert.AlertType.WARNING, "Account Pending Approval",
                            "Your account is not yet approved. Please wait for approval.");
                    clearPasswordField();
                    return;
                }

                // Initialize the session with user data
                UserSession.getInstance().initSession(user);

                // Log success
                System.out.println("Login successful! Welcome, " +
                        UserSession.getInstance().getCurrentUser().getFirstname());

                // Navigate based on role
                String role = UserSession.getInstance().getCurrentUser().getRole();
                if ("Admin".equalsIgnoreCase(role)) {
                    navigateToAdminDashboard();
                } else if ("Organizer".equalsIgnoreCase(role)) {
                    navigateToOrganizerDashboard();
                } else if ("player".equalsIgnoreCase(role)) {
                    navigateToClientDashboard();
                } else {
                    showAlert(Alert.AlertType.WARNING, "Unknown Role",
                            "Unknown role: " + role);
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed",
                        "Invalid email or password.");
                clearPasswordField();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "An error occurred during login. Please try again.");
        }
    }

    private boolean validateInput(String email, String password) {
        return email != null && !email.trim().isEmpty()
                && password != null && !password.trim().isEmpty();
    }

    private void clearPasswordField() {
        passwordTextField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void navigateToAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(MFXDemoResourcesLoader.loadURL("fxml/Demo.fxml"));
            Parent root = loader.load();
            this.rootPane.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to load admin dashboard.");
        }
    }

    private void navigateToClientDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(MFXDemoResourcesLoader.loadURL("fxml/PlayerHome.fxml"));
            Parent root = loader.load();
            this.rootPane.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to load player dashboard.");
        }
    }
    private void navigateToOrganizerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(MFXDemoResourcesLoader.loadURL("fxml/OrganizerHome.fxml"));
            Parent root = loader.load();
            this.rootPane.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to load player dashboard.");
        }
    }

    @FXML
    public void OnSignUpClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Demo.class.getResource("fxml/signup.fxml"));
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

    public void handleLogout() {
        System.out.println("Handling logout...");
        UserSession.getInstance().logout();
        try {
            // Navigate back to login screen
            FXMLLoader loader = new FXMLLoader(Demo.class.getResource("fxml/login.fxml"));
            Parent loginView = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Logout Error",
                    "Error returning to login screen.");
        }
    }

    public void onForgotPasswordClicked(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Demo.class.getResource("fxml/reset_password.fxml"));
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