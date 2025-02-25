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
        //loader.addView(MFXLoaderBean.of("Dashboard", loadURL("fxml/addUser.fxml")).setBeanToNodeMapper(() -> createToggle("fas-circle-dot", "Dashboard")).setDefaultRoot(true).get());
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
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.UNDECORATED);

        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #1B1F3B; -fx-border-radius: 10px; -fx-background-radius: 10px ;-fx-border-color: #ff9800;-fx-border-width: 2;");

        String textFieldStyle = "-fx-text-fill: white; -fx-prompt-text-fill: white; -fx-background-color: #2A2F4F; -fx-border-color: #ff9800;";

        // Get the current user
        User currentUser = UserSession.getInstance().getCurrentUser();

        // Create circular ImageView for profile picture
        ImageView profileImageView = new ImageView();
        profileImageView.setFitWidth(100); // Set the desired width
        profileImageView.setFitHeight(100); // Set the desired height
        profileImageView.setPreserveRatio(true);

        // Create a circular clip
        Circle clip = new Circle(50, 50, 50); // Center X, Center Y, Radius
        profileImageView.setClip(clip);

        // Load the user's profile picture (or default if none exists)
        if (currentUser.getProfilePicture() != null) {
            // Load the user's profile picture from byte array
            ByteArrayInputStream inputStream = new ByteArrayInputStream(currentUser.getProfilePicture());
            Image userImage = new Image(inputStream);
            profileImageView.setImage(userImage);
        } else {
            // Load the default profile picture
            Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/default_profile.jpg"))); // Path to default image
            profileImageView.setImage(defaultImage);
        }

        profileImageView.getStyleClass().add("profile-image");

        // Add a button to upload a new profile picture
        MFXButton uploadImageButton = new MFXButton("Change Picture");
        uploadImageButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        uploadImageButton.setOnAction(e -> handleImageUpload(profileImageView));

        // Create form fields
        MFXTextField firstNameField = new MFXTextField();
        firstNameField.setStyle(textFieldStyle);
        firstNameField.setFloatingText("First Name");
        firstNameField.setText(currentUser.getFirstname());
        firstNameField.setPrefWidth(300);
        firstNameField.setPrefHeight(40);

        MFXTextField lastNameField = new MFXTextField();
        lastNameField.setStyle(textFieldStyle);
        lastNameField.setFloatingText("Last Name");
        lastNameField.setText(currentUser.getLastName());
        lastNameField.setPrefWidth(300);
        lastNameField.setPrefHeight(40);

        MFXTextField emailField = new MFXTextField();
        emailField.setStyle(textFieldStyle);
        emailField.setFloatingText("Email");
        emailField.setText(currentUser.getEmail());
        emailField.setPrefWidth(300);
        emailField.setPrefHeight(40);

        MFXTextField phoneField = new MFXTextField();
        phoneField.setStyle(textFieldStyle);
        phoneField.setFloatingText("Phone Number");
        phoneField.setText(currentUser.getPhoneNumber());
        phoneField.setPrefWidth(300);
        phoneField.setPrefHeight(40);

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
                // Update the user object with new values
                currentUser.setFirstname(firstNameField.getText());
                currentUser.setLastName(lastNameField.getText());
                currentUser.setEmail(emailField.getText());
                currentUser.setPhoneNumber(phoneField.getText());

                // Convert the ImageView to a byte array and save it
                byte[] imageData = imageViewToByteArray(profileImageView);
                if (imageData != null) {
                    currentUser.setProfilePicture(imageData);
                }

                // Save to database
                UserService userService = new UserService();
                userService.update(currentUser);

                // Close the popup
                popupStage.close();

                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");

                // Reload the profile image in the main view
                reloadProfileImage(profileImageView); // Pass the ImageView here
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile: " + ex.getMessage());
            }
        });

        // Handle cancel button
        cancelButton.setOnAction(e -> popupStage.close());

        Scene scene = new Scene(form, 700, 700);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void reloadProfileImage(ImageView profileImageView) {
        User currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser.getProfilePicture() != null) {
            // Load the user's profile picture from byte array
            ByteArrayInputStream inputStream = new ByteArrayInputStream(currentUser.getProfilePicture());
            Image userImage = new Image(inputStream);
            profileImageView.setImage(userImage);
        } else {
            // Load the default profile picture
            Image defaultImage = new Image(getClass().getResourceAsStream("/default_profile.jpg")); // Path to default image
            profileImageView.setImage(defaultImage);
        }
    }


    private void handleImageUpload(ImageView profileImageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Load the original image
                BufferedImage originalImage = ImageIO.read(selectedFile);
                
                // Create the image editor window
                Stage editorStage = new Stage();
                editorStage.initModality(Modality.APPLICATION_MODAL);
                editorStage.setTitle("Edit Profile Picture");

                VBox editorRoot = new VBox(10);
                editorRoot.setAlignment(Pos.CENTER);
                editorRoot.setPadding(new Insets(20));
                editorRoot.setStyle("-fx-background-color: #1B1F3B;");

                // Create ImageView for preview
                ImageView previewImageView = new ImageView();
                previewImageView.setFitWidth(200);
                previewImageView.setFitHeight(200);
                previewImageView.setPreserveRatio(true);
                
                // Convert BufferedImage to JavaFX Image
                Image fxImage = SwingFXUtils.toFXImage(originalImage, null);
                previewImageView.setImage(fxImage);

                // Add size controls
                Label sizeLabel = new Label("Image Size: 200px");
                sizeLabel.setStyle("-fx-text-fill: white;");
                
                MFXSlider sizeSlider = new MFXSlider();
                sizeSlider.setMin(50);
                sizeSlider.setMax(400);
                sizeSlider.setValue(200);
                sizeSlider.setPrefWidth(300);
                sizeSlider.setStyle("-fx-text-fill: white;");
                
                // Update image size when slider changes
                sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    double size = newVal.doubleValue();
                    previewImageView.setFitWidth(size);
                    previewImageView.setFitHeight(size);
                    sizeLabel.setText(String.format("Image Size: %.0fpx", size));
                });

                // Add quality control
                Label qualityLabel = new Label("Image Quality: 80%");
                qualityLabel.setStyle("-fx-text-fill: white;");
                
                MFXSlider qualitySlider = new MFXSlider();
                qualitySlider.setMin(10);
                qualitySlider.setMax(100);
                qualitySlider.setValue(80);
                qualitySlider.setPrefWidth(300);
                qualitySlider.setStyle("-fx-text-fill: white;");
                
                // Update quality label when slider changes
                qualitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    qualityLabel.setText(String.format("Image Quality: %.0f%%", newVal.doubleValue()));
                });

                // Add buttons
                HBox buttonBox = new HBox(10);
                buttonBox.setAlignment(Pos.CENTER);

                MFXButton cropButton = new MFXButton("Crop");
                cropButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
                
                MFXButton rotateButton = new MFXButton("Rotate");
                rotateButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
                rotateButton.setOnAction(e -> {
                    previewImageView.setRotate(previewImageView.getRotate() + 90);
                });

                MFXButton saveButton = new MFXButton("Save");
                saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                saveButton.setOnAction(e -> {
                    try {
                        // Get the final image with all transformations
                        BufferedImage finalImage = createTransformedImage(previewImageView, 
                            qualitySlider.getValue() / 100.0); // Convert percentage to decimal
                        
                        // Update the profile image view
                        Image processedImage = SwingFXUtils.toFXImage(finalImage, null);
                        profileImageView.setImage(processedImage);
                        
                        // Save to database
                        UserService userService = new UserService();
                        User currentUser = UserSession.getInstance().getCurrentUser();
                        currentUser.setProfilePicture(imageToByteArray(finalImage, qualitySlider.getValue() / 100.0));
                        userService.update(currentUser);
                        
                        editorStage.close();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Profile picture updated successfully!");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to save profile picture.");
                    }
                });

                MFXButton cancelButton = new MFXButton("Cancel");
                cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                cancelButton.setOnAction(e -> editorStage.close());

                buttonBox.getChildren().addAll(cropButton, rotateButton, saveButton, cancelButton);

                editorRoot.getChildren().addAll(
                    previewImageView,
                    sizeLabel,
                    sizeSlider,
                    qualityLabel,
                    qualitySlider,
                    buttonBox
                );

                Scene editorScene = new Scene(editorRoot);
                editorStage.setScene(editorScene);
                editorStage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load image.");
            }
        }
    }

    private BufferedImage createTransformedImage(ImageView imageView, double quality) {
        try {
            // Get the displayed image
            Image fxImage = imageView.getImage();
            
            // Create a new BufferedImage with the desired dimensions
            BufferedImage resizedImage = new BufferedImage(
                (int) imageView.getFitWidth(),
                (int) imageView.getFitHeight(),
                BufferedImage.TYPE_INT_RGB
            );

            // Draw the image with transformations
            Graphics2D g2d = resizedImage.createGraphics();
            
            // Apply rotation if any
            if (imageView.getRotate() != 0) {
                g2d.rotate(Math.toRadians(imageView.getRotate()), 
                    resizedImage.getWidth() / 2, 
                    resizedImage.getHeight() / 2);
            }

            // Draw the image
            g2d.drawImage(
                SwingFXUtils.fromFXImage(fxImage, null),
                0, 0,
                (int) imageView.getFitWidth(),
                (int) imageView.getFitHeight(),
                null
            );
            
            g2d.dispose();
            return resizedImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] imageToByteArray(BufferedImage image, double quality) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // Get JPG writer
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();
        
        // Set up output
        ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
        writer.setOutput(ios);
        
        // Set the compression quality
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality((float) quality);
        
        // Write the image
        writer.write(null, new IIOImage(image, null, null), param);
        
        // Cleanup
        ios.close();
        writer.dispose();
        
        return outputStream.toByteArray();
    }

    private byte[] imageViewToByteArray(ImageView imageView) {
        if (imageView.getImage() == null) {
            return null;
        }

        // Step 1: Convert the JavaFX Image to a BufferedImage
        Image image = imageView.getImage();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

        // Step 2: Ensure the BufferedImage is in the sRGB color space
        BufferedImage convertedImage = new BufferedImage(
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                BufferedImage.TYPE_INT_RGB // Force RGB color space
        );

        // Draw the original image onto the converted image
        Graphics2D g = convertedImage.createGraphics();
        g.drawImage(bufferedImage, 0, 0, null);
        g.dispose();

        // Step 3: Write the converted image to a byte array in JPEG format
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            // Get the JPEG image writer
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (!writers.hasNext()) {
                throw new IllegalStateException("No JPEG image writers found!");
            }
            ImageWriter writer = writers.next();

            // Configure compression settings
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.7f); // Adjust quality (0.0-1.0)

            // Write the image to the output stream
            ImageOutputStream outputStream = ImageIO.createImageOutputStream(stream);
            writer.setOutput(outputStream);
            writer.write(null, new IIOImage(convertedImage, null, null), param);

            // Clean up
            writer.dispose();
            outputStream.close();
            return stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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