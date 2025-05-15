package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.materialfx.demo.model.Organizer;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.StringFilter;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.utils.others.observables.When;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class AdminHomeController implements Initializable {

    @FXML
    private MFXTableColumn<User> isActiveColumn;
    @FXML
    private MFXTableColumn<User> coachingLicenseColumn;
    @FXML
    private MFXTableColumn<User> updatedAtColumn;
    @FXML
    private AnchorPane contentArea;
    @FXML
    private MFXTableColumn<User> firstNameColumn;
    @FXML
    private MFXTableColumn<User> profilePictureColumn;
    @FXML
    private MFXTableColumn<User> createdAtColumn;
    @FXML
    private MFXPaginatedTableView<User> usersTable;
    @FXML
    private MFXTableColumn<User> roleColumn;
    @FXML
    private MFXTableColumn<User> dateOfBirthColumn;
    @FXML
    private MFXTableColumn<User> lastNameColumn;
    @FXML
    private MFXTableColumn<User> phoneNumberColumn;
    @FXML
    private MFXTableColumn<User> emailColumn;
    @FXML
    private MFXTableColumn<User> actionsColumn;
    @FXML
    private MFXButton addButton;
    @FXML
    private Label contentLabel;

    private final UserService userService = new UserService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private boolean usersTableInitialized = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeUsersTable();
        String username = UserSession.getInstance().getCurrentUser() != null ?
                UserSession.getInstance().getCurrentUser().getFirstname() :
                "Admin";
        contentLabel.setText("Welcome, " + username);
        
        // Initially show the User Management view
        handleUserManagement();
    }

    private void initializeUsersTable() {
        if (usersTableInitialized) return;
        usersTableInitialized = true;

        // Set table size constraints
        usersTable.setPrefSize(1100, 600);
        usersTable.setMinSize(800, 400);
        usersTable.setMaxSize(1200, 800);

        // Clear existing columns before adding new ones
        usersTable.getTableColumns().clear();

        // Define column cell factories
        firstNameColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getFirstname));
        lastNameColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getLastName));
        emailColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getEmail));
        roleColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getRole));
        phoneNumberColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getPhoneNumber));
        dateOfBirthColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getDateOfBirth));
        profilePictureColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getProfilePicture));

        // Initialize Organizer-specific columns
        isActiveColumn.setRowCellFactory(user -> {
            MFXTableRowCell<User, String> cell = new MFXTableRowCell<>(u -> {
                if ("organizer".equalsIgnoreCase(u.getRole())) {
                    return u.isActive() ? "Yes" : "No";  // Direct mapping for organizers
                }
                return "Yes";  // Non-organizers are always active
            });

            // Add style based on status
            cell.textProperty().addListener((obs, old, newValue) -> {
                if ("Yes".equals(newValue)) {
                    cell.setStyle("-fx-text-fill: #4CAF50;"); // Green for active
                } else {
                    cell.setStyle("-fx-text-fill: #F44336;"); // Red for inactive
                }
            });

            return cell;
        });

        coachingLicenseColumn.setRowCellFactory(user -> new MFXTableRowCell<>(u -> {
            if (u instanceof Organizer) {
                return ((Organizer) u).getCoachingLicense();
            }
            return "N/A";
        }));

        // Initialize actions column
        actionsColumn.setRowCellFactory(user -> {
            MFXButton deleteButton = new MFXButton();
            deleteButton.setGraphic(new MFXFontIcon("fas-trash", 12));
            deleteButton.getStyleClass().addAll("mfx-button-danger", "icon-only");
            deleteButton.setMinWidth(20);
            deleteButton.setMinHeight(20);
            deleteButton.setOnAction(event -> handleDeleteUser((User) user));

            MFXButton updateButton = new MFXButton();
            updateButton.setGraphic(new MFXFontIcon("fas-arrow-rotate-right", 12));
            updateButton.getStyleClass().addAll("mfx-button-primary", "icon-only");
            updateButton.setMinWidth(20);
            updateButton.setOnAction(event -> handleEditUser((User) user));

            HBox hbox = new HBox(5, updateButton, deleteButton);
            hbox.setAlignment(Pos.CENTER);
            MFXTableRowCell<User, Void> cell = new MFXTableRowCell<>(null);
            cell.setGraphic(hbox);
            return cell;
        });

        // Add filters
        usersTable.getFilters().addAll(
                new StringFilter<>("First Name", User::getFirstname),
                new StringFilter<>("Last Name", User::getLastName),
                new StringFilter<>("Email", User::getEmail),
                new StringFilter<>("Role", User::getRole),
                new StringFilter<>("Phone Number", User::getPhoneNumber)
        );

        // Add sorting
        firstNameColumn.setComparator(Comparator.comparing(User::getFirstname));
        lastNameColumn.setComparator(Comparator.comparing(User::getLastName));
        emailColumn.setComparator(Comparator.comparing(User::getEmail));
        roleColumn.setComparator(Comparator.comparing(User::getRole));
        phoneNumberColumn.setComparator(Comparator.comparing(User::getPhoneNumber));
        dateOfBirthColumn.setComparator(Comparator.comparing(User::getDateOfBirth));

        // Set a fixed page size
        usersTable.setRowsPerPage(10);

        // Add a listener to autosize columns when the page changes
        When.onChanged(usersTable.currentPageProperty())
                .then((oldValue, newValue) -> usersTable.autosizeColumns())
                .listen();

        // Add columns to the table
        usersTable.getTableColumns().addAll(
                firstNameColumn,
                lastNameColumn,
                emailColumn,
                roleColumn,
                phoneNumberColumn,
                dateOfBirthColumn,
                isActiveColumn,
                coachingLicenseColumn,
                actionsColumn
        );

        // Load initial data
        loadUsers();
    }

    private void handleDeleteUser(User user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Are you sure you want to delete this user?");
        confirmAlert.setContentText("User: " + user.getFirstname() + " " + user.getLastName() + "\nEmail: " + user.getEmail());

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.delete(user.getId());

                    // Show success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("User successfully deleted");
                    successAlert.showAndWait();

                    // Refresh the table
                    loadUsers();
                } catch (SQLException e) {
                    String errorMessage = e.getMessage();
                    if (errorMessage.contains("does not exist")) {
                        // User already deleted or doesn't exist
                        Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                        errorAlert.setTitle("Warning");
                        errorAlert.setHeaderText("User Not Found");
                        errorAlert.setContentText("The user may have already been deleted. The table will be refreshed.");
                        errorAlert.showAndWait();
                        loadUsers(); // Refresh the table anyway
                    } else {
                        // Other SQL errors
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText("Failed to delete user");
                        errorAlert.setContentText("An error occurred: " + errorMessage);
                        errorAlert.showAndWait();
                    }
                }
            }
        });
    }

    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(Demo.class.getResource("fxml/addUser.fxml"));
            Parent root = loader.load();

            // Get the controller and set the user to update
            AddUserController controller = loader.getController();
            controller.setUserToUpdate(user);  // This sets the mode to UPDATE

            // Create and configure the stage
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Update User");

            // Create scene and show the stage
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Show the popup and wait for it to close
            stage.showAndWait();

            // Refresh the table after the popup closes
            loadUsers();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open update form: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleUserManagement() {
        // This is the current view, so just show the users table
        usersTable.setVisible(true);
        addButton.setVisible(true);
        contentLabel.setText("User Management");
        
        // Make sure the content area only has the appropriate elements
        contentArea.getChildren().clear();
        contentArea.getChildren().addAll(contentLabel, usersTable, addButton);
        
        // Set anchors for the elements
        AnchorPane.setTopAnchor(contentLabel, 20.0);
        AnchorPane.setLeftAnchor(contentLabel, 40.0);
        
        AnchorPane.setTopAnchor(usersTable, 80.0);
        AnchorPane.setLeftAnchor(usersTable, 40.0);
        AnchorPane.setRightAnchor(usersTable, 40.0);
        AnchorPane.setBottomAnchor(usersTable, 100.0);
        
        AnchorPane.setBottomAnchor(addButton, 40.0);
        AnchorPane.setLeftAnchor(addButton, 40.0);
        
        // Reload the users
        loadUsers();
    }

    @FXML
    private void handleProductManagement() {
        // Hide users table and add button
        usersTable.setVisible(false);
        addButton.setVisible(false);
        
        // Show product management view
        contentLabel.setText("Product Management");
        
        try {
            // Load the Product Management view
            // Use absolute path to ensure the file is found
            URL fxmlUrl = Demo.class.getResource("/io/github/palexdev/materialfx/demo/fxml/ProductManagement.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Cannot find ProductManagement.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent productView = loader.load();
            
            // Clear existing content and add product management view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(contentLabel);
            contentArea.getChildren().add(productView);
            
            // Position the product management view
            AnchorPane.setTopAnchor(contentLabel, 20.0);
            AnchorPane.setLeftAnchor(contentLabel, 40.0);
            
            AnchorPane.setTopAnchor(productView, 60.0);
            AnchorPane.setLeftAnchor(productView, 20.0);
            AnchorPane.setRightAnchor(productView, 20.0);
            AnchorPane.setBottomAnchor(productView, 20.0);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load product management view: " + e.getMessage());
        }
    }

    @FXML
    private void handleOrderManagement() {
        // Hide users table and add button
        usersTable.setVisible(false);
        addButton.setVisible(false);
        
        // Show order management view
        contentLabel.setText("Order Management");
        
        try {
            // Load the Order Management view
            URL fxmlUrl = Demo.class.getResource("/io/github/palexdev/materialfx/demo/fxml/OrderAdmin.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Cannot find OrderAdmin.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent orderView = loader.load();
            
            // Clear existing content and add order management view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(contentLabel);
            contentArea.getChildren().add(orderView);
            
            // Position the order management view
            AnchorPane.setTopAnchor(contentLabel, 20.0);
            AnchorPane.setLeftAnchor(contentLabel, 40.0);
            
            AnchorPane.setTopAnchor(orderView, 60.0);
            AnchorPane.setLeftAnchor(orderView, 20.0);
            AnchorPane.setRightAnchor(orderView, 20.0);
            AnchorPane.setBottomAnchor(orderView, 20.0);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load order management view: " + e.getMessage());
        }
    }

    @FXML
    private void handleDashboard() {
        try {
            // Load the admin dashboard
            FXMLLoader loader = new FXMLLoader(Demo.class.getResource("fxml/admin_dashboard.fxml"));
            Parent dashboardView = loader.load();
            
            // Clear existing content and add dashboard
            contentArea.getChildren().clear();
            contentArea.getChildren().add(contentLabel);
            contentArea.getChildren().add(dashboardView);
            
            // Position the elements
            AnchorPane.setTopAnchor(contentLabel, 20.0);
            AnchorPane.setLeftAnchor(contentLabel, 40.0);
            
            AnchorPane.setTopAnchor(dashboardView, 60.0);
            AnchorPane.setLeftAnchor(dashboardView, 20.0);
            AnchorPane.setRightAnchor(dashboardView, 20.0);
            AnchorPane.setBottomAnchor(dashboardView, 20.0);
            
            contentLabel.setText("Admin Dashboard");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load dashboard view: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        // Clear the current user session
        UserSession.getInstance().logout();
        
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(Demo.class.getResource("fxml/login.fxml"));
            Parent loginView = loader.load();
            
            // Get the current stage
            Stage currentStage = (Stage) contentArea.getScene().getWindow();
            
            // Create new scene with login view
            Scene loginScene = new Scene(loginView);
            
            // Set the new scene
            currentStage.setScene(loginScene);
            currentStage.setTitle("Login");
            currentStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load login view: " + e.getMessage());
        }
    }

    private void loadUsers() {
        List<User> users = userService.getAll();
        userList.setAll(users); // More idiomatic for ObservableList
        usersTable.setItems(userList); // Do not set to null first
    }

    @FXML
    public void handleAddUser(ActionEvent actionEvent) {
        openUserForm(null, "Add User", actionEvent);
    }

    private void openUserForm(User user, String title, ActionEvent actionEvent) {
        try {
            // Debug: Check if the FXML file exists
            URL fxmlUrl = Demo.class.getResource("fxml/addUser.fxml");
            if (fxmlUrl == null) {
                System.err.println("FXML file not found at: fxml/addUser.fxml");
                throw new IOException("FXML file not found at: fxml/addUser.fxml");
            } else {
                System.out.println("FXML file found at: " + fxmlUrl);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            VBox form = loader.load();

            AddUserController controller = loader.getController();
            if (user != null) {
                controller.setUserToUpdate(user);
            }

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);

            // Determine the owner stage
            Stage ownerStage = null;
            if (actionEvent != null) {
                // Use the source of the action event as the owner
                ownerStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            } else if (contentArea.getScene() != null && contentArea.getScene().getWindow() != null) {
                // Use the contentArea's scene window as the owner
                ownerStage = (Stage) contentArea.getScene().getWindow();
            } else {
                // Fallback to the primary stage
                ownerStage = (Stage) contentArea.getScene().getWindow();
            }

            popupStage.initOwner(ownerStage);
            popupStage.setTitle(title);
            popupStage.setScene(new Scene(form));

            // Show the popup and wait for it to close
            popupStage.showAndWait();

            // Refresh the table after the popup closes
            loadUsers();
        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Failed to open user form");
            errorAlert.setContentText("An error occurred: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
}