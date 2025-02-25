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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeUsersTable();
        String username = UserSession.getInstance().getCurrentUser() != null ?
                UserSession.getInstance().getCurrentUser().getFirstname() :
                "Admin";
        contentLabel.setText("Welcome, " + username);
    }

    private void initializeUsersTable() {
        // Define column cell factories
        firstNameColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getFirstname));
        lastNameColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getLastName));
        emailColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getEmail));
        roleColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getRole));
        phoneNumberColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getPhoneNumber));
        dateOfBirthColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getDateOfBirth));
        profilePictureColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getProfilePicture));
        createdAtColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getCreatedAt));
        updatedAtColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getUpdatedAt));

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
            updateButton.setOnAction(event -> handleUpdateUser((User) user));

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
        createdAtColumn.setComparator(Comparator.comparing(User::getCreatedAt));
        updatedAtColumn.setComparator(Comparator.comparing(User::getUpdatedAt));

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

    private void handleUpdateUser(User user) {
        openUserForm(user, "Update User", null);
    }

    private void loadUsers() {
        List<User> users = userService.getAll();
        userList.clear(); // Ensure the list is cleared before adding new items
        userList.addAll(users);
        usersTable.setItems(null); // Force refresh
        usersTable.setItems(userList);
    }

    @FXML
    public void handleAddUser(ActionEvent actionEvent) {
        openUserForm(null, "Add User", actionEvent);
    }

    private void openUserForm(User user, String title, ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(Demo.class.getResource("fxml/addUser.fxml"));
            VBox form = loader.load();

            AddUserController controller = loader.getController();
            if (user != null) {
                controller.setUserToUpdate(user);
            }

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(actionEvent != null ?
                    ((Node) actionEvent.getSource()).getScene().getWindow() :
                    contentArea.getScene().getWindow());
            popupStage.setTitle(title);
            popupStage.setScene(new Scene(form));

            popupStage.showAndWait();
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