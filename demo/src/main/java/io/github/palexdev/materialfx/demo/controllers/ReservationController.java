package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.demo.model.Event;
import io.github.palexdev.materialfx.demo.model.Reservation;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.ReservationService;
import io.github.palexdev.materialfx.demo.services.EventService;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationController implements Initializable {

    @FXML
    private VBox mainContainer;

    @FXML
    private VBox reservationsContainer;

    @FXML
    private MFXTextField searchField;

    @FXML
    private MFXComboBox<String> statusFilter;

    @FXML
    private MFXDatePicker dateFilter;

    @FXML
    private MFXButton clearFiltersButton;

    @FXML
    private MFXGenericDialog reservationDialog;

    @FXML
    private Label eventNameLabel;

    @FXML
    private Label eventDateLabel;

    @FXML
    private Label eventLocationLabel;

    @FXML
    private MFXComboBox<String> reservationStatus;

    @FXML
    private MFXTextField comment;

    @FXML
    private MFXButton backButton;

    @FXML
    private MFXTextField adminCommentField;

    private final ReservationService reservationService;
    private final EventService eventService;
    private final ObservableList<Reservation> reservations;
    private Event currentEvent;

    public ReservationController() {
        this.reservationService = new ReservationService();
        this.eventService = new EventService();
        this.reservations = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFilters();
        initializeDialog();
        loadReservations();

        // Add listeners for filters
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
        dateFilter.setOnAction(e -> applyFilters());
        
        clearFiltersButton.setOnAction(e -> clearFilters());
    }

    private void initializeFilters() {
        try {
            // Initialize status filter
            ObservableList<String> statuses = FXCollections.observableArrayList(
                "All",
                "pending",
                "confirmed",
                "cancelled"
            );
            statusFilter.setItems(statuses);
            statusFilter.selectFirst();

            // Initialize reservation status combo box
            if (reservationStatus != null) {
                ObservableList<String> reservationStatuses = FXCollections.observableArrayList(
                    "pending",
                    "confirmed",
                    "cancelled"
                );
                reservationStatus.setItems(reservationStatuses);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to initialize filters: " + e.getMessage());
        }
    }

    private void initializeDialog() {
        reservationDialog.setVisible(false);
        reservationDialog.setOnClose(event -> {
            reservationDialog.setVisible(false);
            clearDialogFields();
        });
    }

    private void loadReservations() {
        try {
            reservations.clear();
            List<Reservation> allReservations = reservationService.getAll();
            reservations.addAll(allReservations);
            displayReservations();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to load reservations: " + e.getMessage());
        }
    }

    private void displayReservations() {
        reservationsContainer.getChildren().clear();
        
        if (reservations.isEmpty()) {
            VBox noReservationsBox = new VBox();
            noReservationsBox.setAlignment(Pos.CENTER);
            noReservationsBox.setPrefHeight(200);
            
            Label noReservationsLabel = new Label("No reservations yet");
            noReservationsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #757575;");
            
            noReservationsBox.getChildren().add(noReservationsLabel);
            reservationsContainer.getChildren().add(noReservationsBox);
            return;
        }
        
        for (Reservation reservation : reservations) {
            reservationsContainer.getChildren().add(createReservationCard(reservation));
        }
    }

    private VBox createReservationCard(Reservation reservation) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        card.setMinHeight(150);
        card.getStyleClass().add("reservation-card");
        card.setPadding(new Insets(15));

        // Event Name
        Label eventName = new Label(reservation.getEvent().getNom());
        eventName.getStyleClass().add("card-title");

        // Player Info
        Label playerInfo = new Label("Player: " + reservation.getUser().getFirstname() + " " + reservation.getUser().getLastName());
        playerInfo.getStyleClass().add("card-player-info");

        // Status
        Label status = new Label("Status: " + reservation.getStatus());
        status.getStyleClass().addAll("status-label", "status-" + reservation.getStatus().toLowerCase());

        // Date
        Label date = new Label(reservation.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        date.getStyleClass().add("card-date");

        // Comment
        Label comment = new Label(reservation.getComment() != null ? reservation.getComment() : "No comment");
        comment.setWrapText(true);
        comment.getStyleClass().add("card-comment");

        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Only show approve/reject buttons for pending reservations
        if (reservation.getStatus().equals("pending")) {
            MFXButton approveButton = new MFXButton("Approve");
            approveButton.getStyleClass().add("approve-button");
            approveButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
            approveButton.setOnAction(e -> {
                reservationDialog.setUserData(reservation);
                handleAcceptReservation();
            });

            MFXButton rejectButton = new MFXButton("Reject");
            rejectButton.getStyleClass().add("reject-button");
            rejectButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            rejectButton.setOnAction(e -> {
                reservationDialog.setUserData(reservation);
                handleRejectReservation();
            });

            actions.getChildren().addAll(approveButton, rejectButton);
        }

        MFXButton editButton = new MFXButton("Edit");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(e -> showEditDialog(reservation));

        MFXButton deleteButton = new MFXButton("Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> handleDelete(reservation));

        actions.getChildren().addAll(editButton, deleteButton);

        // Add all components to the card
        card.getChildren().addAll(
            eventName,
            playerInfo,
            status,
            date,
            comment,
            actions
        );
        return card;
    }

    private void applyFilters() {
        try {
            String searchText = searchField.getText().toLowerCase();
            String selectedStatus = statusFilter.getValue();
            LocalDate selectedDate = dateFilter.getValue();

            List<Reservation> filteredReservations = reservationService.getAll().stream()
                .filter(reservation -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                        reservation.getEvent().getNom().toLowerCase().contains(searchText);
                    
                    boolean matchesStatus = "All".equals(selectedStatus) ||
                        reservation.getStatus().equals(selectedStatus);
                    
                    boolean matchesDate = selectedDate == null ||
                        reservation.getDate().toLocalDate().equals(selectedDate);
                    
                    return matchesSearch && matchesStatus && matchesDate;
                })
                .toList();

            reservations.clear();
            reservations.addAll(filteredReservations);
            displayReservations();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to apply filters: " + e.getMessage());
        }
    }

    private void clearFilters() {
        searchField.clear();
        statusFilter.selectFirst();
        dateFilter.clear();
        loadReservations();
    }

    private void showEditDialog(Reservation reservation) {
        currentEvent = reservation.getEvent();
        eventNameLabel.setText(currentEvent.getNom());
        eventDateLabel.setText("Date: " + currentEvent.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        eventLocationLabel.setText("Location: " + currentEvent.getAddress());
        
        reservationStatus.setValue(reservation.getStatus());
        comment.setText(reservation.getComment());
        
        reservationDialog.setUserData(reservation);
        reservationDialog.setVisible(true);
    }

    private void clearDialogFields() {
        currentEvent = null;
        eventNameLabel.setText("");
        eventDateLabel.setText("");
        eventLocationLabel.setText("");
        reservationStatus.setValue(null);
        comment.clear();
        reservationDialog.setUserData(null);
        adminCommentField.clear();
    }

    @FXML
    private void handleRefresh() {
        loadReservations();
    }

    @FXML
    private void handleCancelReservation() {
        reservationDialog.setVisible(false);
        clearDialogFields();
    }

    @FXML
    private void handleSaveReservation() {
        Reservation reservation = (Reservation) reservationDialog.getUserData();
        if (reservation == null) {
            showAlert(AlertType.ERROR, "Error", "No reservation selected");
            return;
        }

        try {
            reservation.setStatus(reservationStatus.getValue());
            reservation.setComment(comment.getText());
            reservationService.update(reservation);
            
            loadReservations();
            reservationDialog.setVisible(false);
            clearDialogFields();
            showAlert(AlertType.INFORMATION, "Success", "Reservation updated successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to update reservation: " + e.getMessage());
        }
    }

    private void handleDelete(Reservation reservation) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Reservation");
        alert.setHeaderText("Are you sure you want to delete this reservation?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(result -> {
            if (result.getButtonData().isDefaultButton()) {
                try {
                    reservationService.delete(reservation.getId());
                    loadReservations();
                    showAlert(AlertType.INFORMATION, "Success", "Reservation deleted successfully");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(AlertType.ERROR, "Error", "Failed to delete reservation: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleAcceptReservation() {
        Reservation reservation = (Reservation) reservationDialog.getUserData();
        if (reservation == null) {
            showAlert(AlertType.ERROR, "Error", "No reservation selected");
            return;
        }

        try {
            // Update reservation status to confirmed
            reservation.setStatus("confirmed");
            // Add admin comment if provided
            if (!adminCommentField.getText().isEmpty()) {
                reservation.setComment(adminCommentField.getText());
            }
            
            // Get the full event details before updating
            Event event = eventService.getById(reservation.getEvent().getId());
            if (event != null) {
                // Check if the event is not full
                if (event.getMaxParticipants() == null || event.getCurrentParticipants() < event.getMaxParticipants()) {
                    // Increment the participant count
                    event.incrementCurrentParticipants();
                    // Update the event first
                    eventService.update(event);
                    // Then update the reservation
                    reservationService.update(reservation);
                    
                    loadReservations();
                    reservationDialog.setVisible(false);
                    clearDialogFields();
                    showAlert(AlertType.INFORMATION, "Success", "Reservation accepted successfully");
                } else {
                    showAlert(AlertType.ERROR, "Error", "Cannot accept reservation: Event is full");
                }
            } else {
                showAlert(AlertType.ERROR, "Error", "Event not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to accept reservation: " + e.getMessage());
        }
    }

    @FXML
    private void handleRejectReservation() {
        Reservation reservation = (Reservation) reservationDialog.getUserData();
        if (reservation == null) {
            showAlert(AlertType.ERROR, "Error", "No reservation selected");
            return;
        }

        try {
            // Update reservation status to cancelled
            reservation.setStatus("cancelled");
            // Add admin comment if provided
            if (!adminCommentField.getText().isEmpty()) {
                reservation.setComment(adminCommentField.getText());
            }
            
            reservationService.update(reservation);
            
            // Update event participants count since the reservation is rejected
            Event event = reservation.getEvent();
            event.decrementCurrentParticipants();
            eventService.update(event);
            
            loadReservations();
            reservationDialog.setVisible(false);
            clearDialogFields();
            showAlert(AlertType.INFORMATION, "Success", "Reservation rejected successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to reject reservation: " + e.getMessage());
        }
    }

    @FXML
    private void handleCloseDialog() {
        reservationDialog.setVisible(false);
        clearDialogFields();
    }

    private void showAlert(AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public void setCurrentEvent(Event event) {
        this.currentEvent = event;
        if (event != null) {
            // Update the title to show we're viewing reservations for a specific event
            Platform.runLater(() -> {
                Text headerTitle = (Text) mainContainer.lookup(".header-title");
                if (headerTitle != null) {
                    headerTitle.setText("Reservations for " + event.getNom());
                }
                
                // Load only reservations for this event
                try {
                    reservations.clear();
                    List<Reservation> eventReservations = reservationService.getByEvent(event.getId());
                    reservations.addAll(eventReservations);
                    displayReservations();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(AlertType.ERROR, "Error", "Failed to load reservations: " + e.getMessage());
                }
            });
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/palexdev/materialfx/demo/fxml/Events.fxml"));
            Parent eventsView = loader.load();
            
            // Get the current scene
            Scene currentScene = mainContainer.getScene();
            
            // Get the content pane from the root AnchorPane
            AnchorPane root = (AnchorPane) currentScene.getRoot();
            StackPane contentPane = (StackPane) root.lookup("#contentPane");
            
            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(eventsView);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to load events view: " + e.getMessage());
        }
    }
} 