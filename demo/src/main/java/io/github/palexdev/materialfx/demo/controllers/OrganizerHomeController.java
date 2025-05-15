package io.github.palexdev.materialfx.demo.controllers;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader;
import io.github.palexdev.materialfx.demo.model.Organizer;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.dialogs.MFXDialogs;
import io.github.palexdev.materialfx.utils.ScrollUtils;
import io.github.palexdev.materialfx.utils.SwingFXUtils;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoader;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoaderBean;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader.loadURL;

public class OrganizerHomeController implements Initializable{


        @FXML
        public MFXButton profileButton;
        @FXML
        public MFXButton notificationsButton;
        @FXML
        public Label userGreeting;
        @FXML
        public Label activeEventsLabel;
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
        private Label upcomingEventsLabel;
        @FXML
        private Label organizerStatusLabel;
        @FXML
        private Label totalPlayersLabel;
        @FXML
        private Label lastLoginLabel;

        @Override
        public void initialize(URL location, ResourceBundle resources) {
            // Initialize the loader for navigation
            initializeLoader();

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

            // Initialize profile button
            initializeProfileButton();

            // Set greeting message
            User currentUser = UserSession.getInstance().getCurrentUser();
            if (currentUser instanceof Organizer) {
                userGreeting.setText("Welcome, " + ((Organizer) currentUser).getFirstname() + "!");
            }
        }

    private void initializeLoader() {
        MFXLoader loader = new MFXLoader();

        // Add views to the navigation
        // Dashboard View
        URL dashboardUrl = loadURL("fxml/Dashboard.fxml");
        if (dashboardUrl != null) {
            loader.addView(MFXLoaderBean.of("Dashboard", dashboardUrl).setBeanToNodeMapper(() -> createToggle("fas-house", "Dashboard")).setDefaultRoot(true).get());
        }

        // Tournaments View
        URL tournamentsUrl = loadURL("fxml/Tournament.fxml");
        if (tournamentsUrl != null) {
            loader.addView(MFXLoaderBean.of("Tournaments", tournamentsUrl).setBeanToNodeMapper(() -> createToggle("fas-trophy", "Tournaments")).get());
        }

        // Add Store view
        URL storeUrl = loadURL("fxml/Store.fxml");
        if (storeUrl != null) {
            System.out.println("Found Store.fxml at: " + storeUrl);
            loader.addView(MFXLoaderBean.of("Store", storeUrl)
                .setBeanToNodeMapper(() -> createToggle("fas-shopping-cart", "Store"))
                .get());
        } else {
            System.err.println("Could not find Store.fxml");
        }

        loader.setOnLoadedAction(beans -> {
            List<ToggleButton> nodes = beans.stream().map(bean -> {
                ToggleButton toggle = (ToggleButton) bean.getBeanToNodeMapper().get();
                toggle.setOnAction(event -> {
                    // Get the loaded view
                    Parent root = bean.getRoot();

                    // If this is the Store view, hide its sidebar
                    if (bean.getViewName().equals("Store")) {
                        // Find the sidebar container by ID and hide it
                        root.lookup("#sidebarContainer").setVisible(false);
                        root.lookup("#sidebarContainer").setManaged(false);

                        // Find the main content VBox and adjust its anchor
                        VBox mainContent = (VBox) root.lookup(".main-container");
                        if (mainContent != null && mainContent.getParent() instanceof AnchorPane) {
                            AnchorPane.setLeftAnchor(mainContent, 0.0);
                        }
                    }

                    contentPane.getChildren().setAll(root);
                });
                // Default selection handled by setDefaultRoot(true) in the view setup
                return toggle;
            }).toList();
            navBar.getChildren().setAll(nodes);
        });
            // Add organizer-specific views
//            loader.addView(MFXLoaderBean.of("Dashboard", loadURL("fxml/OrganizerDashboard.fxml"))
//                    .setBeanToNodeMapper(() -> createToggle("fas-home", "Dashboard"))
//                    .setDefaultRoot(true)
//                    .get());

//            loader.addView(MFXLoaderBean.of("Manage Events", loadURL("fxml/ManageEvents.fxml"))
//                    .setBeanToNodeMapper(() -> createToggle("fas-calendar", "Events"))
//                    .get());
//
//            loader.addView(MFXLoaderBean.of("Participants", loadURL("fxml/ManageParticipants.fxml"))
//                    .setBeanToNodeMapper(() -> createToggle("fas-users", "Participants"))
//                    .get());

            // Check if organizer has a team to manage
            User currentUser = UserSession.getInstance().getCurrentUser();
            if (currentUser.getIdteam() == 0) {
                loader.addView(MFXLoaderBean.of("Teams", loadURL("fxml/TeamSelection.fxml"))
                        .setBeanToNodeMapper(() -> createToggle("fas-circle-dot", "Teams"))
                        .setDefaultRoot(false)
                        .get());
            } else {
                loader.addView(MFXLoaderBean.of("Teams", loadURL("fxml/TeamFrontOffice.fxml"))
                        .setBeanToNodeMapper(() -> createToggle("fas-circle-dot", "Teams"))
                        .setDefaultRoot(false)
                        .get());

            }

            loader.setOnLoadedAction(beans -> {
                List<ToggleButton> nodes = beans.stream()
                        .map(bean -> {
                            ToggleButton toggle = (ToggleButton) bean.getBeanToNodeMapper().get();
                            toggle.setOnAction(event -> contentPane.getChildren().setAll(bean.getRoot()));
                            if (bean.isDefaultView()) {
                                contentPane.getChildren().setAll(bean.getRoot());
                                toggle.setSelected(true);
                            }
                            return toggle;
                        })
                        .toList();
                navBar.getChildren().setAll(nodes);
            });
            loader.start();
    }

