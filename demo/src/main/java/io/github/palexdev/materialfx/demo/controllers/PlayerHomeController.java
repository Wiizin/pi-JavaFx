package io.github.palexdev.materialfx.demo.controllers;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.utils.ScrollUtils;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoader;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoaderBean;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;


import static io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader.loadURL;


public class PlayerHomeController implements Initializable {

        //private final Stage stage;
        public VBox navButtons;
        @FXML
        public MFXButton profileButton;
        @FXML
        private AnchorPane rootPane;

        @FXML
        private HBox windowHeader;

        @FXML
        private MFXScrollPane scrollPane;

        @FXML
        private VBox navBar;

        @FXML
        private StackPane contentPane;

        @FXML
        private StackPane logoContainer;

        private final ToggleGroup toggleGroup = new ToggleGroup();
        @FXML
        private MFXButton darkModeToggle;
        @FXML
        private MFXFontIcon notificationsIcon;
        @FXML
        private Label userGreeting;
        private File tempProfileImageFile = null; // Temporary storage for the selected image


        @Override
        public void initialize(URL location, ResourceBundle resources) {
            // Handle window controls


            // Initialize the loader for navigation
            initializeLoader();
            //this.userGreeting.setText("welcome " + UserSession.getInstance().getCurrentUser().getFirstname());

            // Add smooth scrolling to the scroll pane
            ScrollUtils.addSmoothScrolling(scrollPane);

            // Load and display the logo
            Image image = new Image(MFXDemoResourcesLoader.load("sportify.png"), 64, 64, true, true);
            ImageView logo = new ImageView(image);
            Circle clip = new Circle(30);
            clip.centerXProperty().bind(logo.layoutBoundsProperty().map(Bounds::getCenterX));
            clip.centerYProperty().bind(logo.layoutBoundsProperty().map(Bounds::getCenterY));
            logo.setClip(clip);
            logoContainer.getChildren().add(logo);
            initializeProfileButton();

        }

        private void initializeLoader() {
            MFXLoader loader = new MFXLoader();
            User currentUser = UserSession.getInstance().getCurrentUser();

            try {
                // Add Events tab
                URL eventsUrl = loadURL("fxml/FrontEvent.fxml");
                if (eventsUrl != null) {
                    loader.addView(MFXLoaderBean.of("Events", eventsUrl)
                            .setBeanToNodeMapper(() -> createToggle("fas-calendar-alt", "Events"))
                            .setDefaultRoot(true)  // Set this as default view
                            .get());
                } else {
                    System.err.println("Failed to load FrontEvent.fxml");
                }

                // Add Reclamations tab with proper error handling
                URL reclamationsUrl = loadURL("fxml/PlayerReclamation.fxml");
                if (reclamationsUrl != null) {
                    loader.addView(MFXLoaderBean.of("Reclamations", reclamationsUrl)
                            .setBeanToNodeMapper(() -> createToggle("fas-comment-dots", "Reclamations"))
                            .setDefaultRoot(false)
                            .get());
                } else {
                    System.err.println("Failed to load PlayerReclamation.fxml");
                }
                URL storeUrl = loadURL("fxml/Store.fxml");
                if (storeUrl != null) {
                    System.out.println("Found Store.fxml at: " + storeUrl);
                    loader.addView(MFXLoaderBean.of("Store", storeUrl)
                            .setBeanToNodeMapper(() -> createToggle("fas-shopping-cart", "Store"))
                            .get());
                } else {
                    System.err.println("Could not find Store.fxml");
                }
                // Add Teams tab with proper error handling
                String teamsFile = currentUser.getIdteam() == 0 ?
                        "fxml/TeamSelectionForPlayer.fxml" :
                        "fxml/TeamPlayerFrontOffice.fxml";
                URL teamsUrl = loadURL(teamsFile);
                if (teamsUrl != null) {
                    loader.addView(MFXLoaderBean.of("Teams", teamsUrl)
                            .setBeanToNodeMapper(() -> createToggle("fas-circle-dot", "Teams"))
                            .setDefaultRoot(false)
                            .get());
                } else {
                    System.err.println("Failed to load " + teamsFile);
                }

                // Set up loader action with proper error handling
                loader.setOnLoadedAction(beans -> {
                    List<ToggleButton> nodes = beans.stream()
                            .map(bean -> {
                                ToggleButton toggle = (ToggleButton) bean.getBeanToNodeMapper().get();
                                toggle.setOnAction(event -> {
                                    Parent root = bean.getRoot();
                                    if (root != null) {
                                        contentPane.getChildren().setAll(root);
                                    } else {
                                        System.err.println("Failed to load view: " + bean.getViewName());
                                    }
                                });
                                if (bean.isDefaultView()) {
                                    Parent root = bean.getRoot();
                                    if (root != null) {
                                        contentPane.getChildren().setAll(root);
                                        toggle.setSelected(true);
                                    }
                                }
                                return toggle;
                            })
                            .toList();
                    navBar.getChildren().setAll(nodes);
                });

                // Start the loader
                loader.start();
            } catch (Exception e) {
                System.err.println("Error initializing loader: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Add the Store view with the shopping cart icon
        // Try to find and load Store.fxml




    private ToggleButton createToggle(String icon, String text) {
        MFXIconWrapper wrapper = new MFXIconWrapper(icon, 24, 32);
        MFXRectangleToggleNode toggleNode = new MFXRectangleToggleNode(text, wrapper);
        toggleNode.setAlignment(Pos.CENTER_LEFT);
        toggleNode.setMaxWidth(Double.MAX_VALUE);
        toggleNode.setToggleGroup(toggleGroup);
        return toggleNode;
    }

    private void initializeProfileButton() {
        // Create a ContextMenu
        ContextMenu contextMenu = new ContextMenu();

        // Add menu items
        MenuItem profileItem = new MenuItem("Profile");
        MenuItem settingsItem = new MenuItem("Settings");
        MenuItem logoutItem = new MenuItem("Logout");

        // Add event handlers for menu items
        profileItem.setOnAction(event -> handleProfileMenuItem());
        settingsItem.setOnAction(event -> handleSettingsMenuItem());
        logoutItem.setOnAction(event -> handleLogoutMenuItem());

        // Add items to the context menu
        contextMenu.getItems().addAll(profileItem, settingsItem, logoutItem);

        // Set the context menu to the profile button
        profileButton.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                contextMenu.show(profileButton, event.getScreenX(), event.getScreenY());
            }
        });
    }


