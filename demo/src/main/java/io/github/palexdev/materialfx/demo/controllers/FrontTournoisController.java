
package io.github.palexdev.materialfx.demo.controllers;

        import io.github.palexdev.materialfx.controls.*;
        import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
        import io.github.palexdev.materialfx.demo.model.Matches;
        import io.github.palexdev.materialfx.demo.model.Tournois;
        import io.github.palexdev.materialfx.demo.services.MatchesService;
        import io.github.palexdev.materialfx.demo.services.TournamentRegulationsGenerator;
        import io.github.palexdev.materialfx.demo.services.TournoisService;
        import io.github.palexdev.materialfx.demo.services.MatchStatusEnum;
        import io.github.palexdev.materialfx.demo.controllers.MatchesController;
        import javafx.beans.binding.Bindings;
        import javafx.collections.FXCollections;
        import javafx.fxml.FXML;
        import javafx.fxml.Initializable;
        import javafx.geometry.Insets;
        import javafx.geometry.Pos;
        import javafx.scene.Node;
        import javafx.scene.Scene;
        import javafx.scene.control.Alert.AlertType;
        import javafx.scene.control.*;
        import javafx.scene.image.Image;
        import javafx.scene.image.ImageView;
        import javafx.scene.layout.FlowPane;
        import javafx.scene.layout.HBox;
        import javafx.scene.layout.VBox;
        import javafx.stage.Modality;
        import javafx.stage.Stage;
        import javafx.stage.StageStyle;
        import java.net.URL;
        import java.sql.SQLException;
        import java.time.LocalDate;
        import java.time.format.DateTimeFormatter;
        import java.util.Arrays;
        import java.util.List;
        import java.util.ResourceBundle;
        import java.util.stream.Collectors;

public class FrontTournoisController implements Initializable {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private MFXButton addTournamentButton;

    private final TournoisService tournoisService = new TournoisService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("FrontTournoisController initialized.");
        loadTournamentCardsFromDB();
    }



    private void showMatchesPopup(Tournois tournament) {
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);

            VBox root = new VBox(20);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: white; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-border-color: #ff9800; -fx-border-width: 2;");

            // Create header
            Label titleLabel = new Label("Matches for " + tournament.getNom());
            titleLabel.setStyle("-fx-text-fill: #1B1B3B; -fx-font-size: 18px; -fx-font-weight: bold;");

            // Initialize matches table
            MFXPaginatedTableView<Matches> matchesTable = new MFXPaginatedTableView<>();
            matchesTable.setPrefSize(800, 600);

            // Set up table columns
            MFXTableColumn<Matches> teamAColumn = new MFXTableColumn<>("Team A", false);
            teamAColumn.setPrefWidth(150);
            teamAColumn.setMinWidth(100);
            teamAColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getTeamAName));

            MFXTableColumn<Matches> teamBColumn = new MFXTableColumn<>("Team B", false);
            teamBColumn.setPrefWidth(150);
            teamBColumn.setMinWidth(100);
            teamBColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getTeamBName));

            MFXTableColumn<Matches> scoreAColumn = new MFXTableColumn<>("Score A", false);
            scoreAColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getScoreTeamA));

            MFXTableColumn<Matches> scoreBColumn = new MFXTableColumn<>("Score B", false);
            scoreBColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getScoreTeamB));

            MFXTableColumn<Matches> statusColumn = new MFXTableColumn<>("Status", false);
            statusColumn.setRowCellFactory(match -> {
                MFXTableRowCell<Matches, String> cell = new MFXTableRowCell<>(Matches::getStatus);
                cell.styleProperty().bind(Bindings.createStringBinding(() -> {
                    String status = cell.getText();
                    if (status == null) return "";
                    switch (status.toLowerCase()) {
                        case "live": return "-fx-text-fill: #00C853;";
                        case "upcoming": return "-fx-text-fill: #2196F3;";
                        case "finished": return "-fx-text-fill: #757575;";
                        default: return "";
                    }
                }, cell.textProperty()));
                return cell;
            });

            MFXTableColumn<Matches> timeColumn = new MFXTableColumn<>("Match Time", false);
            timeColumn.setPrefWidth(150);
            timeColumn.setMinWidth(100);
            timeColumn.setRowCellFactory(match -> new MFXTableRowCell<>(m ->
                    m.getMatchTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

            MFXTableColumn<Matches> locationColumn = new MFXTableColumn<>("Location", false);
            locationColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getLocationMatch));

            // Add columns to table
            matchesTable.getTableColumns().addAll(
                    teamAColumn, scoreAColumn, scoreBColumn, teamBColumn,
                    statusColumn, timeColumn, locationColumn
            );

            // Load matches for this tournament
            try {
                MatchesService matchesService = new MatchesService();
                List<Matches> tournamentMatches = matchesService.showAll().stream()
                        .filter(match -> match.getIdTournoi() == tournament.getId())
                        .collect(Collectors.toList());
                matchesTable.setItems(FXCollections.observableArrayList(tournamentMatches));
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Error", "Failed to load matches");
            }

            // Configure table properties
            matchesTable.setTableRowFactory(match -> {
                MFXTableRow<Matches> row = new MFXTableRow<>(matchesTable, match);
                row.setStyle("-fx-background-color: transparent;");
                return row;
            });

            // Create close button
            MFXButton closeButton = new MFXButton("Close");
            closeButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
            closeButton.setOnAction(e -> popupStage.close());

            // Add components to root
            root.getChildren().addAll(titleLabel, matchesTable, closeButton);

            Scene scene = new Scene(root, 850, 700);
            popupStage.setScene(scene);
            popupStage.show();
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

        // Create button container with View Details, Update and Delete buttons
        HBox buttonBox = new HBox(10);
        buttonBox.getStyleClass().add("button-container");

        MFXButton detailsButton = new MFXButton("Reglements");
        detailsButton.getStyleClass().add("outline-buttonn");
        detailsButton.setOnAction(event -> showTournamentDetails(tournament));
        MFXButton viewMatchesButton = new MFXButton("Matches");
        viewMatchesButton.getStyleClass().add("outline-buttonn");
        viewMatchesButton.setOnAction(event -> showMatchesPopup(tournament));

        buttonBox.getChildren().addAll(detailsButton, viewMatchesButton);

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
        root.setStyle("-fx-background-color: #1B1F3B; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-alignment: center; -fx-text-alignment: center;");

        Label reglementsLabel = new Label(regulationsText);
        reglementsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-alignment: center; -fx-text-alignment: center;");
        reglementsLabel.setWrapText(true);

        MFXScrollPane scrollPane = new MFXScrollPane(reglementsLabel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-alignment: center; -fx-text-alignment: center;");

        // Close button
        MFXButton closeButton = new MFXButton("Close");
        closeButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        closeButton.setOnAction(e -> popupStage.close());

        root.getChildren().addAll(scrollPane, closeButton);

        Scene scene = new Scene(root, 400, 340);
        popupStage.setScene(scene);
        popupStage.show();
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