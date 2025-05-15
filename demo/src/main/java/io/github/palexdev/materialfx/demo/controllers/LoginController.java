package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import io.github.palexdev.materialfx.demo.model.Organizer;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.GoogleAuthService;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
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
        System.out.println("Login button clicked!");
        String email = userNameTextField.getText();
        String password = passwordTextField.getText();
        
        System.out.println("Email: " + email + ", Password length: " + (password != null ? password.length() : 0));

        if (!validateInput(email, password)) {
            System.out.println("Invalid input detected");
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid email or password");
            return;
        }
        
        // FOR TESTING: Hardcoded login credentials
        if (email.equals("admin@admin.com") && password.equals("admin")) {
            System.out.println("Using hardcoded admin login");
            User adminUser = new User();
            adminUser.setId(1);
            adminUser.setFirstname("Admin");
            adminUser.setLastName("User");
            adminUser.setEmail("admin@admin.com");
            adminUser.setRole("Admin");
            adminUser.setActive(true);
            
            UserSession.getInstance().initSession(adminUser);
            navigateToAdminDashboard();
            return;
        } else if (email.equals("player@player.com") && password.equals("player")) {
            System.out.println("Using hardcoded player login");
            User playerUser = new User();
            playerUser.setId(2);
            playerUser.setFirstname("Player");
            playerUser.setLastName("User");
            playerUser.setEmail("player@player.com");
            playerUser.setRole("player");
            playerUser.setActive(true);
            
            UserSession.getInstance().initSession(playerUser);
            navigateToClientDashboard();
            return;
        } else if (email.equals("organizer@organizer.com") && password.equals("organizer")) {
            System.out.println("Using hardcoded organizer login");
            Organizer organizerUser = new Organizer();
            organizerUser.setId(3);
            organizerUser.setFirstname("Organizer");
            organizerUser.setLastName("User");
            organizerUser.setEmail("organizer@organizer.com");
            organizerUser.setRole("Organizer");
            organizerUser.setActive(true);
            organizerUser.setCoachingLicense("LICENSE-123");
            
            UserSession.getInstance().initSession(organizerUser);
            navigateToOrganizerDashboard();
            return;
        }

        // If not using hardcoded login, proceed with database login
        try {
            System.out.println("Attempting database login");
            UserService userService = new UserService();
            User user = userService.login(email, password);

            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password");
                clearPasswordField();
                return;
            }

            if (!user.isActive()) {
                showAlert(Alert.AlertType.WARNING, "Account Pending Approval",
                        "Your account is not yet approved. Please wait for approval.");
                clearPasswordField();
                return;
            }

            UserSession.getInstance().initSession(user);
            System.out.println("Login successful! Welcome, " + user.getFirstname());

            String role = user.getRole();
            if ("Admin".equalsIgnoreCase(role)) {
                navigateToAdminHome();
            } else if ("Organizer".equalsIgnoreCase(role)) {
                navigateToOrganizerHome();
            } else if ("player".equalsIgnoreCase(role)) {
                navigateToPlayerHome();
            } else {
                showAlert(Alert.AlertType.WARNING, "Unknown Role", "Unknown role: " + role);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not process login: " + e.getMessage());
        }
    }

    private boolean validateInput(String email, String password) {
        return email != null && !email.trim().isEmpty() && password != null && !password.trim().isEmpty();
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

    /**
     * Navigate to the admin's home page after successful login
     */
    private void navigateToAdminHome() {
        try {
            System.out.println("Navigating to admin home page");
            // Use a direct path to the resource instead of the resource loader
            String fxmlPath = "/io/github/palexdev/materialfx/demo/fxml/admin_home.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            
            if (fxmlUrl == null) {
                System.err.println("Could not find admin_home.fxml at path: " + fxmlPath);
                // Try to find the FXML file and report its actual location
                findAndReportFxmlLocation("admin_home.fxml");
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not find admin_home.fxml resource");
                return;
            }
            
            System.out.println("Found admin_home.fxml at: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            String cssPath = "/io/github/palexdev/materialfx/demo/css/Stylesheet.css";
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Could not find Stylesheet.css at path: " + cssPath);
            }
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to admin home: " + e.getMessage());
        }
    }

    /**
     * Old method kept for reference - navigates directly to the admin dashboard
     * @deprecated Use navigateToAdminHome() instead
     */
    private void navigateToAdminDashboard() {
        try {
            System.out.println("Navigating to admin dashboard");
            // Use a direct path to the resource instead of the resource loader
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/palexdev/materialfx/demo/fxml/Dashboard.fxml"));
            
            if (loader.getLocation() == null) {
                System.err.println("Could not find Dashboard.fxml");
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not find Dashboard.fxml resource");
                return;
            }
            
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/io/github/palexdev/materialfx/demo/css/Stylesheet.css").toExternalForm());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to dashboard: " + e.getMessage());
        }
    }

    /**
     * Navigate to the player's home page after successful login
     */
    private void navigateToPlayerHome() {
        try {
            System.out.println("Navigating to player home page");
            // Use a direct path to the resource instead of the resource loader
            String fxmlPath = "/io/github/palexdev/materialfx/demo/fxml/PlayerHome.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            
            if (fxmlUrl == null) {
                System.err.println("Could not find PlayerHome.fxml at path: " + fxmlPath);
                // Try to find the FXML file and report its actual location
                findAndReportFxmlLocation("PlayerHome.fxml");
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not find PlayerHome.fxml resource");
                return;
            }
            
            System.out.println("Found PlayerHome.fxml at: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            String cssPath = "/io/github/palexdev/materialfx/demo/css/Stylesheet.css";
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Could not find Stylesheet.css at path: " + cssPath);
            }
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to player home: " + e.getMessage());
        }
    }
    
    /**
     * Old method kept for reference - navigates directly to the store
     * @deprecated Use navigateToPlayerHome() instead
     */
    private void navigateToClientDashboard() {
        try {
            System.out.println("Navigating to client/player dashboard (store)");
            // Use a direct path to the resource instead of the resource loader
            String fxmlPath = "/io/github/palexdev/materialfx/demo/fxml/Store.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            
            if (fxmlUrl == null) {
                System.err.println("Could not find Store.fxml at path: " + fxmlPath);
                // Try to find the FXML file and report its actual location
                findAndReportFxmlLocation("Store.fxml");
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not find Store.fxml resource");
                return;
            }
            
            System.out.println("Found Store.fxml at: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            String cssPath = "/io/github/palexdev/materialfx/demo/css/Stylesheet.css";
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Could not find Stylesheet.css at path: " + cssPath);
            }
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to store: " + e.getMessage());
        }
    }
    
    /**
     * Navigate to the organizer's home page after successful login
     */
    private void navigateToOrganizerHome() {
        try {
            System.out.println("Navigating to organizer home page");
            // Use a direct path to the resource instead of the resource loader
            String fxmlPath = "/io/github/palexdev/materialfx/demo/fxml/OrganizerHome.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            
            if (fxmlUrl == null) {
                System.err.println("Could not find OrganizerHome.fxml at path: " + fxmlPath);
                // Try to find the FXML file and report its actual location
                findAndReportFxmlLocation("OrganizerHome.fxml");
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not find OrganizerHome.fxml resource");
                return;
            }
            
            System.out.println("Found OrganizerHome.fxml at: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            String cssPath = "/io/github/palexdev/materialfx/demo/css/Stylesheet.css";
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Could not find Stylesheet.css at path: " + cssPath);
            }
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to organizer home: " + e.getMessage());
        }
    }
    
    /**
     * Old method kept for reference - navigates directly to the store
     * @deprecated Use navigateToOrganizerHome() instead
     */
    private void navigateToOrganizerDashboard() {
        try {
            System.out.println("Navigating to organizer dashboard (store)");
            // Use a direct path to the resource instead of the resource loader
            String fxmlPath = "/io/github/palexdev/materialfx/demo/fxml/Store.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            
            if (fxmlUrl == null) {
                System.err.println("Could not find Store.fxml at path: " + fxmlPath);
                // Try to find the FXML file and report its actual location
                findAndReportFxmlLocation("Store.fxml");
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not find Store.fxml resource");
                return;
            }
            
            System.out.println("Found Store.fxml at: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            String cssPath = "/io/github/palexdev/materialfx/demo/css/Stylesheet.css";
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Could not find Stylesheet.css at path: " + cssPath);
            }
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to store: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to search for FXML files in the project and report their locations.
     * This is useful for debugging when the FXML files are not found at the expected paths.
     *
     * @param fileName the name of the FXML file to find (e.g., "Store.fxml")
     */
    private void findAndReportFxmlLocation(String fileName) {
        try {
            System.out.println("Searching for " + fileName + " in project...");
            
            // Try to find the file in the resources directory
            URL resourceUrl = getClass().getResource("/");
            if (resourceUrl != null) {
                System.out.println("Resource root URL: " + resourceUrl);
                String resourcePath = resourceUrl.getPath();
                Path resourceDir = Paths.get(resourcePath);
                
                if (Files.exists(resourceDir)) {
                    // Search for the file in the resource directory
                    try (Stream<Path> paths = Files.walk(resourceDir)) {
                        paths.filter(path -> path.getFileName().toString().equals(fileName))
                             .forEach(path -> System.out.println("Found file at: " + path));
                    }
                } else {
                    System.err.println("Resource directory does not exist: " + resourceDir);
                }
            } else {
                System.err.println("Could not get resource root URL");
            }
            
            // Also try using the class loader
            URL classLoaderUrl = getClass().getClassLoader().getResource(fileName);
            if (classLoaderUrl != null) {
                System.out.println("Found via ClassLoader: " + classLoaderUrl);
            }
            
            // Try alternative paths
            String[] altPaths = {
                "/fxml/" + fileName,
                "/io/github/palexdev/materialfx/demo/fxml/" + fileName,
                "/io/github/palexdev/materialfx/fxml/" + fileName,
                "/demo/fxml/" + fileName
            };
            
            for (String altPath : altPaths) {
                URL altUrl = getClass().getResource(altPath);
                if (altUrl != null) {
                    System.out.println("Found at alternative path: " + altPath + " -> " + altUrl);
                }
            }
        } catch (Exception e) {
            System.err.println("Error searching for FXML file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void OnSignUpClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/palexdev/materialfx/demo/fxml/signup.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open signup form: " + e.getMessage());
        }
    }

    public void handleLogout() {
        try {
            // Clear the current user session
            UserSession.getInstance().logout();

            // Navigate back to login page
            FXMLLoader loader = new FXMLLoader(MFXDemoResourcesLoader.loadURL("fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(MFXDemoResourcesLoader.load("css/Stylesheet.css"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onForgotPasswordClicked(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(MFXDemoResourcesLoader.loadURL("fxml/ForgotPassword.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(MFXDemoResourcesLoader.load("css/Stylesheet.css"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load forgot password page: " + e.getMessage());
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
                    // Initialize UserService
                    UserService userService = new UserService();

                    // Check if user exists by email
                    User user = userService.findUserByEmail(userInfo.getEmail());
                    
                    // If user doesn't exist, register a new user
                    if (user == null) {
                        // Navigate to Google registration page
                        navigateToGoogleRegistration();
                        System.out.println("Login successful! Welcome, " +
                                UserSession.getInstance().getCurrentUser().getFirstname());
                        return;
                    }

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
            FXMLLoader loader = new FXMLLoader(MFXDemoResourcesLoader.loadURL("fxml/GoogleRegister.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(MFXDemoResourcesLoader.load("css/Stylesheet.css"));
            Stage stage = (Stage) googleLoginButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load Google registration page: " + e.getMessage());
        }
    }
    
    private void showMaterialFXAlert(String title, String message, String iconCode, String styleClass) {
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
            dialogContent.clearActions();

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
