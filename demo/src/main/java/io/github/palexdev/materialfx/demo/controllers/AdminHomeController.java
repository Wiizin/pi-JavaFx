package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.StringFilter;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class AdminHomeController implements Initializable {

    @FXML
    private MFXTableColumn updatedAtColumn;
    @FXML
    private AnchorPane contentArea;
    @FXML
    private MFXTableColumn firstNameColumn;
    @FXML
    private MFXTableColumn profilePictureColumn;
    @FXML
    private MFXTableColumn createdAtColumn;
    @FXML
    private MFXPaginatedTableView<User> usersTable; // Changed to MFXPaginatedTableView
    @FXML
    private MFXTableColumn roleColumn;
    @FXML
    private MFXTableColumn dateOfBirthColumn;
    @FXML
    private MFXTableColumn lastNameColumn;
    @FXML
    private MFXTableColumn phoneNumberColumn;
    @FXML
    private MFXTableColumn emailColumn;
    @FXML
    private MFXTableColumn actionsColumn;
    @FXML
    private MFXButton addButton;

    private final UserService userService = new UserService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    private void initializeUsersTable() {
        // Define columns
        firstNameColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getFirstname));
        lastNameColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getLastName));
        emailColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getEmail));
        roleColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getRole));
        phoneNumberColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getPhoneNumber));
        dateOfBirthColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getDateOfBirth));
        profilePictureColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getProfilePicture));
        createdAtColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getCreatedAt));
        updatedAtColumn.setRowCellFactory(user -> new MFXTableRowCell<>(User::getUpdatedAt));

        // Add filters
        usersTable.getFilters().addAll(
                new StringFilter<>("First Name", User::getFirstname),
                new StringFilter<>("Last Name", User::getLastName),
                new StringFilter<>("Email", User::getEmail),
                new StringFilter<>("Role", User::getRole),
                new StringFilter<>("Phone Number", User::getPhoneNumber),
                new StringFilter<>("Profile Picture", User::getProfilePicture)
        );

        // Add sorting
        firstNameColumn.setComparator(Comparator.comparing(User::getFirstname));
        lastNameColumn.setComparator(Comparator.comparing(User::getLastName));
        emailColumn.setComparator(Comparator.comparing(User::getEmail));
        roleColumn.setComparator(Comparator.comparing(User::getRole));
        phoneNumberColumn.setComparator(Comparator.comparing(User::getPhoneNumber));
        dateOfBirthColumn.setComparator(Comparator.comparing(User::getDateOfBirth));
        profilePictureColumn.setComparator(Comparator.comparing(User::getProfilePicture));
        createdAtColumn.setComparator(Comparator.comparing(User::getCreatedAt));
        updatedAtColumn.setComparator(Comparator.comparing(User::getUpdatedAt));

        // Add actions column
        actionsColumn.setRowCellFactory(user -> {
            MFXButton deleteButton = new MFXButton("Delete");
            deleteButton.setMinWidth((20));
            deleteButton.getStyleClass().add("mfx-button-danger");
            deleteButton.setOnAction(event -> handleDeleteUser((User) user));

            MFXButton updateButton = new MFXButton("Update");
            updateButton.setMinWidth(20);
            updateButton.getStyleClass().add("mfx-button-primary");
            updateButton.setOnAction(event -> handleUpdateUser((User) user));

            // Create a custom cell and set the HBox as its graphic
            HBox hbox = new HBox(5, updateButton, deleteButton);
            MFXTableRowCell<User, Void> cell = new MFXTableRowCell<>(null); // Pass null for the value extractor
            cell.setGraphic(hbox); // Set the HBox as the graphic for the cell
            return cell;
        });

        // Load users from the database
        loadUsers();
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete this user?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("Deleting user: " + user.getFirstname()); // Debug
                userService.delete(user.getId()); // Ensure this actually removes the user

                loadUsers();  // Reload users from database
                 // Ensure UI is updated
            }
        });
    }


    private void handleUpdateUser(User user) {

        openUserForm(user, "Update User", null);
        }


    private void loadUsers() {
        List<User> users = userService.getAll();
        userList.setAll(users);
        usersTable.setItems(userList);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeUsersTable();
    }

    public void handleAddUser(ActionEvent actionEvent) {
        openUserForm(null, "Add User", actionEvent);
    }

    private void openUserForm(User user, String title, ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(Demo.class.getResource("fxml/addUser.fxml"));
            VBox form = loader.load();

            // Pass the user to the form controller
            AddUserController controller = loader.getController();
            if (user != null) {
                controller.setUserToUpdate(user);
            }

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(actionEvent != null ? ((Node) actionEvent.getSource()).getScene().getWindow() : contentArea.getScene().getWindow());
            popupStage.setTitle(title);
            popupStage.setScene(new Scene(form));

            popupStage.showAndWait();

            // Refresh the table after closing the form
            loadUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}