package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.demo.model.Event;
import io.github.palexdev.materialfx.demo.model.Reservation;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.EventService;
import io.github.palexdev.materialfx.demo.services.ReservationService;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class FrontEventController implements Initializable {

    @FXML private FlowPane eventsContainer;
    @FXML private MFXTextField searchField;
    @FXML private MFXComboBox<String> statusFilter;
    @FXML private MFXDatePicker dateFilter;
    @FXML private MFXButton clearFiltersButton;
    @FXML private MFXButton refreshButton;
    @FXML private MFXGenericDialog participationDialog;
    @FXML private Label eventNameLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventLocationLabel;
    @FXML private Label eventParticipantsLabel;
    @FXML private MFXTextField commentField;

    private final EventService eventService;
    private final ReservationService reservationService;
    private final ObservableList<Event> events;
    private Event currentEvent;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public FrontEventController() {
        this.eventService = new EventService();
        this.reservationService = new ReservationService();
        this.events = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFilters();
        initializeDialog();
        loadEvents();

        // Add listeners for filters
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
        dateFilter.setOnAction(e -> applyFilters());
        
        clearFiltersButton.setOnAction(e -> clearFilters());
    }

    private void initializeFilters() {
        // Initialize status filter - removed since we only show active events
        searchField.setPromptText("Search active events...");
        searchField.setFloatingText("Search active events...");
    }

    private void initializeDialog() {
        participationDialog.setVisible(false);
        participationDialog.setManaged(false);
        
        // Set dialog size constraints
        participationDialog.setPrefWidth(500);
        participationDialog.setMaxWidth(500);
        
        // Center the dialog
        StackPane parent = (StackPane) participationDialog.getParent();
        if (parent != null) {
            participationDialog.translateXProperty().bind(
                parent.widthProperty().subtract(participationDialog.widthProperty()).divide(2)
            );
            participationDialog.translateYProperty().bind(
                parent.heightProperty().subtract(participationDialog.heightProperty()).divide(2)
            );
        }
        
        participationDialog.setOnClose(event -> {
            participationDialog.setVisible(false);
            clearDialogFields();
        });
    }

    private void loadEvents() {
        try {
            events.clear();
            List<Event> allEvents = eventService.getAll();
            // Filter to only include active events
            List<Event> activeEvents = allEvents.stream()
                .toList();
            
            // Debug logging
            System.out.println("Total events: " + allEvents.size());
            System.out.println("Active events: " + activeEvents.size());
            allEvents.forEach(event -> System.out.println("Event: " + event.getNom() + ", Status: " + event.getStatus() + ", End Time: " + event.getEndTime()));
            
            events.addAll(activeEvents);
            displayEvents(events);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to load events: " + e.getMessage());
        }
    }

    private boolean isEventActive(Event event) {
        // Check if event is active and not in the past, case-insensitive check
        if (event.getStatus() == null || !event.getStatus().equalsIgnoreCase("active")) {
            return false;
        }

        if (event.getEndTime() == null) {
            return false;
        }

        // Compare the full LocalDateTime, not just the date
        LocalDateTime now = LocalDateTime.now();
        
        System.out.println("Checking event: " + event.getNom());
        System.out.println("Current time: " + now);
        System.out.println("Event end time: " + event.getEndTime());
        
        // Event is active if it hasn't ended yet (end time is in the future)
        return now.isBefore(event.getEndTime()) || now.equals(event.getEndTime());
    }

    private void displayEvents(List<Event> eventsList) {
        eventsContainer.getChildren().clear();
        
        if (eventsList.isEmpty()) {
            VBox noEventsBox = new VBox();
            noEventsBox.setAlignment(Pos.CENTER);
            noEventsBox.setPrefHeight(200);
            
            Label noEventsLabel = new Label("No active events available");
            noEventsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #757575;");
            
            noEventsBox.getChildren().add(noEventsLabel);
            eventsContainer.getChildren().add(noEventsBox);
            return;
        }
        
        for (Event event : eventsList) {
            eventsContainer.getChildren().add(createEventCard(event));
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(15);
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        card.setMinHeight(200);
        card.getStyleClass().add("event-card");
        card.setPadding(new Insets(20));

        // Event Name
        Label nameLabel = new Label(event.getNom());
        nameLabel.getStyleClass().add("event-name");

        // Status
        Label statusLabel = new Label(event.getStatus());
        statusLabel.getStyleClass().addAll("status-badge", getStatusStyleClass(event.getStatus()));

        // Date
        Label dateLabel = new Label(event.getStartTime().format(dateTimeFormatter));
        dateLabel.getStyleClass().add("event-date");

        // Description
        Label descriptionLabel = new Label(event.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("description-text");

        // Location
        Label locationLabel = new Label("Location: " + event.getAddress());
        locationLabel.getStyleClass().add("address-text");

        // Time
        Label timeLabel = new Label(String.format("Time: %s - %s",
            event.getStartTime().format(timeFormatter),
            event.getEndTime().format(timeFormatter)));
        timeLabel.getStyleClass().add("time-text");

        // Participants
        Label participantsLabel = new Label(String.format("Participants: %d/%s",
            event.getCurrentParticipants(),
            event.getMaxParticipants() != null ? event.getMaxParticipants().toString() : "∞"));
        participantsLabel.getStyleClass().add("participants-text");

        // Participate Button
        MFXButton participateButton = new MFXButton("Participate");
        participateButton.getStyleClass().add("participate-button");
        participateButton.setMaxWidth(Double.MAX_VALUE);
        
        // Check if user is already participating
        boolean isParticipating = checkIfUserIsParticipating(event);
        if (isParticipating) {
            participateButton.setText("Already Participating");
            participateButton.setDisable(true);
        } else if (event.isEventFull()) {
            participateButton.setText("Event Full");
            participateButton.setDisable(true);
        } else if ("Completed".equalsIgnoreCase(event.getStatus()) || 
                   "Cancelled".equalsIgnoreCase(event.getStatus())) {
            participateButton.setText("Event " + event.getStatus());
            participateButton.setDisable(true);
        }

        participateButton.setOnAction(e -> showParticipationDialog(event));

        // Add all components to card
        VBox.setMargin(participateButton, new Insets(10, 0, 0, 0));
        card.getChildren().addAll(
            nameLabel, 
            statusLabel, 
            dateLabel, 
            descriptionLabel, 
            locationLabel, 
            timeLabel, 
            participantsLabel,
            participateButton
        );

        return card;
    }

    private boolean checkIfUserIsParticipating(Event event) {
        try {
            User currentUser = UserSession.getInstance().getCurrentUser();
            List<Reservation> userReservations = reservationService.getByUser(currentUser.getId());
            return userReservations.stream()
                .anyMatch(r -> r.getEvent().getId().equals(event.getId()) && 
                             !r.getStatus().equalsIgnoreCase("cancelled"));
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getStatusStyleClass(String status) {
        if (status == null) return "status-default";
        
        return switch (status.toLowerCase()) {
            case "pending" -> "status-pending";
            case "active" -> "status-active";  // Changed from "Active" to "active"
            case "completed" -> "status-completed";
            case "cancelled" -> "status-cancelled";
            default -> "status-default";
        };
    }

    private void showParticipationDialog(Event event) {
        currentEvent = event;
        eventNameLabel.setText(event.getNom());
        eventDateLabel.setText("Date: " + event.getStartTime().format(dateTimeFormatter));
        eventLocationLabel.setText("Location: " + event.getAddress());
        eventParticipantsLabel.setText(String.format("Participants: %d/%s",
            event.getCurrentParticipants(),
            event.getMaxParticipants() != null ? event.getMaxParticipants().toString() : "∞"));
        
        commentField.clear();
        
        // Show dialog centered
        Platform.runLater(() -> {
            // Ensure dialog is properly centered
            StackPane parent = (StackPane) participationDialog.getParent();
            if (parent != null && !participationDialog.translateXProperty().isBound()) {
                participationDialog.translateXProperty().bind(
                    parent.widthProperty().subtract(participationDialog.widthProperty()).divide(2)
                );
                participationDialog.translateYProperty().bind(
                    parent.heightProperty().subtract(participationDialog.heightProperty()).divide(2)
                );
            }
            
            participationDialog.setVisible(true);
            participationDialog.toFront();
        });
    }

    private void clearDialogFields() {
        currentEvent = null;
        eventNameLabel.setText("");
        eventDateLabel.setText("");
        eventLocationLabel.setText("");
        eventParticipantsLabel.setText("");
        commentField.clear();
    }

    @FXML
    private void handleRefresh() {
        loadEvents();
    }

    @FXML
    private void handleCancelParticipation() {
        participationDialog.setVisible(false);
        clearDialogFields();
    }

    @FXML
    private void handleParticipate() {
        if (currentEvent == null) {
            showAlert(AlertType.ERROR, "Error", "No event selected");
            return;
        }

        try {
            Reservation reservation = new Reservation();
            reservation.setEvent(currentEvent);
            reservation.setUser(UserSession.getInstance().getCurrentUser());
            reservation.setDate(LocalDateTime.now());
            reservation.setStatus("pending");
            reservation.setComment(commentField.getText());

            reservationService.create(reservation);
            
            participationDialog.setVisible(false);
            clearDialogFields();
            loadEvents(); // Refresh to update participation status
            showAlert(AlertType.INFORMATION, "Success", "Successfully registered for the event!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to register for event: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (events == null) return;

        List<Event> filteredEvents = events.stream()
            .filter(event -> {
                // Search filter
                boolean matchesSearch = searchField.getText().isEmpty() ||
                    event.getNom().toLowerCase().contains(searchField.getText().toLowerCase()) ||
                    event.getDescription().toLowerCase().contains(searchField.getText().toLowerCase());

                // Date filter
                boolean matchesDate = dateFilter.getValue() == null ||
                    event.getStartTime().toLocalDate().equals(dateFilter.getValue());

                // Only show active events
                boolean isActive = isEventActive(event);

                return matchesSearch && matchesDate && isActive;
            })
            .toList();

        displayEvents(filteredEvents);
    }

    private void clearFilters() {
        searchField.clear();
        statusFilter.selectFirst();
        dateFilter.clear();
        loadEvents();
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
} 