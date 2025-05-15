package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader;
import io.github.palexdev.materialfx.demo.model.Organizer;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.GoogleAuthService;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Map;

public class LoginController {
    public Label statusLabel;
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
    private MFXButton googleLoginButton;
    private GoogleAuthService googleAuthService;
    private MFXGenericDialog dialogContent;
    private MFXStageDialog dialog;


    @FXML
    public void initialize() {
        try {
            // Initialize the GoogleAuthService
            googleAuthService = new GoogleAuthService();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            // Handle the error using MFX dialog
            Platform.runLater(() -> {
                showMaterialFXAlert("Initialization Error",
                        "Failed to initialize GoogleAuthService: " + e.getMessage(),
                        "fas-circle-xmark", "mfx-error-dialog");
            });
        }


        // Initialize MFX dialog components
        dialogContent = new MFXGenericDialog();
        dialog = new MFXStageDialog();
        dialog.setContent(dialogContent);
        dialog.setOwnerNode(rootPane); // Set the owner node for the dialog

        googleLoginButton.setOnAction(event -> handleGoogleLogin());


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
            showMaterialFXAlert("Error", "Invalid email or password", "fas-triangle-exclamation", "mfx-error-dialog");
            return;
        }

        try {
            UserService userService = new UserService();
            User user = userService.login(email, password);

            if (user != null) {
                // Check if the user's account is not active
                if (!user.isActive()) {
                    showMaterialFXAlert("Error", "Account Pending Approval Your account is not yet approved" +
                            " Please wait for approval.", "fas-triangle-exclamation", "mfx-error-dialog");
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

    @FXML
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

    @FXML
    public void handleGoogleLogin() {
        // Disable the login button to prevent multiple clicks
        googleLoginButton.setDisable(true);

        // Start the OAuth flow on a background thread
        googleAuthService.startOAuthFlowAsync().thenAccept(userInfo -> {
            Platform.runLater(() -> {
                try {
                    // No need to extract fields and create GoogleUserInfo
                    // Just use the userInfo directly

                    // Initialize UserService
                    UserService userService = new UserService();

                    // First check if this Google user already exists
                    User user = userService.findUserByEmail(userInfo.getEmail());

                    if (user == null) {
                        // New Google user - navigate to registration page
                        UserSession.getInstance().setTempUserInfo(userInfo);
                        navigateToGoogleRegistration();
                        System.out.println("Login successful! Welcome, " +
                                UserSession.getInstance().getCurrentUser().getFirstname());
                        return;
                    }

                    // Rest of the code remains the same
                    // Existing user - check if account is active
                    if (!user.isActive()) {
                        showAlert(Alert.AlertType.WARNING, "Account Pending Approval",
                                "Your account is not yet approved. Please wait for approval.");
                        googleLoginButton.setDisable(false);
                        return;
                    }

                    // Initialize the session with user data
                    UserSession.getInstance().initSession(user);
                    System.out.println("Login successful! Welcome, " +
                            UserSession.getInstance().getCurrentUser().getFirstname());

                    // Navigate based on role
                    String role = user.getRole();
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

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error",
                            "Could not process login: " + e.getMessage());
                    googleLoginButton.setDisable(false);
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Login Failed",
                        "An error occurred during Google login: " + e.getMessage());
                googleLoginButton.setDisable(false);
            });
            return null;
        });
    }

    private void navigateToGoogleRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Demo.class.getResource("fxml/google_registration.fxml"));
            Parent registerView = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(registerView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to load registration page.");
        }
    }













    private void showMaterialFXAlert(String title, String message, String iconCode, String styleClass) {
        // Ensure the correct icon codes are used with MaterialFX FontAwesome
        String updatedIconCode;
        switch (iconCode) {
            case "fas-check-circle":
                updatedIconCode = "fas-circle-check";
                break;
            case "fas-circle-exclamation":
                updatedIconCode = "fas-triangle-exclamation";
                break;
            case "fas-circle-xmark":
                updatedIconCode = "fas-circle-x";
                break;
            default:
                updatedIconCode = "fas-circle-info";
        }

        // Create the icon with a clear size
        MFXFontIcon icon = new MFXFontIcon(updatedIconCode, 18);

        // Ensure UI updates happen on JavaFX Application Thread
        Platform.runLater(() -> {
            // Ensure dialog content is initialized
            if (dialogContent == null || dialog == null) {
                System.err.println("Dialog components not properly initialized");
                return;
            }

            // Clear any existing actions


            // Reset style classes
            dialogContent.getStyleClass().removeIf(
                    s -> s.equals("mfx-info-dialog") ||
                            s.equals("mfx-warn-dialog") ||
                            s.equals("mfx-error-dialog")
            );

            // Set content and icon
            dialogContent.setHeaderText(title);
            dialogContent.setContentText(message);
            dialogContent.setHeaderIcon(icon);

            // Add close action
            dialogContent.addActions(
                    Map.entry(new MFXButton("OK"), event -> dialog.close())
            );

            // Add style class
            if (styleClass != null) {
                dialogContent.getStyleClass().add(styleClass);
            }

            // Show the dialog
            dialog.showDialog();
        });
    }







}
