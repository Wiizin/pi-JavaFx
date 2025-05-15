package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.demo.model.Answer;
import io.github.palexdev.materialfx.demo.model.Reclamation;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.AnswerService;
import io.github.palexdev.materialfx.demo.services.ReclamationService;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReclamationController implements Initializable {

    public MFXTextField responseField;
    public StackPane dialogContainer;
    public MFXGenericDialog responseDialog;
    public MFXComboBox statusFilter;
    @FXML
    private VBox mainContainer;

    @FXML
    private FlowPane reclamationsContainer;

    private final ReclamationService reclamationService;
    private final AnswerService answerService;
    private final ObservableList<Reclamation> reclamations;
    private Answer currentEditingAnswer; // To track which answer is being edited

    public ReclamationController() {
        this.reclamationService = new ReclamationService();
        this.answerService = new AnswerService();
        this.reclamations = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadReclamations();
        initializeDialog();
        initializeStatusFilter();
    }

    private void initializeDialog() {
        // Hide dialog initially
        responseDialog.setVisible(false);
        
        // Set up close button handler
        responseDialog.setOnClose(event -> {
            responseDialog.setVisible(false);
            responseField.clear();
        });
    }

    private void initializeStatusFilter() {
        ObservableList<String> statuses = FXCollections.observableArrayList(
            "All",  // Add "All" option
            Reclamation.STATUS_PENDING,
            Reclamation.STATUS_RESOLVED
        );
        statusFilter.setItems(statuses);
        statusFilter.selectFirst(); // Select "All" by default
        statusFilter.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterReclamations(newVal.toString());
            }
        });
    }

    private void filterReclamations(String status) {
        try {
            reclamations.clear();
            // Get all reclamations first
            var allReclamations = reclamationService.getAll();
            
            // Filter in memory based on status
            if (status.equals("All")) {
                reclamations.addAll(allReclamations);
            } else {
                reclamations.addAll(
                    allReclamations.stream()
                        .filter(r -> r.getStatus().equals(status))
                        .toList()
                );
            }
            displayReclamations();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to filter reclamations: " + e.getMessage());
        }
    }

    private void loadReclamations() {
        try {
            reclamations.clear();
            reclamations.addAll(reclamationService.getAll());
            displayReclamations();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load reclamations: " + e.getMessage());
        }
    }

    private void displayReclamations() {
        reclamationsContainer.getChildren().clear();
        for (Reclamation reclamation : reclamations) {
            reclamationsContainer.getChildren().add(createReclamationCard(reclamation));
        }
    }

    private VBox createReclamationCard(Reclamation reclamation) {
        VBox card = new VBox(15);
        card.setPrefWidth(350);
        card.setMaxWidth(350);
        card.setMinHeight(200);
        card.getStyleClass().add("reclamation-card");
        card.setPadding(new Insets(20));

        // Header with user info, status and delete button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("card-header");
        header.setPadding(new Insets(0, 0, 10, 0));

        VBox userInfo = new VBox(5);
        Label userLabel = new Label(reclamation.getUser().getFirstname() + " " + reclamation.getUser().getLastName());
        userLabel.getStyleClass().add("user-info");
        
        Label dateLabel = new Label(reclamation.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateLabel.getStyleClass().add("timestamp");
        
        userInfo.getChildren().addAll(userLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(reclamation.getStatus());
        statusLabel.getStyleClass().addAll("status-badge", 
            reclamation.getStatus().equals(Reclamation.STATUS_PENDING) ? "status-pending" : "status-resolved");

        // Add delete button
        MFXButton deleteButton = new MFXButton("Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> handleDelete(reclamation));

        header.getChildren().addAll(userInfo, spacer, statusLabel, deleteButton);

        // Message content
        VBox messageBox = new VBox(10);
        messageBox.getStyleClass().add("message-box");
        messageBox.setPadding(new Insets(15));
        messageBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        Label messageLabel = new Label(reclamation.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("message-text");
        messageBox.getChildren().add(messageLabel);

        // Answers section
        VBox answersBox = new VBox(10);
        answersBox.getStyleClass().add("answers-section");
        
        if (!reclamation.getAnswers().isEmpty()) {
            Label answersLabel = new Label("Responses");
            answersLabel.getStyleClass().add("section-header");
            answersLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b3990;");
            answersBox.getChildren().add(answersLabel);
            
            for (Answer answer : reclamation.getAnswers()) {
                VBox answerBox = new VBox(5);
                answerBox.getStyleClass().add("answer-item");
                answerBox.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #e0e6ed; -fx-border-radius: 8;");
                
                Label adminLabel = new Label(answer.getAdmin().getFirstname());
                adminLabel.getStyleClass().add("admin-name");
                adminLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b3990;");
                
                Label answerMessage = new Label(answer.getMessage());
                answerMessage.setWrapText(true);
                answerMessage.getStyleClass().add("answer-text");

                // Add edit button if the answer is from the current admin
                if (answer.getAdmin().getId() == UserSession.getInstance().getCurrentUser().getId()) {
                    HBox answerActions = new HBox(5);
                    answerActions.setAlignment(Pos.CENTER_RIGHT);
                    
                    MFXButton editButton = new MFXButton("Edit");
                    editButton.getStyleClass().add("edit-button");
                    editButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
                    editButton.setOnAction(e -> showEditAnswerDialog(answer));
                    
                    answerActions.getChildren().add(editButton);
                    answerBox.getChildren().addAll(adminLabel, answerMessage, answerActions);
                } else {
                    answerBox.getChildren().addAll(adminLabel, answerMessage);
                }
                
                answersBox.getChildren().add(answerBox);
            }
        }

        // Reply button
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(10, 0, 0, 0));

        MFXButton replyButton = new MFXButton("Reply");
        replyButton.getStyleClass().add("send-button");
        replyButton.setOnAction(e -> showResponseDialog(reclamation));
        actionBox.getChildren().add(replyButton);

        card.getChildren().addAll(header, messageBox, answersBox, actionBox);
        return card;
    }

    private void handleReply(Reclamation reclamation, String message, VBox answersBox) {
        if (message == null || message.trim().isEmpty()) {
            showAlert("Error", "Please write a response");
            return;
        }

        try {
            Answer answer = new Answer();
            answer.setMessage(message);
            answer.setReclamation(reclamation);
            answer.setAdmin(UserSession.getInstance().getCurrentUser());

            answerService.create(answer);
            reclamation.addAnswer(answer);
            reclamation.setStatus(Reclamation.STATUS_RESOLVED);  // Update status when replied
            reclamationService.update(reclamation);

            // Update UI
            loadReclamations();
            showAlert("Success", "Response added successfully");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to add response: " + e.getMessage());
        }
    }

    private void handleDelete(Reclamation reclamation) {
        Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Reclamation");
        confirmDialog.setHeaderText("Are you sure you want to delete this reclamation?");
        confirmDialog.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete all associated answers first
                for (Answer answer : reclamation.getAnswers()) {
                    answerService.delete(answer.getId());
                }
                // Then delete the reclamation
                reclamationService.delete(reclamation.getId());
                loadReclamations(); // Refresh the view
                showAlert("Success", "Reclamation deleted successfully");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to delete reclamation: " + e.getMessage());
            }
        }
    }

    private void showEditAnswerDialog(Answer answer) {
        currentEditingAnswer = answer;
        responseField.setText(answer.getMessage());
        responseDialog.setVisible(true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleRefresh(ActionEvent actionEvent) {
        loadReclamations();
    }

    @FXML
    public void handleCancelResponse(ActionEvent actionEvent) {
        currentEditingAnswer = null; // Reset the editing state
        responseDialog.setVisible(false);
        responseField.clear();
    }

    @FXML
    public void handleSendResponse(ActionEvent actionEvent) {
        String response = responseField.getText();
        if (response == null || response.trim().isEmpty()) {
            showAlert("Error", "Please write a response");
            return;
        }

        try {
            if (currentEditingAnswer != null) {
                // Update existing answer
                currentEditingAnswer.setMessage(response);
                answerService.update(currentEditingAnswer);
                currentEditingAnswer = null; // Reset the editing state
                showAlert("Success", "Response updated successfully");
            } else {
                // Create new answer
                Reclamation selectedReclamation = (Reclamation) responseDialog.getUserData();
                if (selectedReclamation != null) {
                    handleReply(selectedReclamation, response, null);
                }
            }
            responseDialog.setVisible(false);
            responseField.clear();
            loadReclamations(); // Refresh the view
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save response: " + e.getMessage());
        }
    }

    // Add this method to show the dialog for a specific reclamation
    public void showResponseDialog(Reclamation reclamation) {
        responseDialog.setUserData(reclamation);  // Store the reclamation for later use
        responseField.clear();
        responseDialog.setVisible(true);
    }
} 