package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.demo.model.Event;
import io.github.palexdev.materialfx.demo.model.Reservation;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.EventService;
import io.github.palexdev.materialfx.demo.services.ReservationService;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class EventsController implements Initializable {

    @FXML private VBox mainContainer;
    @FXML private MFXButton addEventButton;
    @FXML private MFXButton refreshButton;
    @FXML private MFXTextField searchField;
    @FXML private MFXComboBox<String> statusFilter;
    @FXML private MFXDatePicker dateFilter;
    @FXML private MFXButton clearFiltersButton;
    @FXML private VBox eventsContainer;
    @FXML private MFXGenericDialog eventDialog;

    @FXML private MFXTextField eventName;
    @FXML private MFXTextField eventDescription;
    @FXML private MFXTextField eventAddress;
    @FXML private MFXTextField eventLatitude;
    @FXML private MFXTextField eventLongitude;
    @FXML private MFXDatePicker startDate;
    @FXML private MFXTextField startTime;
    @FXML private MFXDatePicker endDate;
    @FXML private MFXTextField endTime;
    @FXML private MFXDatePicker breakDate;
    @FXML private MFXTextField breakTime;
    @FXML private MFXTextField maxParticipants;
    @FXML private MFXComboBox<String> eventStatus;
    @FXML private ImageView eventImagePreview;
    @FXML private MFXButton uploadImageButton;

    private final EventService eventService;
    private final ObservableList<Event> events;
    private Event currentEvent;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private File tempImageFile;

    private final List<String> EVENT_STATUSES = List.of(
        "Pending",
        "Active",
        "Completed",
        "Cancelled"
    );

    public EventsController() {
        this.eventService = new EventService();
        this.events = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            initializeDialog();
            initializeFilters();
            setupEventHandlers();
            loadEvents();
            
            // Initialize image upload button
            uploadImageButton.setOnAction(e -> handleImageUpload());
            setDefaultEventImage(eventImagePreview);
        });
    }

    private void initializeDialog() {
        eventDialog.setVisible(false);

        // Configure dialog size and position
        eventDialog.setPrefWidth(600);
        eventDialog.setPrefHeight(Region.USE_COMPUTED_SIZE);
        eventDialog.setTranslateY(-50); // Move dialog up

        // Set background style
        eventDialog.getStyleClass().add("event-dialog");

        // Initialize status options
        eventStatus.setItems(FXCollections.observableArrayList(EVENT_STATUSES));

        // Handle close event
        eventDialog.setOnClose(event -> {
            clearDialogFields();
            eventDialog.setVisible(false);
        });
    }

    private void initializeFilters() {
        // Initialize status filter
        ObservableList<String> statuses = FXCollections.observableArrayList();
        statuses.add("All");
        statuses.addAll(EVENT_STATUSES);
        statusFilter.setItems(statuses);
        statusFilter.selectFirst();

        // Add listeners for filters
        statusFilter.selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Clear filters button
        clearFiltersButton.setOnAction(e -> {
            searchField.clear();
            statusFilter.selectFirst();
            dateFilter.clear();
        });
    }

    private void setupEventHandlers() {
        addEventButton.setOnAction(e -> showEventDialog(null));
        refreshButton.setOnAction(e -> loadEvents());
    }

    private void loadEvents() {
        try {
            events.clear();
            events.addAll(eventService.getAll());
            Platform.runLater(() -> displayEvents(events));
        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert("Error", "Failed to load events: " + e.getMessage()));
        }
    }

    private void displayEvents(List<Event> eventsList) {
        eventsContainer.getChildren().clear();
        for (Event event : eventsList) {
            eventsContainer.getChildren().add(createEventCard(event));
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(15);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(200);
        card.getStyleClass().add("event-card");
        card.setPadding(new Insets(20));

        // Header with event info and status
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("card-header");
        header.setPadding(new Insets(0, 0, 10, 0));

        // Event image
        ImageView eventImage = new ImageView();
        eventImage.setFitWidth(80);
        eventImage.setFitHeight(80);
        eventImage.getStyleClass().add("event-image");
        
        // Make the image view circular
        Circle clip = new Circle(40, 40, 40);
        eventImage.setClip(clip);
        
        // Load the event image
        if (event.getImage() != null && !event.getImage().isEmpty()) {
            try {
                File imageFile = new File("C:/xampp/htdocs/events/" + event.getImage());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    eventImage.setImage(image);
                } else {
                    setDefaultEventImage(eventImage);
                }
            } catch (Exception e) {
                e.printStackTrace();
                setDefaultEventImage(eventImage);
            }
        } else {
            setDefaultEventImage(eventImage);
        }

        // Event basic info (name and date)
        VBox eventInfo = new VBox(5);
        Label nameLabel = new Label(event.getNom());
        nameLabel.getStyleClass().add("event-name");

        Label dateLabel = new Label(event.getStartTime().format(dateTimeFormatter));
        dateLabel.getStyleClass().add("event-date");

        eventInfo.getChildren().addAll(nameLabel, dateLabel);

        // Status badge
        Label statusLabel = new Label(event.getStatus());
        statusLabel.getStyleClass().addAll("status-badge", getStatusStyleClass(event.getStatus()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(eventImage, eventInfo, spacer, statusLabel);

        // Event details
        VBox detailsBox = new VBox(10);
        detailsBox.getStyleClass().add("details-box");
        detailsBox.setPadding(new Insets(15));

        Label descriptionLabel = new Label(event.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("description-text");

        Label addressLabel = new Label("Location: " + event.getAddress());
        addressLabel.getStyleClass().add("address-text");

        Label timeLabel = new Label(String.format("Time: %s - %s",
            event.getStartTime().format(timeFormatter),
            event.getEndTime().format(timeFormatter)));
        timeLabel.getStyleClass().add("time-text");

        Label participantsLabel = new Label(String.format("Participants: %d/%s",
            event.getCurrentParticipants(),
            event.getMaxParticipants() != null ? event.getMaxParticipants().toString() : "∞"));
        participantsLabel.getStyleClass().add("participants-text");

        detailsBox.getChildren().addAll(descriptionLabel, addressLabel, timeLabel, participantsLabel);

        // Actions section with buttons
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setPadding(new Insets(15, 0, 0, 0));
        actionsBox.getStyleClass().add("actions-box");

        // Left side buttons (View Reservations)
        HBox leftButtons = new HBox(10);
        leftButtons.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftButtons, Priority.ALWAYS);

        MFXButton viewReservationsButton = new MFXButton("View Reservations");
        viewReservationsButton.getStyleClass().add("view-reservations-button");
        viewReservationsButton.setOnAction(e -> navigateToReservations(event));
        leftButtons.getChildren().add(viewReservationsButton);

        // Right side buttons (Edit, Delete)
        HBox rightButtons = new HBox(10);
        rightButtons.setAlignment(Pos.CENTER_RIGHT);

        MFXButton editButton = new MFXButton("Edit");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(e -> showEventDialog(event));

        MFXButton deleteButton = new MFXButton("Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> handleDelete(event));

        rightButtons.getChildren().addAll(editButton, deleteButton);

        // Add both button groups to the actions box
        actionsBox.getChildren().addAll(leftButtons, rightButtons);

        // Add all sections to the card
        card.getChildren().addAll(header, detailsBox, actionsBox);
        return card;
    }

    private String getStatusStyleClass(String status) {
        return switch (status.toLowerCase()) {
            case "pending" -> "status-pending";
            case "active" -> "status-active";
            case "completed" -> "status-completed";
            case "cancelled" -> "status-cancelled";
            default -> "status-default";
        };
    }

    private void setDefaultEventImage(ImageView imageView) {
        try {
            String defaultImageUrl = Objects.requireNonNull(getClass().getResource("/default_event.jpg")).toExternalForm();
            Image defaultImage = new Image(defaultImageUrl);
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            e.printStackTrace();
            // If default image fails to load, just leave it empty
        }
    }

    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Event Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(mainContainer.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Store the selected file temporarily
                tempImageFile = selectedFile;

                // Display preview
                Image image = new Image(selectedFile.toURI().toString());
                eventImagePreview.setImage(image);

                showAlert("Success", "Image selected. Click 'Save' to upload.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load image: " + e.getMessage());
            }
        }
    }

    private void showEventDialog(Event event) {
        currentEvent = event;
        eventDialog.setHeaderText(event == null ? "Add New Event" : "Edit Event");
        tempImageFile = null;

        if (event != null) {
            // Load event image
            if (event.getImage() != null && !event.getImage().isEmpty()) {
                try {
                    File imageFile = new File("C:/xampp/htdocs/events/" + event.getImage());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        eventImagePreview.setImage(image);
                    } else {
                        setDefaultEventImage(eventImagePreview);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setDefaultEventImage(eventImagePreview);
                }
            } else {
                setDefaultEventImage(eventImagePreview);
            }

            // Populate fields with event data
            eventName.setText(event.getNom());
            eventDescription.setText(event.getDescription());
            eventAddress.setText(event.getAddress());
            eventLatitude.setText(event.getLatitude() != null ? event.getLatitude().toString() : "");
            eventLongitude.setText(event.getLongitude() != null ? event.getLongitude().toString() : "");

            if (event.getStartTime() != null) {
                startDate.setValue(event.getStartTime().toLocalDate());
                startTime.setText(event.getStartTime().format(timeFormatter));
            }

            if (event.getEndTime() != null) {
                endDate.setValue(event.getEndTime().toLocalDate());
                endTime.setText(event.getEndTime().format(timeFormatter));
            }

            if (event.getBreakTime() != null) {
                breakDate.setValue(event.getBreakTime().toLocalDate());
                breakTime.setText(event.getBreakTime().format(timeFormatter));
            }

            maxParticipants.setText(event.getMaxParticipants() != null ?
                event.getMaxParticipants().toString() : "");

            eventStatus.setValue(event.getStatus());
        } else {
            clearDialogFields();
            eventStatus.setValue("Pending"); // Default status for new events
            setDefaultEventImage(eventImagePreview);
        }

        Platform.runLater(() -> {
            eventDialog.setVisible(true);
            eventName.requestFocus();
        });
    }

    private void clearDialogFields() {
        eventName.clear();
        eventDescription.clear();
        eventAddress.clear();
        eventLatitude.clear();
        eventLongitude.clear();
        startDate.clear();
        startTime.clear();
        endDate.clear();
        endTime.clear();
        breakDate.clear();
        breakTime.clear();
        maxParticipants.clear();
        eventStatus.setValue(null);
        setDefaultEventImage(eventImagePreview);
        tempImageFile = null;
    }

    @FXML
    private void handleSaveEvent() {
        if (!validateEventFields()) {
            return;
        }

        try {
            Event event = currentEvent != null ? currentEvent : new Event();

            // Handle image upload if a new image was selected
            if (tempImageFile != null) {
                try {
                    // Generate a unique filename
                    String extension = tempImageFile.getName().substring(tempImageFile.getName().lastIndexOf("."));
                    String uniqueFileName = System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + extension;

                    // Create events directory in XAMPP htdocs if it doesn't exist
                    File uploadsDir = new File("C:/xampp/htdocs/events");
                    if (!uploadsDir.exists()) {
                        uploadsDir.mkdirs();
                    }

                    // Create the destination file
                    File destinationFile = new File(uploadsDir, uniqueFileName);

                    // Copy the file
                    Files.copy(tempImageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Set the image filename in the event
                    event.setImage(uniqueFileName);

                    // Clear the temporary file
                    tempImageFile = null;
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to save image: " + e.getMessage());
                    return;
                }
            }

            // Set other event fields
            event.setNom(eventName.getText());
            event.setDescription(eventDescription.getText());
            event.setAddress(eventAddress.getText());

            try {
                event.setLatitude(Double.parseDouble(eventLatitude.getText()));
                event.setLongitude(Double.parseDouble(eventLongitude.getText()));
            } catch (NumberFormatException e) {
                // Ignore parsing errors, coordinates will remain null
            }

            LocalTime startTimeValue = parseTime(startTime.getText());
            LocalTime endTimeValue = parseTime(endTime.getText());
            LocalTime breakTimeValue = breakTime.getText().isEmpty() ? null : parseTime(breakTime.getText());

            if (startTimeValue == null || endTimeValue == null) {
                showAlert("Error", "Invalid time format. Please use HH:mm format (e.g., 14:30)");
                return;
            }

            // Create LocalDateTime objects
            LocalDateTime startDateTime = LocalDateTime.of(startDate.getValue(), startTimeValue);
            LocalDateTime endDateTime = LocalDateTime.of(endDate.getValue(), endTimeValue);

            event.setStartTime(startDateTime);
            event.setEndTime(endDateTime);

            if (breakDate.getValue() != null && breakTimeValue != null) {
                LocalDateTime breakDateTime = LocalDateTime.of(breakDate.getValue(), breakTimeValue);
                event.setBreakTime(breakDateTime);
            }

            try {
                event.setMaxParticipants(Integer.parseInt(maxParticipants.getText()));
            } catch (NumberFormatException e) {
                // Ignore parsing errors, max participants will remain null
            }

            event.setStatus(eventStatus.getValue());

            if (currentEvent == null) {
                event.setOrganizer(UserSession.getInstance().getCurrentUser());
                eventService.create(event);
            } else {
                eventService.update(event);
            }

            eventDialog.setVisible(false);
            loadEvents();
            showAlert("Success", "Event saved successfully");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save event: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelEvent() {
        currentEvent = null;
        eventDialog.setVisible(false);
        clearDialogFields();
    }

    private void handleDelete(Event event) {
        System.out.println("Delete handler called for event: " + event.getId());

        MFXGenericDialog confirmDialog = new MFXGenericDialog();
        confirmDialog.setHeaderText("Delete Event");
        confirmDialog.setContentText("Are you sure you want to delete this event?\nThis action cannot be undone.");
        confirmDialog.setPrefWidth(400);
        confirmDialog.setPrefHeight(Region.USE_COMPUTED_SIZE);
        confirmDialog.setTranslateY(100);
        confirmDialog.setShowClose(true);
        confirmDialog.setShowMinimize(false);
        confirmDialog.setShowAlwaysOnTop(false);
        confirmDialog.setAlwaysOnTop(true);

        // Set background style
        confirmDialog.getStyleClass().add("mfx-generic-dialog");

        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setPadding(new Insets(20, 0, 0, 0));

        MFXButton cancelBtn = new MFXButton("Cancel");
        cancelBtn.getStyleClass().add("cancel-button");
        cancelBtn.setMinWidth(100);
        cancelBtn.setOnAction(e -> {
            System.out.println("Delete cancelled");
            confirmDialog.setVisible(false);
            // Remove dialog from parent when cancelled
            removeDialogFromParent(confirmDialog);
        });

        MFXButton deleteBtn = new MFXButton("Delete");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setMinWidth(100);
        deleteBtn.setOnAction(e -> {
            System.out.println("Delete confirmed for event: " + event.getId());
            try {
                System.out.println("Attempting to delete event from database...");
                eventService.delete(event.getId());
                System.out.println("Event deleted successfully from database");
                loadEvents();
                confirmDialog.setVisible(false);
                // Remove dialog from parent after successful deletion
                removeDialogFromParent(confirmDialog);
                showAlert("Success", "Event deleted successfully");
            } catch (SQLException ex) {
                System.err.println("Error deleting event: " + ex.getMessage());
                ex.printStackTrace();
                showAlert("Error", "Failed to delete event: " + ex.getMessage());
            }
        });

        actionsBox.getChildren().addAll(cancelBtn, deleteBtn);
        confirmDialog.setContent(actionsBox);

        // Add the dialog to the root StackPane instead of mainContainer
        Platform.runLater(() -> {
            System.out.println("Showing delete confirmation dialog");
            StackPane root = (StackPane) mainContainer.getParent();
            if (root != null && !root.getChildren().contains(confirmDialog)) {
                root.getChildren().add(confirmDialog);
                confirmDialog.setVisible(true);
            }
        });
    }

    private void removeDialogFromParent(MFXGenericDialog dialog) {
        Platform.runLater(() -> {
            StackPane root = (StackPane) mainContainer.getParent();
            if (root != null) {
                root.getChildren().remove(dialog);
            }
        });
    }

    private boolean validateEventFields() {
        if (eventName.getText().isEmpty()) {
            showAlert("Error", "Event name is required");
            return false;
        }

        if (startDate.getValue() == null || startTime.getText().isEmpty()) {
            showAlert("Error", "Start date and time are required");
            return false;
        }

        if (endDate.getValue() == null || endTime.getText().isEmpty()) {
            showAlert("Error", "End date and time are required");
            return false;
        }

        if (eventStatus.getValue() == null) {
            showAlert("Error", "Event status is required");
            return false;
        }

        LocalTime startTimeValue = parseTime(startTime.getText());
        LocalTime endTimeValue = parseTime(endTime.getText());

        if (startTimeValue == null || endTimeValue == null) {
            showAlert("Error", "Please enter valid times in HH:mm format");
            return false;
        }

        // Create full LocalDateTime objects for comparison
        LocalDateTime startDateTime = LocalDateTime.of(startDate.getValue(), startTimeValue);
        LocalDateTime endDateTime = LocalDateTime.of(endDate.getValue(), endTimeValue);

        // Check if end time is after start time
        if (!endDateTime.isAfter(startDateTime)) {
            showAlert("Error", "End time must be after start time");
            return false;
        }

        return true;
    }

    private LocalTime parseTime(String timeStr) {
        try {
            // First try the standard format HH:mm
            return LocalTime.parse(timeStr, timeFormatter);
        } catch (Exception e) {
            try {
                // If that fails, try a more lenient parsing
                if (timeStr.matches("\\d{1,2}:\\d{2}")) {
                    // Add leading zero if needed
                    if (timeStr.length() == 4) {
                        timeStr = "0" + timeStr;
                    }
                    return LocalTime.parse(timeStr, timeFormatter);
                }
            } catch (Exception ex) {
                System.err.println("Failed to parse time: " + timeStr);
                ex.printStackTrace();
            }
            return null;
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

                // Status filter
                boolean matchesStatus = statusFilter.getValue().equals("All") ||
                    event.getStatus().equalsIgnoreCase(statusFilter.getValue());

                // Date filter
                boolean matchesDate = dateFilter.getValue() == null ||
                    event.getStartTime().toLocalDate().equals(dateFilter.getValue());

                return matchesSearch && matchesStatus && matchesDate;
            })
            .toList();

        Platform.runLater(() -> displayEvents(filteredEvents));
    }

    @FXML
    public void handleRefresh() {
        loadEvents();
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void navigateToReservations(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/palexdev/materialfx/demo/fxml/Reservation.fxml"));
            Parent reservationView = loader.load();

            // Get the controller and set the event
            ReservationController reservationController = loader.getController();
            reservationController.setCurrentEvent(event);

            // Get the current scene
            Scene currentScene = eventsContainer.getScene();

            // Get the content pane from the root AnchorPane
            AnchorPane root = (AnchorPane) currentScene.getRoot();
            StackPane contentPane = (StackPane) root.lookup("#contentPane");

            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(reservationView);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load reservations view: " + e.getMessage());
        }
    }
}