    private ToggleButton createToggle(String icon, String text) {
        MFXIconWrapper wrapper = new MFXIconWrapper(icon, 24, 32);
        MFXRectangleToggleNode toggleNode = new MFXRectangleToggleNode(text, wrapper);
        toggleNode.setAlignment(Pos.CENTER_LEFT);
        toggleNode.setMaxWidth(Double.MAX_VALUE);
        toggleNode.setToggleGroup(toggleGroup);
        return toggleNode;
    }

    private void initializeProfileButton() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem profileItem = new MenuItem("Profile");
        MenuItem eventsItem = new MenuItem("My Events");
        MenuItem settingsItem = new MenuItem("Settings");
        MenuItem logoutItem = new MenuItem("Logout");

        profileItem.setOnAction(event -> handleProfileMenuItem());
        eventsItem.setOnAction(event -> handleEventsMenuItem());
        settingsItem.setOnAction(event -> handleSettingsMenuItem());
        logoutItem.setOnAction(event -> handleLogoutMenuItem());

        contextMenu.getItems().addAll(profileItem, eventsItem, settingsItem, logoutItem);

        profileButton.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                contextMenu.show(profileButton, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private void handleEventsMenuItem() {
        // Implement events view navigation
        System.out.println("Events menu item clicked!");
    }

    private void handleSettingsMenuItem() {
        // Implement settings view navigation
        System.out.println("Settings menu item clicked!");
    }

    private void handleLogoutMenuItem() {
        UserSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(Demo.class.getResource("fxml/login.fxml"));
            Parent loginView = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
        }
    }

