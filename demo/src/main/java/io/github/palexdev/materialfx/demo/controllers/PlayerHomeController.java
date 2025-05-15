package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.utils.ScrollUtils;
import io.github.palexdev.materialfx.utils.SwingFXUtils;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoader;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoaderBean;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
        //loader.addView(MFXLoaderBean.of("Dashboard", loadURL("fxml/addUser.fxml")).setBeanToNodeMapper(() -> createToggle("fas-circle-dot", "Dashboard")).setDefaultRoot(true).get());
        loader.addView(MFXLoaderBean.of("Tournaments", loadURL("fxml/Front.fxml")).setBeanToNodeMapper(() -> createToggle("fas-trophy", "Tournaments")).get());
        loader.addView(MFXLoaderBean.of("Matches", loadURL("fxml/PlayerFront.fxml")).setBeanToNodeMapper(() -> createToggle("fas-circle-dot", "Matches")).get());

//        loader.addView(MFXLoaderBean.of("Profile", loadURL("fxml/PlayerProfile.fxml"))
//                .setBeanToNodeMapper(() -> createToggle("fas-user", "Profile"))
//                .get());

        loader.setOnLoadedAction(beans -> {
            List<ToggleButton> nodes = beans.stream().map(bean -> {
                ToggleButton toggle = (ToggleButton) bean.getBeanToNodeMapper().get();
                toggle.setOnAction(event -> contentPane.getChildren().setAll(bean.getRoot()));
                if (bean.isDefaultView()) {
                    contentPane.getChildren().setAll(bean.getRoot());
                    toggle.setSelected(true);
                }
                return toggle;
            }).toList();
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

        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setStyle("-fx-background-color: #1B1F3B;");
        mainContainer.setPadding(new Insets(20));

        // Header section with close button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label titleLabel = new Label("Edit Profile");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MFXButton closeButton = new MFXButton("×");
        closeButton.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-text-fill: white;");
        closeButton.setOnAction(e -> popupStage.close());

        header.getChildren().addAll(titleLabel, spacer, closeButton);

        // Profile section
        VBox profileSection = new VBox(10);
        profileSection.setAlignment(Pos.CENTER);

        // Profile picture setup
        ImageView profileImageView = new ImageView();
        profileImageView.setFitWidth(120);
        profileImageView.setFitHeight(120);

        // Set proper image preservation settings
        profileImageView.setPreserveRatio(false);
        profileImageView.setSmooth(true);

        // Create a StackPane to center the ImageView
        StackPane imageContainer = new StackPane(profileImageView);
        imageContainer.setMaxSize(120, 120);
        imageContainer.setMinSize(120, 120);

        // Create and apply circular clip
        Circle clip = new Circle(60);
        clip.setCenterX(60);
        clip.setCenterY(60);
        imageContainer.setClip(clip);

        // Load current profile picture
        reloadProfileImage(profileImageView);

        MFXButton uploadImageButton = new MFXButton("Change Picture");
        uploadImageButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-size: 14px;");
        uploadImageButton.setOnAction(e -> handleImageUpload(profileImageView));

        profileSection.getChildren().addAll(imageContainer, uploadImageButton);

        // Form sections
        VBox formContainer = new VBox(20);
        formContainer.setStyle("-fx-background-color: #2A2F4F; -fx-padding: 20; -fx-background-radius: 10;");

        // Personal Information Section
        VBox personalInfoSection = new VBox(10);
        Label personalInfoLabel = new Label("Personal Information");
        personalInfoLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        MFXTextField firstNameField = new MFXTextField();
        firstNameField.setFloatingText("First Name");
        firstNameField.setText(UserSession.getInstance().getCurrentUser().getFirstname());
        firstNameField.setStyle("-fx-text-fill: black;");

        MFXTextField lastNameField = new MFXTextField();
        lastNameField.setFloatingText("Last Name");
        lastNameField.setText(UserSession.getInstance().getCurrentUser().getLastName());
        lastNameField.setStyle("-fx-text-fill: black;");

        MFXTextField emailField = new MFXTextField();
        emailField.setFloatingText("Email");
        emailField.setText(UserSession.getInstance().getCurrentUser().getEmail());
        emailField.setStyle("-fx-text-fill: black;");

        MFXTextField phoneField = new MFXTextField();
        phoneField.setFloatingText("Phone Number");
        phoneField.setText(UserSession.getInstance().getCurrentUser().getPhoneNumber());
        phoneField.setStyle("-fx-text-fill: black;");

        personalInfoSection.getChildren().addAll(personalInfoLabel, firstNameField, lastNameField, emailField, phoneField);

        // Password Change Section
        VBox passwordSection = new VBox(10);
        Label passwordLabel = new Label("Change Password");
        passwordLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        MFXPasswordField currentPasswordField = new MFXPasswordField();
        currentPasswordField.setFloatingText("Current Password");
        currentPasswordField.setStyle("-fx-text-fill: black;");

        MFXPasswordField newPasswordField = new MFXPasswordField();
        newPasswordField.setFloatingText("New Password");
        newPasswordField.setStyle("-fx-text-fill: black;");

        MFXPasswordField confirmPasswordField = new MFXPasswordField();
        confirmPasswordField.setFloatingText("Confirm New Password");
        confirmPasswordField.setStyle("-fx-text-fill: black;");

        passwordSection.getChildren().addAll(passwordLabel, currentPasswordField, newPasswordField, confirmPasswordField);

        // Add sections to form container
        formContainer.getChildren().addAll(personalInfoSection, passwordSection);

        // Create a scroll pane for the form
        MFXScrollPane scrollPane = new MFXScrollPane(formContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        MFXButton saveButton = new MFXButton("Save Changes");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> popupStage.close());

        buttonBox.getChildren().addAll(saveButton, cancelButton);

        // Save button action
        saveButton.setOnAction(e -> {
            User currentUser = UserSession.getInstance().getCurrentUser();
            boolean hasChanges = false;

            // Check for changes in text fields
            if (!firstNameField.getText().equals(currentUser.getFirstname())) {
                currentUser.setFirstname(firstNameField.getText());
                hasChanges = true;
            }
            if (!lastNameField.getText().equals(currentUser.getLastName())) {
                currentUser.setLastName(lastNameField.getText());
                hasChanges = true;
            }
            if (!emailField.getText().equals(currentUser.getEmail())) {
                currentUser.setEmail(emailField.getText());
                hasChanges = true;
            }
            if (!phoneField.getText().equals(currentUser.getPhoneNumber())) {
                currentUser.setPhoneNumber(phoneField.getText());
                hasChanges = true;
            }

            // Handle password change if new password is provided
            if (!newPasswordField.getText().isEmpty()) {
                if (!currentPasswordField.getText().equals(currentUser.getPassword())) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect");
                    return;
                }
                if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                    showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match");
                    return;
                }
                currentUser.setPassword(newPasswordField.getText());
                hasChanges = true;
            }

            // Handle profile picture change if a new image was selected
            if (tempProfileImageFile != null) {
                try {
                    // Generate a unique filename
                    String extension = tempProfileImageFile.getName().substring(tempProfileImageFile.getName().lastIndexOf("."));
                    String uniqueFileName = System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + extension;

                    // Create an "uploads" directory in the user's home directory if it doesn't exist
                    String userHome = System.getProperty("user.home");
                    File uploadsDir = new File(userHome + "/sportify/uploads/images");
                    if (!uploadsDir.exists()) {
                        uploadsDir.mkdirs();
                    }

                    // Create the destination file
                    File destinationFile = new File(uploadsDir, uniqueFileName);

                    // Copy the selected file to the destination
                    Files.copy(tempProfileImageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Update user profile picture in the database with the new filename
                    currentUser.setProfilePicture(uniqueFileName);
                    hasChanges = true;

                    // Clear the temporary file
                    tempProfileImageFile = null;
                } catch (Exception ex) {

                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload profile picture: " + ex.getMessage());
                    return;
                }
            }

            // Save changes if any
            if (hasChanges) {
                try {
                    UserService userService = new UserService();
                    userService.update(currentUser);

                    // Update the session with the modified user
                    UserSession.getInstance().updateUser(currentUser);

                    // Reload the profile image in the main view if it was changed
                    if (currentUser.getProfilePicture() != null) {
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

        // Add all components to main container
        mainContainer.getChildren().addAll(header, profileSection, scrollPane, buttonBox);

        Scene scene = new Scene(mainContainer, 700, 800);
        popupStage.setScene(scene);
        popupStage.show();
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
                    File imageFile = new File(userHome + "/sportify/uploads/images/" + fileName);
                    if (!imageFile.exists()) {
                        throw new IOException("Profile image not found: " + imageFile.getAbsolutePath());
                    }
                    fullPath = imageFile.toURI().toString();
                }

                Image image = new Image(fullPath,
                        profileImageView.getFitWidth(),
                        profileImageView.getFitHeight(),
                        true, true);

                profileImageView.setImage(image);

                // Add the style class for future updates
                profileImageView.getStyleClass().add("profile-image-view");

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