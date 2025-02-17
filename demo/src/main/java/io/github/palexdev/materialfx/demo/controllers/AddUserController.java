package io.github.palexdev.materialfx.demo.controllers;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalDate;
import java.util.Map;

public class AddUserController {

    @FXML
    private MFXTextField firstNameField;

    @FXML
    private MFXTextField lastNameField;

    @FXML
    private MFXTextField emailField;

    @FXML
    private MFXTextField passwordField;

    @FXML
    private MFXFilterComboBox<String> roleComboBox;

    @FXML
    private MFXTextField phoneField;

    @FXML
    private MFXDatePicker dateOfBirthPicker;

    @FXML
    private MFXTextField profilePictureField;

    @FXML
    private ImageView profilePicturePreview;

    @FXML
    private VBox rootPane; // Use VBox as the root pane

    private UserService userService = new UserService();

    private MFXGenericDialog dialogContent;
    private MFXStageDialog dialog;
    private enum Mode { ADD, UPDATE }
    private Mode mode = Mode.ADD; // Default mode is ADD
    private User userToUpdate;

    public void setUserToUpdate(User user) {
        this.userToUpdate = user;
        this.mode = Mode.UPDATE; // Switch to UPDATE mode
        populateFields(); // Populate fields with the user's data
    }
    private void populateFields() {
        if (userToUpdate != null) {
            firstNameField.setText(userToUpdate.getFirstname());
            lastNameField.setText(userToUpdate.getLastName());
            emailField.setText(userToUpdate.getEmail());
            passwordField.setText(userToUpdate.getPassword());
            roleComboBox.selectItem(userToUpdate.getRole());
            phoneField.setText(userToUpdate.getPhoneNumber());
            dateOfBirthPicker.setValue(userToUpdate.getDateOfBirth());
            profilePictureField.setText(userToUpdate.getProfilePicture());

            if (userToUpdate.getProfilePicture() != null && !userToUpdate.getProfilePicture().isEmpty()) {
                Image image = new Image(new File(userToUpdate.getProfilePicture()).toURI().toString());
                profilePicturePreview.setImage(image);
            }
        }
    }

    @FXML
    public void initialize() {
        // Delay the dialog initialization until the scene is ready
        Platform.runLater(() -> {
            Stage ownerStage = (Stage) rootPane.getScene().getWindow();
            this.dialogContent = MFXGenericDialogBuilder.build()
                    .setContentText("")
                    .makeScrollable(true)
                    .get();
            this.dialog = MFXGenericDialogBuilder.build(dialogContent)
                    .toStageDialogBuilder()
                    .initOwner(ownerStage)
                    .initModality(Modality.APPLICATION_MODAL)
                    .setDraggable(true)
                    .setTitle("Alert")
                    .setOwnerNode(rootPane) // Set the ownerNode to rootPane
                    .setScrimPriority(ScrimPriority.WINDOW)
                    .setScrimOwner(true)
                    .get();

            // Add a close button to the dialog
            dialogContent.addActions(
                    Map.entry(new MFXButton("Confirm"), event -> {
                    }),
                    Map.entry(new MFXButton("Cancel"), event -> dialog.close())
            );

            dialogContent.setMaxSize(400, 200);
        });
    }

    @FXML

    private void handleSaveUser() {
        // Validate inputs (uncomment and implement your validation logic)
        // if (firstNameField.getText().isEmpty() || ...) {
        //     showMaterialFXAlert("Error", "Please fill all fields.", "fas-circle-info", "mfx-error-dialog");
        //     return;
        // }

        // Get values from the form
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = (String) roleComboBox.getSelectedItem();
        String phone = phoneField.getText();
        String profilePicture = profilePictureField.getText();
        LocalDate dateOfBirth = dateOfBirthPicker.getValue();

        try {
            if (mode == Mode.ADD) {
                // Create a new User object
                User newUser = new User();
                newUser.setFirstname(firstName);
                newUser.setLastName(lastName);
                newUser.setEmail(email);
                newUser.setPassword(password);
                newUser.setRole(role);
                newUser.setPhoneNumber(phone);
                newUser.setDateOfBirth(dateOfBirth);
                newUser.setProfilePicture(profilePicture);

                // Save the user
                userService.create(newUser);

                // Show success message
                showMaterialFXAlert("Success", "User added successfully!", "fas-check-circle", "mfx-success-dialog");
            } else if (mode == Mode.UPDATE && userToUpdate != null) {
                // Update the existing user
                userToUpdate.setFirstname(firstName);
                userToUpdate.setLastName(lastName);
                userToUpdate.setEmail(email);
                userToUpdate.setPassword(password);
                userToUpdate.setRole(role);
                userToUpdate.setPhoneNumber(phone);
                userToUpdate.setDateOfBirth(dateOfBirth);
                userToUpdate.setProfilePicture(profilePicture);

                // Update the user in the database
                userService.update(userToUpdate);

                // Show success message
                showMaterialFXAlert("Success", "User updated successfully!", "fas-check-circle", "mfx-success-dialog");
            }

            // Delay closing the window for 1 second after the alert is shown
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> {
                // Close the window
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.close();
            });
            delay.play();

        } catch (Exception e) {
            System.out.println("Error saving user: " + e.getMessage()); // Debugging
            e.printStackTrace();

            // Show error message
            showMaterialFXAlert("Error", "An error occurred while saving the user: " + e.getMessage(), "fas-circle-info", "mfx-error-dialog");
        }
    }



    @FXML
    private void handleCancel() {
        // Close the pop-up window
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(profilePicturePreview.getScene().getWindow());
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            profilePicturePreview.setImage(image);
            profilePictureField.setText(file.getAbsolutePath());
        }
    }

    private void showMaterialFXAlert(String title, String message, String iconCode, String styleClass) {
        // Create an MFXFontIcon for the alert
        MFXFontIcon icon = new MFXFontIcon(iconCode, 32); // Use the provided icon code

        // Set the dialog content
        dialogContent.setHeaderText(title);
        dialogContent.setContentText(message);
        dialogContent.setHeaderIcon(icon); // Set the icon

        // Apply the appropriate style class
        convertDialogTo(styleClass);

        // Show the dialog
        dialog.showAndWait();
    }
    private void convertDialogTo(String styleClass) {
        // Remove existing style classes
        dialogContent.getStyleClass().removeIf(
                s -> s.equals("mfx-info-dialog") || s.equals("mfx-warn-dialog") || s.equals("mfx-error-dialog")
        );

        // Add the new style class if provided
        if (styleClass != null) {
            dialogContent.getStyleClass().add(styleClass);
        }
    }

}