    private void handleProfileMenuItem() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.UNDECORATED);

        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #1B1F3B; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-border-color: #ff9800; -fx-border-width: 2;");

        // Get current user
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (!(currentUser instanceof Organizer)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid user type!");
            return;
        }

        Organizer currentOrganizer = (Organizer) currentUser;

        // Create circular ImageView for profile picture
        ImageView profileImageView = new ImageView();
        profileImageView.setFitWidth(100); // Set the desired width
        profileImageView.setFitHeight(100); // Set the desired height
        profileImageView.setPreserveRatio(true);

        // Create a circular clip
        Circle clip = new Circle(50, 50, 50); // Center X, Center Y, Radius
        profileImageView.setClip(clip);

        // Load the user's profile picture (or default if none exists)
        if (currentOrganizer.getProfilePicture() != null) {
            try {
                Image userImage = new Image(currentOrganizer.getProfilePicture());
                profileImageView.setImage(userImage);
            } catch (Exception e) {
                setDefaultProfileImage(profileImageView);
            }
        } else {
            setDefaultProfileImage(profileImageView);
        }

        profileImageView.getStyleClass().add("profile-image");

        // Add a button to upload a new profile picture
        MFXButton uploadImageButton = new MFXButton("Change Picture");
        uploadImageButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        uploadImageButton.setOnAction(e -> handleImageUpload(profileImageView));

        // Add basic profile fields
        MFXTextField firstNameField = new MFXTextField();
        firstNameField.setFloatingText("First Name");
        firstNameField.setText(currentOrganizer.getFirstname());

        MFXTextField lastNameField = new MFXTextField();
        lastNameField.setFloatingText("Last Name");
        lastNameField.setText(currentOrganizer.getLastName());

        MFXTextField emailField = new MFXTextField();
        emailField.setFloatingText("Email");
        emailField.setText(currentOrganizer.getEmail());

        MFXTextField phoneField = new MFXTextField();
        phoneField.setFloatingText("Phone Number");
        phoneField.setText(currentOrganizer.getPhoneNumber());

        Label titleLabel = new Label("Edit Profile");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        // Create buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        MFXButton saveButton = new MFXButton("Save");
        saveButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        saveButton.setPrefWidth(100);
        saveButton.setPrefHeight(40);

        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        cancelButton.setPrefWidth(100);
        cancelButton.setPrefHeight(40);

        // Add all components to form
        form.getChildren().addAll(
                profileImageView,
                uploadImageButton,
                titleLabel,
                firstNameField,
                lastNameField,
                emailField,
                phoneField,
                buttonBox
        );

        // Handle save button
        saveButton.setOnAction(e -> {
            try {
                // Update organizer information
                currentOrganizer.setFirstname(firstNameField.getText());
                currentOrganizer.setLastName(lastNameField.getText());
                currentOrganizer.setEmail(emailField.getText());
                currentOrganizer.setPhoneNumber(phoneField.getText());

                // Save to database
                UserService userService = new UserService();
                userService.update2(currentOrganizer);

                popupStage.close();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");

                // Reload the profile image in the main view
                reloadProfileImage(profileImageView);
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile: " + ex.getMessage());
            }
        });

        // Handle cancel button
        cancelButton.setOnAction(e -> popupStage.close());

        Scene scene = new Scene(form, 600, 500);
        popupStage.setScene(scene);
        popupStage.show();
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
                // Get the file extension
                String fileName = selectedFile.getName();
                String extension = fileName.substring(fileName.lastIndexOf("."));

                // Generate a unique filename using timestamp
                String uniqueFileName = System.currentTimeMillis() + extension;

                // Create an "uploads" directory in the user's home directory
                String userHome = System.getProperty("user.home");
                File uploadsDir = new File(userHome, "sportify/uploads/images");
                if (!uploadsDir.exists()) {
                    uploadsDir.mkdirs();
                }

                // Create the destination file
                File destinationFile = new File(uploadsDir, uniqueFileName);

                // Copy the file
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Store only the relative path
                String relativePath = "sportify/uploads/images/" + uniqueFileName;

                // Update the current user's profile picture with relative path
                User currentUser = UserSession.getInstance().getCurrentUser();
                currentUser.setProfilePicture(relativePath);

                // For display, we need the full path
                String imageUrl = destinationFile.toURI().toString();
                Image image = new Image(imageUrl,
                        profileImageView.getFitWidth(),
                        profileImageView.getFitHeight(),
                        true, true);
                profileImageView.setImage(image);

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save image: " + e.getMessage());
            }
        }
    }

    private void reloadProfileImage(ImageView profileImageView) {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getProfilePicture() != null) {
            try {
                String relativePath = currentUser.getProfilePicture();
                String fullPath;

                if (relativePath.equals("default_profile.jpg")) {
                    // Load from resources for default image
                    fullPath = getClass().getResource("/" + relativePath).toExternalForm();
                } else {
                    // Load from user home directory for uploaded images
                    String userHome = System.getProperty("user.home");
                    File imageFile = new File(userHome, relativePath);
                    fullPath = imageFile.toURI().toString();
                }

                Image image = new Image(fullPath,
                        profileImageView.getFitWidth(),
                        profileImageView.getFitHeight(),
                        true, true);
                profileImageView.setImage(image);
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
            Image defaultImage = new Image(defaultImageUrl,
                    profileImageView.getFitWidth(),
                    profileImageView.getFitHeight(),
                    true, true);
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load default profile image");
        }
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
