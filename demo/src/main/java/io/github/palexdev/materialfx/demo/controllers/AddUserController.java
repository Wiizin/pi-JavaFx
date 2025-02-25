package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.demo.model.Organizer;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.dialogs.*;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Pattern;

public class AddUserController {
    @FXML private MFXTextField firstNameField;
    @FXML private MFXTextField lastNameField;
    @FXML private MFXTextField emailField;
    @FXML private MFXTextField passwordField;
    @FXML private MFXFilterComboBox<String> roleComboBox;
    @FXML private MFXTextField phoneField;
    @FXML private MFXDatePicker dateOfBirthPicker;
    @FXML private MFXTextField profilePictureField;
    @FXML private ImageView profilePicturePreview;
    @FXML private VBox rootPane;

    private UserService userService = new UserService();
    private MFXGenericDialog dialogContent;
    private MFXStageDialog dialog;
    @FXML
    private MFXTextField coachingLicenseField;
    @FXML
    private MFXCheckbox isActiveCheckBox;

    private enum Mode { ADD, UPDATE }
    private Mode mode = Mode.ADD;
    private User userToUpdate;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9][0-9]{7,14}$");

    private record ValidationResult(boolean isValid, String message) {}

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            // Set stage properties
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setWidth(600);  // Set fixed width
            stage.setHeight(800); // Set fixed height
            stage.setResizable(false); // Make it non-resizable
            stage.centerOnScreen(); // Center on screen

            initializeDialog();
            setupValidationListeners();
            initializeFields();
        });

        // Add listener to roleComboBox for handling organizer-specific fields
        roleComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;
            
            boolean isOrganizer = "organizer".equalsIgnoreCase(newValue);
            updateOrganizerFields(isOrganizer);
        });
    }

    private void initializeDialog() {
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
                .setTitle("Notification")
                .setOwnerNode(rootPane)
                .setScrimPriority(ScrimPriority.WINDOW)
                .setScrimOwner(true)
                .get();

        MFXButton closeButton = new MFXButton("Close");
        closeButton.setOnAction(event -> dialog.close());


        dialogContent.addActions(Map.entry(closeButton, event -> dialog.close()));
        dialogContent.setMaxSize(400, 200);
    }

    private void setupValidationListeners() {
        emailField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.isEmpty() && !EMAIL_PATTERN.matcher(newValue).matches()) {
                emailField.setStyle("-fx-border-color: #FF0000;");
            } else {
                emailField.setStyle("");
            }
        });

        passwordField.textProperty().addListener((obs, old, newValue) -> {
            boolean isValid = newValue.length() >= 8
                    && newValue.matches(".*[A-Z].*")
                    && newValue.matches(".*[a-z].*")
                    && newValue.matches(".*\\d.*");

            passwordField.setStyle(isValid || newValue.isEmpty() ? "" : "-fx-border-color: #FF0000;");
        });

        phoneField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.isEmpty() && !PHONE_PATTERN.matcher(newValue).matches()) {
                phoneField.setStyle("-fx-border-color: #FF0000;");
            } else {
                phoneField.setStyle("");
            }
        });
    }

    private void initializeFields() {
        // Set floating text labels
        firstNameField.setFloatingText("First Name");
        lastNameField.setFloatingText("Last Name");
        emailField.setFloatingText("Email Address");
        passwordField.setFloatingText("Password");
        roleComboBox.setFloatingText("Role");
        phoneField.setFloatingText("Phone Number");
        dateOfBirthPicker.setFloatingText("Date of Birth");
        profilePictureField.setFloatingText("Profile Picture");
        coachingLicenseField.setFloatingText("Coaching License");

        // Initialize role combo box only if it's empty
        if (roleComboBox.getItems().isEmpty()) {
            roleComboBox.getItems().addAll("admin", "player", "organizer");
        }

        // Only set default role for new users
        if (mode == Mode.ADD) {
            roleComboBox.selectItem("player"); // Set default role only for new users
        }

        // Initially hide organizer-specific fields
        coachingLicenseField.setVisible(false);
        coachingLicenseField.setManaged(false);
        isActiveCheckBox.setVisible(false);
        isActiveCheckBox.setManaged(false);
    }

    private ValidationResult validateInputs() {
        if (firstNameField.getText().trim().isEmpty()) {
            return new ValidationResult(false, "First name is required");
        }
        if (firstNameField.getText().length() < 2) {
            return new ValidationResult(false, "First name must be at least 2 characters");
        }

        if (lastNameField.getText().trim().isEmpty()) {
            return new ValidationResult(false, "Last name is required");
        }
        if (lastNameField.getText().length() < 2) {
            return new ValidationResult(false, "Last name must be at least 2 characters");
        }

        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            return new ValidationResult(false, "Email address is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new ValidationResult(false, "Please enter a valid email address");
        }

        String password = passwordField.getText();
        if (password.isEmpty()) {
            return new ValidationResult(false, "Password is required");
        }
        if (password.length() < 8) {
            return new ValidationResult(false, "Password must be at least 8 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            return new ValidationResult(false, "Password must include an uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            return new ValidationResult(false, "Password must include a lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            return new ValidationResult(false, "Password must include a number");
        }

        if (roleComboBox.getSelectedItem() == null) {
            return new ValidationResult(false, "Please select a role");
        }

        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            return new ValidationResult(false, "Please enter a valid phone number");
        }

        LocalDate dob = dateOfBirthPicker.getValue();
        if (dob == null) {
            return new ValidationResult(false, "Date of birth is required");
        }
        if (dob.isAfter(LocalDate.now().minusYears(18))) {
            return new ValidationResult(false, "User must be at least 18 years old");
        }

        // Validate coaching license only for organizers
        if ("organizer".equalsIgnoreCase(roleComboBox.getSelectedItem()) && 
            coachingLicenseField.getText().trim().isEmpty()) {
            return new ValidationResult(false, "Coaching license is required for organizers");
        }

        return new ValidationResult(true, "");
    }

    @FXML
    private void handleSaveUser() {
        ValidationResult validationResult = validateInputs();
        if (!validationResult.isValid()) {
            showMaterialFXAlert("Validation Error", validationResult.message(), "fas-triangle-exclamation", "mfx-error-dialog");
            return;
        }

        try {
            User user = mode == Mode.ADD ? new User() : userToUpdate;
            String selectedRole = roleComboBox.getSelectedItem();

            // Handle role-specific user type
            if ("organizer".equalsIgnoreCase(selectedRole)) {
                Organizer organizer;
                if (user instanceof Organizer) {
                    organizer = (Organizer) user;
                } else {
                    organizer = new Organizer();
                    // Copy common fields if updating
                    if (mode == Mode.UPDATE) {
                        organizer.setId(user.getId());
                        organizer.setCreatedAt(user.getCreatedAt());
                    }
                }
                organizer.setCoachingLicense(coachingLicenseField.getText().trim());
                // Explicitly set the active status from checkbox
                boolean isActive = isActiveCheckBox.isSelected();
                System.out.println("Setting organizer active status to: " + isActive); // Debug log
                organizer.setActive(isActive);
                user = organizer;
            } else {
                // For admin and player users, always set active to true
                user.setActive(true);
            }

            // Set common fields
            user.setFirstname(firstNameField.getText().trim());
            user.setLastName(lastNameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setPassword(passwordField.getText());
            user.setRole(selectedRole.toLowerCase());
            user.setPhoneNumber(phoneField.getText().trim());
            user.setDateOfBirth(dateOfBirthPicker.getValue());

            // Handle profile picture
            String profilePicturePath = profilePictureField.getText().trim();
            if (!profilePicturePath.isEmpty()) {
                try {
                    File imageFile = new File(profilePicturePath);
                    BufferedImage bufferedImage = ImageIO.read(imageFile);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpg", baos);
                    user.setProfilePicture(baos.toByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                    user.setProfilePicture(new byte[0]);
                }
            } else {
                user.setProfilePicture(new byte[0]);
            }

            if (mode == Mode.ADD) {
                userService.create(user);
                showMaterialFXAlert("Success", "User successfully added", "fas-circle-check", "mfx-success-dialog");
            } else {
                System.out.println("Updating user with active status: " + user.isActive()); // Debug log
                userService.update(user);
                showMaterialFXAlert("Success", "User successfully updated", "fas-circle-check", "mfx-success-dialog");
            }

            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> {
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.close();
            });
            delay.play();

        } catch (Exception e) {
            e.printStackTrace();
            showMaterialFXAlert("Error", "Failed to save user: " + e.getMessage(), "fas-circle-x", "mfx-error-dialog");
        }
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(profilePicturePreview.getScene().getWindow());
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            profilePicturePreview.setImage(image);
            profilePictureField.setText(file.getAbsolutePath());
        }
    }

    private void showMaterialFXAlert(String title, String message, String iconCode, String styleClass) {
        // Update icon codes to use correct MaterialFX FontAwesome icons
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
                updatedIconCode = "fas-info-circle";
        }

        MFXFontIcon icon = new MFXFontIcon(updatedIconCode, 32);

        dialogContent.setHeaderText(title);
        dialogContent.setContentText(message);
        dialogContent.setHeaderIcon(icon);

        convertDialogTo(styleClass);

        dialog.showAndWait();
    }

    private void convertDialogTo(String styleClass) {
        dialogContent.getStyleClass().removeIf(s ->
                s.equals("mfx-info-dialog") ||
                        s.equals("mfx-warn-dialog") ||
                        s.equals("mfx-error-dialog") ||
                        s.equals("mfx-success-dialog")
        );

        if (styleClass != null) {
            dialogContent.getStyleClass().add(styleClass);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    public void setUserToUpdate(User user) {
        this.userToUpdate = user;
        this.mode = Mode.UPDATE;
        populateFields();
    }

    private void updateOrganizerFields(boolean isOrganizer) {
        coachingLicenseField.setVisible(isOrganizer);
        coachingLicenseField.setManaged(isOrganizer);
        isActiveCheckBox.setVisible(isOrganizer);
        isActiveCheckBox.setManaged(isOrganizer);
        
        if (isOrganizer) {
            if (mode == Mode.ADD) {
                isActiveCheckBox.setSelected(false); // New organizers start as inactive
                coachingLicenseField.clear();
            } else if (mode == Mode.UPDATE && userToUpdate instanceof Organizer) {
                // Restore previous organizer data during update
                Organizer organizer = (Organizer) userToUpdate;
                coachingLicenseField.setText(organizer.getCoachingLicense());
                isActiveCheckBox.setSelected(organizer.isActive());
                System.out.println("Restoring organizer data - License: " + organizer.getCoachingLicense() + 
                                 ", Active: " + organizer.isActive());
            }
        } else {
            coachingLicenseField.clear();
            isActiveCheckBox.setSelected(true); // Non-organizers are always active
        }
    }

    private void populateFields() {
        if (userToUpdate != null) {
            firstNameField.setText(userToUpdate.getFirstname());
            lastNameField.setText(userToUpdate.getLastName());
            emailField.setText(userToUpdate.getEmail());
            passwordField.setText(userToUpdate.getPassword());
            phoneField.setText(userToUpdate.getPhoneNumber());
            dateOfBirthPicker.setValue(userToUpdate.getDateOfBirth());

            // Ensure the roleComboBox is populated before selecting an item
            if (roleComboBox.getItems().isEmpty()) {
                roleComboBox.getItems().addAll("admin", "player", "organizer");
            }

            // Get the role and handle organizer-specific fields
            String role = userToUpdate.getRole();
            if (role != null) {
                System.out.println("Setting role for update: " + role); // Debug log
                boolean isOrganizer = "organizer".equalsIgnoreCase(role);
                
                // Set the role first
                roleComboBox.selectItem(role.toLowerCase());
                
                // Manually update organizer fields
                updateOrganizerFields(isOrganizer);
                
                // Then populate organizer-specific fields if applicable
                if (isOrganizer && userToUpdate instanceof Organizer) {
                    Organizer organizer = (Organizer) userToUpdate;
                    coachingLicenseField.setText(organizer.getCoachingLicense());
                    boolean isActive = organizer.isActive();
                    System.out.println("Setting organizer active status to: " + isActive); // Debug log
                    isActiveCheckBox.setSelected(isActive);
                }
            }
        }
    }
}