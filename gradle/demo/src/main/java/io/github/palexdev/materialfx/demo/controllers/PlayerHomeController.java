package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import io.github.palexdev.materialfx.utils.ScrollUtils;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoader;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoaderBean;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
        loader.addView(MFXLoaderBean.of("Dashboard", loadURL("fxml/addUser.fxml")).setBeanToNodeMapper(() -> createToggle("fas-circle-dot", "Dashboard")).setDefaultRoot(true).get());
        //loader.addView(MFXLoaderBean.of("Tournaments", loadURL("fxml/login.fxml")).setBeanToNodeMapper(() -> createToggle("fas-trophy", "Tournaments")).get());
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
        System.out.println("Profile menu item clicked!");


        User currentUser = UserSession.getInstance().getCurrentUser();

        // Create a dialog
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);

        // Set the header text
        dialog.setHeaderText("Edit your profile information");

        // Create the form content
        dialog.getDialogPane().setContent(createProfileForm(currentUser));

        // Add buttons (Save and Cancel)
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Handle the Save button action
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                // Save logic here
                System.out.println("Profile saved!");

                // Update the user in the database
                UserService userService = new UserService();
                userService.update(currentUser);

                System.out.println("Profile updated in the database!");
                return true; // Return true to indicate success
            }
            return false; // Return false for Cancel
        });

        // Show the dialog and wait for a response
        dialog.showAndWait().ifPresent(saved -> {
            if (saved) {
                System.out.println("Profile updated successfully!");
            } else {
                System.out.println("Profile update failed.");
            }
        });
    }


    private Node createProfileForm(User currentUser) {
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        // First Name
        MFXTextField firstNameField = new MFXTextField();
        firstNameField.setFloatingText("First Name");
        firstNameField.setText(currentUser.getFirstname());

        // Last Name
        MFXTextField lastNameField = new MFXTextField();
        lastNameField.setFloatingText("Last Name");
        lastNameField.setText(currentUser.getLastName());

        // Email
        MFXTextField emailField = new MFXTextField();
        emailField.setFloatingText("Email");
        emailField.setText(currentUser.getEmail());

        // Phone Number
        MFXTextField phoneNumberField = new MFXTextField();
        phoneNumberField.setFloatingText("Phone Number");
        phoneNumberField.setText(currentUser.getPhoneNumber());

        // Date of Birth
        MFXDatePicker dateOfBirthPicker = new MFXDatePicker();
        dateOfBirthPicker.setFloatingText("Date of Birth");
        dateOfBirthPicker.setValue(currentUser.getDateOfBirth());

        // Profile Picture (File Chooser)
        MFXButton uploadPictureButton = new MFXButton("Upload Profile Picture");
        uploadPictureButton.setOnAction(event -> handleUploadProfilePicture(currentUser));

        // Add fields to the form
        form.getChildren().addAll(
                new Label("First Name:"), firstNameField,
                new Label("Last Name:"), lastNameField,
                new Label("Email:"), emailField,
                new Label("Phone Number:"), phoneNumberField,
                new Label("Date of Birth:"), dateOfBirthPicker,
                uploadPictureButton
        );

        // Update the user object when fields change
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> currentUser.setFirstname(newVal));
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> currentUser.setLastName(newVal));
        emailField.textProperty().addListener((obs, oldVal, newVal) -> currentUser.setEmail(newVal));
        phoneNumberField.textProperty().addListener((obs, oldVal, newVal) -> currentUser.setPhoneNumber(newVal));
        dateOfBirthPicker.valueProperty().addListener((obs, oldVal, newVal) -> currentUser.setDateOfBirth(newVal));

        return form;
    }

    private void handleUploadProfilePicture(User currentUser) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());

        if (selectedFile != null) {
            // Handle the selected file (e.g., save the path or upload the image)
            currentUser.setProfilePicture(selectedFile.getAbsolutePath());
            System.out.println("Profile picture uploaded: " + selectedFile.getAbsolutePath());
        }
    }






}