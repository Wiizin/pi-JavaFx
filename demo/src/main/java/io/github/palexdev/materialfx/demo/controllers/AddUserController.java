package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.demo.model.Organizer;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.dialogs.*;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.materialfx.demo.services.UltraMsgApi;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
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
    private void handleBrowseImage() {
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
                profilePictureField.setText(relativePath);

                // For display, we need the full URL
                String imageUrl = destinationFile.toURI().toString();
                Image image = new Image(imageUrl, 100, 100, true, true);
                profilePicturePreview.setImage(image);
                profilePicturePreview.setFitWidth(100);
                profilePicturePreview.setFitHeight(100);

                // Make the preview circular
                Circle clip = new Circle(70, 70, 70);
                profilePicturePreview.setClip(clip);
            } catch (Exception e) {
                e.printStackTrace();
                showMaterialFXAlert(
                        "Error",
                        "Failed to save image: " + e.getMessage(),
                        "fas-circle-xmark",
                        "mfx-error-dialog"
                );
            }
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
                updatedIconCode = "fas-circle-info";
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
        
        // Wait for FXML to be fully loaded before populating fields
        Platform.runLater(() -> {
            populateFields();
            
            // Update the role combo box and related fields
            if (user.getRole() != null) {
                roleComboBox.setValue(user.getRole().toUpperCase());
                updateOrganizerFields("organizer".equalsIgnoreCase(user.getRole()));
            }
            
            // Load the profile picture
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                try {
                    String fileName = user.getProfilePicture();
                    String fullPath;
                    
                    if (fileName.equals("default_profile.jpg")) {
                        URL resourceUrl = getClass().getResource("/default_profile.jpg");
                        if (resourceUrl != null) {
                            fullPath = resourceUrl.toExternalForm();
                        } else {
                            throw new IOException("Default profile image not found");
                        }
                    } else {
                        String userHome = System.getProperty("user.home");
                        File imageFile = new File(userHome + "/sportify/uploads/images/" + fileName);
                        if (!imageFile.exists()) {
                            throw new IOException("Profile image not found");
                        }
                        fullPath = imageFile.toURI().toString();
                    }
                    
                    Image image = new Image(fullPath, 100, 100, true, true);
                    profilePicturePreview.setImage(image);
                    profilePicturePreview.setFitWidth(100);
                    profilePicturePreview.setFitHeight(100);
                    
                    // Make the preview circular
                    Circle clip = new Circle(50, 50, 50);
                    profilePicturePreview.setClip(clip);
                    
                    // Set the profile picture field
                    profilePictureField.setText(fileName);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    setDefaultProfileImage();
                }
            } else {
                setDefaultProfileImage();
            }
        });
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
            
            // Handle organizer-specific fields
            if (userToUpdate instanceof Organizer) {
                Organizer organizer = (Organizer) userToUpdate;
                coachingLicenseField.setText(organizer.getCoachingLicense());
                isActiveCheckBox.setSelected(organizer.isActive());
                
                // Make organizer fields visible
                coachingLicenseField.setVisible(true);
                coachingLicenseField.setManaged(true);
                isActiveCheckBox.setVisible(true);
                isActiveCheckBox.setManaged(true);
            } else {
                // Hide organizer fields for non-organizers
                coachingLicenseField.setVisible(false);
                coachingLicenseField.setManaged(false);
                isActiveCheckBox.setVisible(false);
                isActiveCheckBox.setManaged(false);
            }
        }
    }

    private void setDefaultProfileImage() {
        String defaultImagePath = "default_profile.jpg";
        profilePictureField.setText(defaultImagePath);

        try {
            String defaultImageUrl = Objects.requireNonNull(getClass().getResource("/" + defaultImagePath)).toExternalForm();
            Image defaultImage = new Image(defaultImageUrl, 100, 100, true, true);
            profilePicturePreview.setImage(defaultImage);
            profilePicturePreview.setFitWidth(100);
            profilePicturePreview.setFitHeight(100);

            // Make the preview circular
            Circle clip = new Circle(50, 50, 50);
            profilePicturePreview.setClip(clip);
        } catch (Exception e) {
            e.printStackTrace();
            showMaterialFXAlert(
                    "Error",
                    "Failed to load default profile image: " + e.getMessage(),
                    "fas-circle-xmark",
                    "mfx-error-dialog"
            );
        }
    }

    @FXML
    private void handleSaveUser() {
        ValidationResult validationResult = validateInputs();
        if (!validationResult.isValid()) {
            showMaterialFXAlert(
                    "Validation Error",
                    validationResult.message(),
                    "fas-circle-xmark",
                    "mfx-error-dialog"
            );
            return;
        }

        try {
            User user;
            String selectedRole = roleComboBox.getValue().toLowerCase();

            // Create or update the appropriate user type
            if ("organizer".equals(selectedRole)) {
                boolean wasActive = false;

                // Check if this is an update and the status is changing from inactive to active
                if (mode == Mode.UPDATE && userToUpdate instanceof Organizer) {
                    Organizer existingOrganizer = (Organizer) userToUpdate;
                    wasActive = existingOrganizer.isActive();
                    user = existingOrganizer;
                } else {
                    user = new Organizer();
                }

                Organizer organizer = (Organizer) user;
                organizer.setCoachingLicense(coachingLicenseField.getText().trim());
                organizer.setActive(isActiveCheckBox.isSelected());

                // Check if status changed from inactive to active
                boolean isNowActive = isActiveCheckBox.isSelected();
                if (!wasActive && isNowActive && mode == Mode.UPDATE) {
                    // Status changed from inactive to active - send WhatsApp message
                    String phoneNumber = phoneField.getText().trim();
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        String message = "Congratulations! Your organizer account has been approved. You can now log in and start using all features.";
                        try {
                            UltraMsgApi.sendSMS(phoneNumber, message);
                        } catch (Exception e) {
                            // Log the error but continue with the save process
                            System.err.println("Failed to send WhatsApp message: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                user = (mode == Mode.UPDATE) ? userToUpdate : new User();
                user.setActive(true);
            }

            // Set common fields
            user.setFirstname(firstNameField.getText().trim());
            user.setLastName(lastNameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setPassword(passwordField.getText());
            user.setRole(selectedRole);
            user.setPhoneNumber(phoneField.getText().trim());
            user.setDateOfBirth(dateOfBirthPicker.getValue());

            // Handle profile picture
            String profilePicturePath = profilePictureField.getText().trim();
            if (profilePicturePath.isEmpty()) {
                profilePicturePath = "default_profile.jpg";
            } else if (profilePicturePath.contains("/") || profilePicturePath.contains("\\")) {
                // Extract just the filename if it's a full path
                profilePicturePath = profilePicturePath.substring(profilePicturePath.lastIndexOf(File.separator) + 1);
            }
            user.setProfilePicture(profilePicturePath);

            if (mode == Mode.ADD) {
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userService.create(user);
                showMaterialFXAlert("Success", "User created successfully!", "fas-circle-check", "mfx-success-dialog");

            } else {
                user.setId(userToUpdate.getId());
                user.setCreatedAt(userToUpdate.getCreatedAt());
                user.setUpdatedAt(LocalDateTime.now());
                userService.update(user);
                showMaterialFXAlert("Success", "User updated successfully!", "fas-circle-check", "mfx-success-dialog");
            }

            // Close the form
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showMaterialFXAlert(
                    "Error",
                    "Failed to " + (mode == Mode.ADD ? "create" : "update") + " user: " + e.getMessage(),
                    "fas-circle-xmark",
                    "mfx-error-dialog"
            );
        }
    }
}