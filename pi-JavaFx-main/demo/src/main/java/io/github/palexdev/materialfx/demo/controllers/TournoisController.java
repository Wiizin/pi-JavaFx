package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.demo.model.Tournois;
import io.github.palexdev.materialfx.demo.services.TournamentRegulationsGenerator;
import io.github.palexdev.materialfx.demo.services.TournoisService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.stage.StageStyle;


public class TournoisController implements Initializable {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private MFXButton addTournamentButton;



    private final TournoisService tournoisService = new TournoisService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("TournoisController initialized.");
        loadTournamentCardsFromDB();
    }

    private void loadTournamentCardsFromDB() {
        try {
            List<Tournois> tournaments = tournoisService.showAll();
            System.out.println("Number of tournaments loaded: " + tournaments.size());
            for (Tournois tournament : tournaments) {
                VBox card = createTournamentCard(tournament);
                System.out.println("Adding tournament: " + tournament.getNom());
                cardsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading tournaments: " + e.getMessage());
        }
    }

    private VBox createTournamentCard(Tournois tournament) {
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("tournament-card");

        // Create and configure the ImageView
        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        try {
            // Ensure sportify.png is in your resources folder (e.g., src/main/resources)
            Image image = new Image(getClass().getResource("/io/github/palexdev/materialfx/demo/sportify.png").toExternalForm());
            imageView.setImage(image);
        } catch (Exception e) {
            System.out.println("Image not found: " + e.getMessage());
        }

        // Create tournament name label
        Label nameLabel = new Label(tournament.getNom());
        nameLabel.getStyleClass().add("header-label");

        // Create information boxes
        HBox formatBox = createInfoBox("Format:", tournament.getFormat());
        formatBox.getStyleClass().add("status-label");
        HBox statusBox = createInfoBox("Status:", tournament.getStatus());
        statusBox.getStyleClass().add("status-label");
        HBox dateBox = createInfoBox("Date:", tournament.getStartDate() + " - " + tournament.getEndDate());
        dateBox.getStyleClass().add("status-label");
        HBox teamsBox = createInfoBox("Teams:", String.valueOf(tournament.getNbEquipe()));
        teamsBox.getStyleClass().add("status-label");
        HBox locationBox = createInfoBox("Location:", tournament.getTournoisLocation());
        locationBox.getStyleClass().add("status-label");

        // Create button container with View Details and Register buttons
        HBox buttonBox = new HBox(10);
        buttonBox.getStyleClass().add("button-container");

        MFXButton detailsButton = new MFXButton("View Reglements");
        detailsButton.getStyleClass().add("outline-button");
        detailsButton.setOnAction(event -> showTournamentDetails(tournament));

        MFXButton updateButton = new MFXButton("UPDATE");
        updateButton.getStyleClass().add("outline-button");
        updateButton.setOnAction(event -> updateTournament(tournament));

        MFXButton deleteButton = new MFXButton("Delete");
        deleteButton.getStyleClass().add("outline-button");
        deleteButton.setOnAction(event -> deleteTournament(tournament));

        buttonBox.getChildren().addAll(detailsButton, updateButton, deleteButton);
        // Assemble the card
        card.getChildren().addAll(imageView, nameLabel, formatBox, statusBox, dateBox, teamsBox, locationBox, buttonBox);
        return card;
    }

    private HBox createInfoBox(String labelText, String valueText) {
        HBox box = new HBox(10);
        Label infoLabel = new Label(labelText);
        infoLabel.getStyleClass().add("info-label");
        Label infoValue = new Label(valueText);
        box.getChildren().addAll(infoLabel, infoValue);
        return box;
    }

    private void showTournamentDetails(Tournois tournament) {
        System.out.println("Showing details for " + tournament.getNom());

        // Generate random tournament regulations
        String regulationsText = TournamentRegulationsGenerator.generateRandomRegulations();

        // Create a new stage for the popup dialog
        Stage popupStage = new Stage();
        popupStage.initStyle(StageStyle.UNDECORATED); // Remove title bar

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1B1F3B; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Label reglementsLabel = new Label(regulationsText);
        reglementsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        reglementsLabel.setWrapText(true);

        MFXScrollPane scrollPane = new MFXScrollPane(reglementsLabel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Close button
        MFXButton closeButton = new MFXButton("Close");
        closeButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        closeButton.setOnAction(e -> popupStage.close());

        root.getChildren().addAll(scrollPane, closeButton);

        Scene scene = new Scene(root, 400, 300);
        popupStage.setScene(scene);
        popupStage.show();
    }

    @FXML
    private void handleAddNewTournament() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.UNDECORATED);

        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #1B1F3B; -fx-border-radius: 10px; -fx-background-radius: 10px ;-fx-border-color: #ff9800;-fx-border-width: 2;");

        String textFieldStyle = "-fx-text-fill: white; -fx-prompt-text-fill: white; -fx-background-color: #2A2F4F; -fx-border-color: #ff9800;-fx-text-field : #ff9800;";

        // Create form fields
        MFXTextField nameField = new MFXTextField();
        nameField.setStyle(textFieldStyle);
        nameField.setFloatingText("Tournament Name");
   /*     nameField.setPromptText("Tournament Name");
        nameField.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800;");*/
        nameField.setPrefWidth(300);
        nameField.setPrefHeight(40);

        MFXTextField formatField = new MFXTextField();
        formatField.setPromptText("Format");
        formatField.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");
        formatField.setPrefWidth(300);
        formatField.setPrefHeight(40);



        MFXTextField statusField = new MFXTextField();
        statusField.setPromptText("Status");
        statusField.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");
        statusField.setPrefWidth(300);
        statusField.setPrefHeight(40);


        MFXDatePicker startDatePicker = new MFXDatePicker();
        startDatePicker.setPromptText("Start Date");
        startDatePicker.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");
        startDatePicker.setPrefWidth(300);
        startDatePicker.setPrefHeight(40);

        MFXDatePicker endDatePicker = new MFXDatePicker();
        endDatePicker.setPromptText("End Date");
        endDatePicker.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");
        endDatePicker.setPrefWidth(300);
        endDatePicker.setPrefHeight(40);

        MFXTextField teamsField = new MFXTextField();
        teamsField.setPromptText("Number of Teams");
        teamsField.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");

        teamsField.setPrefWidth(300);
        teamsField.setPrefHeight(40);



        MFXTextField locationField = new MFXTextField();
        locationField.setPromptText("Location");
        locationField.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");
        locationField.setPrefWidth(300);
        locationField.setPrefHeight(40);

        Label titleLabel = new Label("Add New Tournament");
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
                titleLabel,
                nameField,
                formatField,
                statusField,
                startDatePicker,
                endDatePicker,
                teamsField,
                locationField,
                buttonBox
        );

        // Handle save button
        saveButton.setOnAction(e -> {
            try {
                Tournois newTournament = new Tournois();
                newTournament.setNom(nameField.getText());
                newTournament.setFormat(formatField.getText());
                newTournament.setStatus(statusField.getText());
                newTournament.setStartDate(LocalDate.parse(startDatePicker.getValue().toString()));
                newTournament.setEndDate(LocalDate.parse(endDatePicker.getValue().toString()));
                newTournament.setNbEquipe(Integer.parseInt(teamsField.getText()));
                newTournament.setTournoisLocation(locationField.getText());

                // Save to database
                tournoisService.insert(newTournament);

                // Create and add new card
                VBox newCard = createTournamentCard(newTournament);
                cardsContainer.getChildren().add(newCard);

                // Close the popup
                popupStage.close();

                // Show success message
                showAlert(AlertType.INFORMATION, "Success", "Tournament added successfully!");
            } catch (Exception ex) {
                showAlert(AlertType.ERROR, "Error", "Failed to add tournament: " + ex.getMessage());
            }
        });

        // Handle cancel button
        cancelButton.setOnAction(e -> popupStage.close());

        Scene scene = new Scene(form, 400, 500);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }






    private void updateTournament(Tournois tournament) {
     /*   // Debug check at the start
        System.out.println("Opening update form for tournament with ID: " + tournament.getId());
        if (tournament.getId() == 0) {
            showAlert(AlertType.ERROR, "Error", "Invalid tournament ID");
            return;
        }*/

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.UNDECORATED);

        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #1B1F3B; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-border-color: #ff9800;-fx-border-width: 2;");


        MFXTextField nameField = new MFXTextField(tournament.getNom());
        nameField.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");
        nameField.setFloatingText("Tournament Name");
        nameField.setPrefWidth(300);
        nameField.setPrefHeight(40);

        MFXTextField formatField = new MFXTextField(tournament.getFormat());
        formatField.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");
        formatField.setFloatingText("Format");
        formatField.setPrefWidth(300);
        formatField.setPrefHeight(40);

        MFXTextField statusField = new MFXTextField(tournament.getStatus());
        statusField.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");
        statusField.setFloatingText("Status");
        statusField.setPrefWidth(300);
        statusField.setPrefHeight(40);

        MFXDatePicker startDatePicker = new MFXDatePicker();
        startDatePicker.setValue(tournament.getStartDate());
        startDatePicker.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");
        startDatePicker.setPrefWidth(300);
        startDatePicker.setPrefHeight(40);

        MFXDatePicker endDatePicker = new MFXDatePicker();
        endDatePicker.setValue(tournament.getEndDate());
        endDatePicker.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");
        endDatePicker.setPrefWidth(300);
        endDatePicker.setPrefHeight(40);

        MFXTextField teamsField = new MFXTextField(String.valueOf(tournament.getNbEquipe()));
        teamsField.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");
        teamsField.setFloatingText("Number of Teams");
        teamsField.setPrefWidth(300);
        teamsField.setPrefHeight(40);

        MFXTextField locationField = new MFXTextField(tournament.getTournoisLocation());
        locationField.setStyle("-fx-text-fill: white; -fx-background-color: #2A2F4F;-fx-border-color: #ff9800");
        locationField.setFloatingText("Location");
        locationField.setPrefWidth(300);
        locationField.setPrefHeight(40);

        Label titleLabel = new Label("Update Tournament");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        MFXButton saveButton = new MFXButton("Update");
        saveButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        saveButton.setPrefWidth(100);
        saveButton.setPrefHeight(40);

        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        cancelButton.setPrefWidth(100);
        cancelButton.setPrefHeight(40);

        buttonBox.getChildren().addAll(saveButton, cancelButton);

        form.getChildren().addAll(
                titleLabel,
                nameField,
                formatField,
                statusField,
                startDatePicker,
                endDatePicker,
                teamsField,
                locationField,
                buttonBox
        );

        saveButton.setOnAction(e -> {
            try {
                System.out.println("\n=== FORM VALUES ===");
                System.out.println("ID being updated: " + tournament.getId());
                System.out.println("New name value: " + nameField.getText());
                System.out.println("New format value: " + formatField.getText());
                System.out.println("New status value: " + statusField.getText());
                System.out.println("New start date: " + startDatePicker.getValue());
                System.out.println("New end date: " + endDatePicker.getValue());
                System.out.println("New teams value: " + teamsField.getText());
                System.out.println("New location value: " + locationField.getText());

                // Update tournament object
                tournament.setNom(nameField.getText());
                tournament.setFormat(formatField.getText());
                tournament.setStatus(statusField.getText());
                tournament.setStartDate(startDatePicker.getValue());
                tournament.setEndDate(endDatePicker.getValue());
                tournament.setNbEquipe(Integer.parseInt(teamsField.getText()));
                tournament.setTournoisLocation(locationField.getText());

                int updateResult = tournoisService.update(tournament);

                if (updateResult > 0) {
                    cardsContainer.getChildren().clear();
                    loadTournamentCardsFromDB();
                    popupStage.close();
                    showAlert(AlertType.INFORMATION, "Success",
                            "Tournament updated successfully! Rows affected: " + updateResult);
                } else {
                    showAlert(AlertType.WARNING, "Update Failed",
                            "No changes were made to the tournament. Please check if the data is different from the original.");
                }
            } catch (NumberFormatException ex) {
                showAlert(AlertType.ERROR, "Input Error",
                        "Please enter a valid number for the team count!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(AlertType.ERROR, "Database Error",
                        "Failed to update tournament: " + ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(AlertType.ERROR, "Error",
                        "An unexpected error occurred: " + ex.getMessage());
            }
        });
        cancelButton.setOnAction(e -> popupStage.close());

        Scene scene = new Scene(form, 400, 500);
        popupStage.setScene(scene);
        popupStage.show();
    }


    private void deleteTournament(Tournois tournament) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete this tournament?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int result = tournoisService.delete(tournament);
                    if (result > 0) {
                        cardsContainer.getChildren().clear();
                        loadTournamentCardsFromDB();
                        showAlert(AlertType.INFORMATION, "Success", "Tournament deleted successfully!");
                    } else {
                        showAlert(AlertType.WARNING, "Deletion Failed", "No tournament found with that ID.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(AlertType.ERROR, "Database Error", "Failed to delete tournament: " + e.getMessage());
                }
            }
        });
    }


    private void registerForTournament(Tournois tournament) {
        System.out.println("Registering for " + tournament.getNom());
        // Implement registration logic here
    }
}