    private void handleSettingsMenuItem() {
        System.out.println("Settings menu item clicked!");
        // Add logic to open the settings view
        // Example: Load the settings FXML into the contentPane
        // contentPane.getChildren().setAll(loadFXML("/path/to/Settings.fxml"));
    }

    private void handleLogoutMenuItem() {
        System.out.println("Logout menu item clicked!");

        // First, get the UserSession instance and logout
        UserSession.getInstance().logout();

        try {
            FXMLLoader loader = new FXMLLoader(Demo.class.getResource("fxml/login.fxml"));
            Parent signUpView = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(signUpView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //////////////////////////////////////////////////
    private void handleProfileMenuItem() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.UNDECORATED);

        // Main container with rich dark blue background
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a1f3c, #2d3250); -fx-background-radius: 20;");
        mainContainer.setPadding(new Insets(0));
        mainContainer.setMaxWidth(1000);
        mainContainer.setMaxHeight(700);

        // Header with subtle gradient
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(25));
        header.setStyle("-fx-background-color: linear-gradient(to right, #1a1f3c, #2d3250); -fx-background-radius: 20 20 0 0; -fx-border-color: rgba(255, 152, 0, 0.2); -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label("Profile Settings");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ff9800;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MFXButton closeButton = new MFXButton("×");
        closeButton.setStyle("-fx-font-size: 24px; -fx-background-color: transparent; -fx-text-fill: #ff9800;");
        closeButton.setOnAction(e -> popupStage.close());

        header.getChildren().addAll(titleLabel, spacer, closeButton);

        // Content container with scroll
        MFXScrollPane scrollPane = new MFXScrollPane();
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        VBox contentBox = new VBox(30);
        contentBox.setPadding(new Insets(30));
        contentBox.setStyle("-fx-background-color: transparent;");

        // Profile section with modern design
        HBox profileSection = new HBox(30);
        profileSection.setAlignment(Pos.CENTER_LEFT);

        // Profile picture container with subtle glow
        StackPane imageContainer = new StackPane();
        imageContainer.setMaxSize(120, 120);
        imageContainer.setMinSize(120, 120);
        imageContainer.setStyle("-fx-background-color: rgba(255, 152, 0, 0.1); -fx-background-radius: 60; " +
                "-fx-border-color: linear-gradient(to bottom right, #ff9800, #ff5722); -fx-border-width: 2; " +
                "-fx-border-radius: 60; -fx-effect: dropshadow(gaussian, rgba(255, 152, 0, 0.3), 10, 0, 0, 0);");

        // Create a container for the image to ensure proper centering
        StackPane imageWrapper = new StackPane();
        imageWrapper.setMaxSize(110, 110);
        imageWrapper.setMinSize(110, 110);

        ImageView profileImageView = new ImageView();
        profileImageView.setFitWidth(110);
        profileImageView.setFitHeight(110);
        profileImageView.setPreserveRatio(true);
        profileImageView.setSmooth(true);

        Circle clip = new Circle(55);
        clip.setCenterX(55);
        clip.setCenterY(55);
        imageWrapper.setClip(clip);

        reloadProfileImage(profileImageView);
        imageWrapper.getChildren().add(profileImageView);
        imageContainer.getChildren().add(imageWrapper);

        // Profile info section
        VBox profileInfo = new VBox(10);
        profileInfo.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(UserSession.getInstance().getCurrentUser().getFirstname() + " " +
                UserSession.getInstance().getCurrentUser().getLastName());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label emailLabel = new Label(UserSession.getInstance().getCurrentUser().getEmail());
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff9800;");

        MFXButton uploadImageButton = new MFXButton("Change Photo");
        uploadImageButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff9800; -fx-font-size: 14px; " +
                "-fx-padding: 8 16; -fx-border-color: #ff9800; -fx-border-radius: 20; -fx-border-width: 1;");
        uploadImageButton.setOnAction(e -> handleImageUpload(profileImageView));

        profileInfo.getChildren().addAll(nameLabel, emailLabel, uploadImageButton);
        profileSection.getChildren().addAll(imageContainer, profileInfo);

        // Form sections with modern card design
        VBox formContainer = new VBox(25);
        formContainer.setStyle("-fx-background-color: rgba(255, 152, 0, 0.05); -fx-background-radius: 15; " +
                "-fx-padding: 25; -fx-border-color: linear-gradient(to bottom right, rgba(255, 152, 0, 0.2), rgba(255, 87, 34, 0.2)); " +
                "-fx-border-radius: 15; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 10, 0, 0, 0);");

        // Personal Information Section
        VBox personalInfoSection = new VBox(20);
        Label personalInfoLabel = new Label("Personal Information");
        personalInfoLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff9800;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        // Create modern text fields with consistent styling
        MFXTextField firstNameField = createStyledTextField("First Name", UserSession.getInstance().getCurrentUser().getFirstname());
        MFXTextField lastNameField = createStyledTextField("Last Name", UserSession.getInstance().getCurrentUser().getLastName());
        MFXTextField emailField = createStyledTextField("Email", UserSession.getInstance().getCurrentUser().getEmail());
        MFXTextField phoneField = createStyledTextField("Phone Number", UserSession.getInstance().getCurrentUser().getPhoneNumber());

        grid.add(firstNameField, 0, 0);
        grid.add(lastNameField, 1, 0);
        grid.add(emailField, 0, 1);
        grid.add(phoneField, 1, 1);

        personalInfoSection.getChildren().addAll(personalInfoLabel, grid);

        // Password Change Section
        VBox passwordSection = new VBox(20);
        Label passwordLabel = new Label("Change Password");
        passwordLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff9800;");

        MFXPasswordField currentPasswordField = createStyledPasswordField("Current Password");
        MFXPasswordField newPasswordField = createStyledPasswordField("New Password");
        MFXPasswordField confirmPasswordField = createStyledPasswordField("Confirm New Password");

        passwordSection.getChildren().addAll(passwordLabel, currentPasswordField, newPasswordField, confirmPasswordField);

        formContainer.getChildren().addAll(personalInfoSection, passwordSection);

        // Add all sections to content box
        contentBox.getChildren().addAll(profileSection, formContainer);
        scrollPane.setContent(contentBox);

        // Footer with buttons
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(20, 30, 30, 30));
        footer.setStyle("-fx-background-color: linear-gradient(to right, #2d3250, #1a1f3c); " +
                "-fx-background-radius: 0 0 20 20; -fx-border-color: rgba(255, 152, 0, 0.2); -fx-border-width: 1 0 0 0;");

        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff9800; -fx-font-size: 14px; " +
                "-fx-padding: 10 20; -fx-border-color: #ff9800; -fx-border-radius: 20; -fx-border-width: 1;");
        cancelButton.setOnAction(e -> popupStage.close());

        MFXButton saveButton = new MFXButton("Save Changes");
        saveButton.setStyle("-fx-background-color: linear-gradient(to right, #ff9800, #ff5722); -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(255, 152, 0, 0.3), 10, 0, 0, 0);");
        saveButton.setOnAction(e -> {
            User currentUser = UserSession.getInstance().getCurrentUser();
            boolean hasChanges = false;
            User updatedUser = new User(); // Create a new user object for updates
            updatedUser.setId(currentUser.getId()); // Set the ID to match current user

            // Check for changes in text fields
            if (!firstNameField.getText().equals(currentUser.getFirstname())) {
                updatedUser.setFirstname(firstNameField.getText());
                hasChanges = true;
            } else {
                updatedUser.setFirstname(currentUser.getFirstname());
            }

            if (!lastNameField.getText().equals(currentUser.getLastName())) {
                updatedUser.setLastName(lastNameField.getText());
                hasChanges = true;
            } else {
                updatedUser.setLastName(currentUser.getLastName());
            }

            if (!emailField.getText().equals(currentUser.getEmail())) {
                updatedUser.setEmail(emailField.getText());
                hasChanges = true;
            } else {
                updatedUser.setEmail(currentUser.getEmail());
            }

            if (!phoneField.getText().equals(currentUser.getPhoneNumber())) {
                updatedUser.setPhoneNumber(phoneField.getText());
                hasChanges = true;
            } else {
                updatedUser.setPhoneNumber(currentUser.getPhoneNumber());
            }

            // Handle password change only if new password is provided
            if (!newPasswordField.getText().isEmpty()) {
                // Verify current password using BCrypt
                if (!BCrypt.checkpw(currentPasswordField.getText(), currentUser.getPassword())) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect");
                    return;
                }

                if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                    showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match");
                    return;
                }

                updatedUser.setPassword(newPasswordField.getText());
                hasChanges = true;
            } else {
                // Set password to null if no new password is provided
                // This will make the service use the existing password from the database
                updatedUser.setPassword(null);
            }

            // Handle profile picture change if a new image was selected
            if (tempProfileImageFile != null) {
                try {
                    // Generate a unique filename
                    String extension = tempProfileImageFile.getName().substring(tempProfileImageFile.getName().lastIndexOf("."));
                    String uniqueFileName = System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + extension;

                    // Create an "uploads" directory in the user's home directory if it doesn't exist
                    String xamppPath = "C:/xampp/htdocs/img/profile_pictures";
                    File uploadsDir = new File(xamppPath);
                    if (!uploadsDir.exists()) {
                        uploadsDir.mkdirs();
                    }

                    // Create the destination file
                    File destinationFile = new File(uploadsDir, uniqueFileName);

                    // Copy the selected file to the destination
                    Files.copy(tempProfileImageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Update user profile picture with the new filename
                    updatedUser.setProfilePicture(uniqueFileName);
                    hasChanges = true;

                    // Clear the temporary file
                    tempProfileImageFile = null;
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload profile picture: " + ex.getMessage());
                    return;
                }
            } else {
                // Keep the existing profile picture if no new one was selected
                updatedUser.setProfilePicture(currentUser.getProfilePicture());
            }

            // Copy other necessary fields from current user
            updatedUser.setRole(currentUser.getRole());
            updatedUser.setDateOfBirth(currentUser.getDateOfBirth());
            updatedUser.setCreatedAt(currentUser.getCreatedAt());
            updatedUser.setUpdatedAt(LocalDateTime.now());
            updatedUser.setActive(currentUser.isActive());
            updatedUser.setIdteam(currentUser.getIdteam());

            // Save changes if any
            if (hasChanges) {
                try {
                    UserService userService = new UserService();
                    userService.update2(updatedUser);

                    // Update the session with the modified user
                    UserSession.getInstance().updateUser(updatedUser);

                    // Reload the profile image in the main view if it was changed
                    if (updatedUser.getProfilePicture() != null) {
                        reloadProfileImage(profileImageView);
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully");
                    popupStage.close();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile: " + ex.getMessage());
                }
            } else {
                popupStage.close();
            }
        });

        footer.getChildren().addAll(cancelButton, saveButton);

        // Add all components to main container
        mainContainer.getChildren().addAll(header, scrollPane, footer);

        // Create scene with modern styling
        Scene scene = new Scene(mainContainer);
        scene.setFill(null);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private MFXTextField createStyledTextField(String floatingText, String initialValue) {
        MFXTextField textField = new MFXTextField();
        textField.setFloatingText(floatingText);
        textField.setText(initialValue);
        textField.setStyle("-fx-background-color: rgba(255, 152, 0, 0.05); -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-background-radius: 8; -fx-border-color: linear-gradient(to bottom right, rgba(255, 152, 0, 0.2), rgba(255, 87, 34, 0.2)); " +
                "-fx-border-radius: 8; -fx-border-width: 1;");
        textField.setPrefHeight(45);
        textField.setPrefWidth(300);
        return textField;
    }

    private MFXPasswordField createStyledPasswordField(String floatingText) {
        MFXPasswordField passwordField = new MFXPasswordField();
        passwordField.setFloatingText(floatingText);
        passwordField.setStyle("-fx-background-color: rgba(255, 152, 0, 0.05); -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-background-radius: 8; -fx-border-color: linear-gradient(to bottom right, rgba(255, 152, 0, 0.2), rgba(255, 87, 34, 0.2)); " +
                "-fx-border-radius: 8; -fx-border-width: 1;");
        passwordField.setPrefHeight(45);
        passwordField.setPrefWidth(300);
        return passwordField;
    }

    private void reloadProfileImage(ImageView profileImageView) {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getProfilePicture() != null) {
            try {
                String fileName = currentUser.getProfilePicture();
                String fullPath;

                if (fileName.equals("default_profile.jpg")) {
                    // Load default image from resources
                    URL resourceUrl = getClass().getResource("/default_profile.jpg");
                    if (resourceUrl != null) {
                        fullPath = resourceUrl.toExternalForm();
                    } else {
                        throw new IOException("Default profile image not found in resources");
                    }
                } else {
                    // Load from uploads directory
                    String userHome = System.getProperty("user.home");
                    File imageFile = new File("C:/xampp/htdocs/img/profile_pictures/" + fileName);
                    if (!imageFile.exists()) {
                        throw new IOException("Profile image not found: " + imageFile.getAbsolutePath());
                    }
                    fullPath = imageFile.toURI().toString();
                }

                Image image = new Image(fullPath);

                // Calculate the scaling to ensure the image fills the circle while maintaining aspect ratio
                double scale = Math.max(110 / image.getWidth(), 110 / image.getHeight());
                profileImageView.setFitWidth(image.getWidth() * scale);
                profileImageView.setFitHeight(image.getHeight() * scale);

                profileImageView.setImage(image);
                profileImageView.setPreserveRatio(true);

            } catch (Exception e) {
                e.printStackTrace();
                setDefaultProfileImage(profileImageView);
            }
        } else {
            setDefaultProfileImage(profileImageView);
        }
    }

    private void setDefaultProfileImage(ImageView profileImageView) {
        try {
            String defaultImagePath = "default_profile.jpg";
            String defaultImageUrl = getClass().getResource("/" + defaultImagePath).toExternalForm();
            Image defaultImage = new Image(defaultImageUrl);

            // Calculate the scaling to ensure the image fills the circle while maintaining aspect ratio
            double scale = Math.max(110 / defaultImage.getWidth(), 110 / defaultImage.getHeight());
            profileImageView.setFitWidth(defaultImage.getWidth() * scale);
            profileImageView.setFitHeight(defaultImage.getHeight() * scale);

            profileImageView.setImage(defaultImage);
            profileImageView.setPreserveRatio(true);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load default profile image");
        }
    }

    private void handleImageUpload(ImageView profileImageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Temporarily store the selected file
                tempProfileImageFile = selectedFile;

                // Display the selected image in the ImageView
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile picture selected. Click 'Save' to upload.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load image: " + e.getMessage());
            }
        }
    }

    private String imageViewToUrl(ImageView imageView) {
        if (imageView.getImage() != null && imageView.getImage().getUrl() != null) {
            return imageView.getImage().getUrl();
        }
        return null;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Stage alertStage = new Stage();
        alertStage.initStyle(StageStyle.UNDECORATED);
        alertStage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1B1F3B; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-border-color: #ff9800; -fx-border-width: 2;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + (type == Alert.AlertType.ERROR ? "#FF4444" : "#ff9800") +
                "; -fx-font-size: 18px; -fx-font-weight: BOLD;");

        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        contentLabel.setWrapText(true);

        MFXButton okButton = new MFXButton("OK");
        okButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        okButton.setOnAction(e -> alertStage.close());

        root.getChildren().addAll(titleLabel, contentLabel, okButton);

        Scene scene = new Scene(root, 400, 250);
        alertStage.setScene(scene);
        alertStage.showAndWait();
    }
}
