package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.demo.model.Answer;
import io.github.palexdev.materialfx.demo.model.Reclamation;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.ReclamationService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PlayerReclamationController implements Initializable {

    public VBox mainContainer;
    public MFXComboBox statusFilter;
    @FXML
    private MFXTextField messageField;

    @FXML
    private MFXButton submitButton;

    @FXML
    private FlowPane reclamationsContainer;

    private final ReclamationService reclamationService;

    public PlayerReclamationController() {
        this.reclamationService = new ReclamationService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadMyReclamations();
    }

    @FXML
    private void handleSubmit() {
        String message = messageField.getText();
        if (message == null || message.trim().isEmpty()) {
            showAlert("Error", "Please write your message");
            return;
        }

        try {
            Reclamation reclamation = new Reclamation();
            reclamation.setMessage(message);
            reclamation.setUser(UserSession.getInstance().getCurrentUser());
            reclamation.setCreatedAt(LocalDateTime.now());
            reclamation.setStatus(Reclamation.STATUS_PENDING);

            reclamationService.create(reclamation);
            
            // Clear the field and reload reclamations
            messageField.clear();
            loadMyReclamations();
            showAlert("Success", "Your reclamation has been submitted");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to submit reclamation: " + e.getMessage());
        }
    }

    private void loadMyReclamations() {
        try {
            int userId = UserSession.getInstance().getCurrentUser().getId();
            reclamationsContainer.getChildren().clear();
            reclamationService.getReclamationsByUser(userId).forEach(this::createReclamationCard);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load reclamations: " + e.getMessage());
        }
    }

    private void createReclamationCard(Reclamation reclamation) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        card.setMinHeight(200);
        card.getStyleClass().add("reclamation-card");
        card.setPadding(new Insets(15));

        // Status
        Label statusLabel = new Label(reclamation.getStatus());
        statusLabel.getStyleClass().addAll("status-label", 
            reclamation.getStatus().equals(Reclamation.STATUS_PENDING) ? "status-pending" : "status-resolved");

        // Message
        Label messageLabel = new Label(reclamation.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("card-message");

        // Date
        Label dateLabel = new Label(reclamation.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateLabel.getStyleClass().add("card-date");

        // Answers section
        VBox answersBox = new VBox(5);
        answersBox.getStyleClass().add("answers-box");
        
        if (!reclamation.getAnswers().isEmpty()) {
            Label answersLabel = new Label("Responses:");
            answersLabel.getStyleClass().add("answers-header");
            answersBox.getChildren().add(answersLabel);
            
            for (Answer answer : reclamation.getAnswers()) {
                HBox answerBox = new HBox(10);
                answerBox.getStyleClass().add("answer-box");
                
                Label adminLabel = new Label(answer.getAdmin().getFirstname() + ":");
                adminLabel.getStyleClass().add("admin-name");
                
                Label answerMessage = new Label(answer.getMessage());
                answerMessage.setWrapText(true);
                answerMessage.getStyleClass().add("answer-message");
                
                answerBox.getChildren().addAll(adminLabel, answerMessage);
                answersBox.getChildren().add(answerBox);
            }
        }

        card.getChildren().addAll(statusLabel, messageLabel, dateLabel, answersBox);
        reclamationsContainer.getChildren().add(card);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 