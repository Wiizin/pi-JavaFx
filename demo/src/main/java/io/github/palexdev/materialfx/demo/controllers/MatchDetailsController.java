package io.github.palexdev.materialfx.demo.controllers;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.demo.model.Matches;
import io.github.palexdev.materialfx.demo.model.Tournois;
import io.github.palexdev.materialfx.demo.services.MatchesService;
import io.github.palexdev.materialfx.demo.services.TournoisService;
import io.github.palexdev.materialfx.filter.StringFilter;
import javafx.animation.FadeTransition;
import javafx.animation.KeyValue;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.ResourceBundle;
import io.github.palexdev.materialfx.demo.services.MatchStatusEnum;
import javafx.scene.layout.*;

import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Insets;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;


public class MatchDetailsController implements Initializable {

    @FXML private MFXPaginatedTableView<Matches> paginated;
    @FXML private Label headerLabel;
    @FXML private MFXButton addButton;
    @FXML private MFXButton refreshButton;
    @FXML private MFXComboBox<String> tournamentFilter;
    @FXML private MFXComboBox<String> statusFilter;
    @FXML private MFXTextField searchField;

    private final MatchesService matchesService;
    private final TournoisService tournoisService;
    private final ObservableList<Matches> matches;
    private final ObservableList<Tournois> tournaments;
    private FilteredList<Matches> filteredMatches;
    private  MatchStatusEnum status;
    Image teamImage = new Image(getClass().getResource("/io/github/palexdev/materialfx/demo/sportify.png").toExternalForm());//new Image("/io/github/palexdev/materialfx/demo/sportify.png"); // l'uplod me xampp lhne
    ImageView teamImageView = new ImageView(teamImage);

    public MatchDetailsController() {
        System.out.println("MatchesController: Constructor called");
        this.matchesService = new MatchesService();
        this.tournoisService = new TournoisService();
        this.matches = FXCollections.observableArrayList();
        this.tournaments = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("MatchesController: Initializing controller");

        if (paginated == null || headerLabel == null || addButton == null ||
                refreshButton == null || tournamentFilter == null ||
                statusFilter == null || searchField == null) {
            System.err.println("MatchesController: One or more FXML elements not injected properly");
            if (paginated == null) System.err.println("- paginated is null");
            if (headerLabel == null) System.err.println("- headerLabel is null");
            if (addButton == null) System.err.println("- addButton is null");
            if (refreshButton == null) System.err.println("- refreshButton is null");
            if (tournamentFilter == null) System.err.println("- tournamentFilter is null");
            if (statusFilter == null) System.err.println("- statusFilter is null");
            if (searchField == null) System.err.println("- searchField is null");
            return;
        }

        setupTable();
        setupFilters();
        setupButtons();
        loadMatches();
        updateLiveCount();

        System.out.println("MatchesController: Initialization completed");
    }

    private void setupFilters() {
        System.out.println("MatchesController: Setting up filters");

        try {
            tournaments.clear();
            tournaments.addAll(tournoisService.showAll());
            System.out.println("Loaded " + tournaments.size() + " tournaments from database");

            tournamentFilter.getItems().clear();
            tournamentFilter.getItems().add("All Tournaments");
            tournaments.forEach(tournament ->
                    tournamentFilter.getItems().add(tournament.getNom())
            );
            tournamentFilter.selectFirst();

        } catch (SQLException e) {
            System.err.println("Failed to load tournaments: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Error Loading Tournaments",
                    "Failed to load tournaments from database.");
        }


        statusFilter.getItems().addAll(
                Arrays.stream(MatchStatusEnum.values())
                        .map(MatchStatusEnum::getDisplayValue)
                        .collect(Collectors.toList())
        );

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Search field changed: " + newVal);
            filterMatches();
        });

        tournamentFilter.setOnAction(e -> {
            System.out.println("Tournament filter changed: " + tournamentFilter.getValue());
            filterMatches();
        });

        statusFilter.setOnAction(e -> {
            System.out.println("Status filter changed: " + statusFilter.getValue());
            filterMatches();
        });
    }

    private void filterMatches() {
        if (filteredMatches == null) {
            System.err.println("MatchesController: filteredMatches is null during filtering");
            return;
        }

        String searchText = searchField.getText().toLowerCase();
        String tournamentName = tournamentFilter.getValue();
        String status = statusFilter.getValue();

        System.out.println("Applying filters - Search: '" + searchText +
                "', Tournament: '" + tournamentName +
                "', Status: '" + status + "'");

        filteredMatches.setPredicate(match -> {
            boolean matchesSearch = searchText.isEmpty()
                    || match.getTeamAName().toLowerCase().contains(searchText)
                    || match.getTeamBName().toLowerCase().contains(searchText);


            boolean matchesTournament;
            if (tournamentName.equals("All Tournaments")) {
                matchesTournament = true;
            } else {
                matchesTournament = tournaments.stream()
                        .filter(t -> t.getNom().equals(tournamentName))
                        .findFirst()
                        .map(t -> match.getIdTournoi() == t.getId())
                        .orElse(false);
            }

            boolean matchesStatus = status.equals("All Matches")
                    || match.getStatus().equalsIgnoreCase(status);

            return matchesSearch && matchesTournament && matchesStatus;
        });

        System.out.println("Filtered matches count: " + filteredMatches.size());
    }

    public void setupTable() {
        System.out.println("MatchesController: Setting up table");

        if (paginated == null) {
            System.err.println("MatchesController: paginated table is null during setup");
            return;
        }

        MFXTableColumn<Matches> teamAColumn = new MFXTableColumn<>("Team A", false);
        teamAColumn.setPrefWidth(150);
        teamAColumn.setMinWidth(100);
        teamAColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getTeamAName));

        MFXTableColumn<Matches> teamBColumn = new MFXTableColumn<>("Team B", false);
        teamBColumn.setPrefWidth(150);
        teamBColumn.setMinWidth(100);
        teamBColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getTeamBName));

        MFXTableColumn<Matches> idColumn = new MFXTableColumn<>("ID", false, Comparator.comparing(Matches::getId));
        idColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getId));

        MFXTableColumn<Matches> scoreAColumn = new MFXTableColumn<>("Score A", false, Comparator.comparing(Matches::getScoreTeamA));
        scoreAColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getScoreTeamA));

        MFXTableColumn<Matches> scoreBColumn = new MFXTableColumn<>("Score B", false, Comparator.comparing(Matches::getScoreTeamB));
        scoreBColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getScoreTeamB));

        MFXTableColumn<Matches> statusColumn = new MFXTableColumn<>("Status", false, Comparator.comparing(Matches::getStatus));
        statusColumn.setRowCellFactory(match -> {
            MFXTableRowCell<Matches, String> cell = new MFXTableRowCell<>(Matches::getStatus);
            cell.styleProperty().bind(Bindings.createStringBinding(() -> {
                String status = cell.getText();
                if (status == null) return "";
                switch (status.toLowerCase()) {
                    case "live": return "status-live";
                    case "upcoming": return "status-upcoming";
                    case "finished": return "status-finished";
                    default: return "";
                }
            }, new javafx.beans.Observable[]{ cell.textProperty() }));
            return cell;
        });

        MFXTableColumn<Matches> timeColumn = new MFXTableColumn<>("Match Time", false, Comparator.comparing(Matches::getMatchTime));
        timeColumn.setPrefWidth(150);
        timeColumn.setMinWidth(100);
        timeColumn.setRowCellFactory(match -> new MFXTableRowCell<>(m ->
                m.getMatchTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        MFXTableColumn<Matches> locationColumn = new MFXTableColumn<>("Location", false, Comparator.comparing(Matches::getLocationMatch));
        locationColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getLocationMatch));

        MFXTableColumn<Matches> tournoiColumn = new MFXTableColumn<>("Tournament", false);
        tournoiColumn.setPrefWidth(200);
        tournoiColumn.setMinWidth(150);
        tournoiColumn.setRowCellFactory(match -> new MFXTableRowCell<>(m -> {
            return tournaments.stream()
                    .filter(t -> t.getId() == m.getIdTournoi())
                    .findFirst()
                    .map(Tournois::getNom)
                    .orElse("Unknown Tournament");
        }));

        MFXTableColumn<Matches> actionColumn = new MFXTableColumn<>("Action", false);
        actionColumn.setPrefWidth(200);
        actionColumn.setMinWidth(150);
        actionColumn.setRowCellFactory(match -> {
            MFXTableRowCell<Matches, String> cell = new MFXTableRowCell<>(m -> "");
            HBox buttonContainer = new HBox(10);
            buttonContainer.setPadding(new Insets(2));
            buttonContainer.setAlignment(Pos.CENTER);
            buttonContainer.getChildren().addAll(); // Add buttons as needed
            cell.setGraphic(buttonContainer);
            return cell;
        });

        paginated.getTableColumns().addAll(
                tournoiColumn, teamAColumn,
                scoreAColumn, scoreBColumn,
                teamBColumn, statusColumn,
                timeColumn, locationColumn
        );

        paginated.getTableColumns().forEach(column -> {
            column.setMinWidth(100);
            System.out.println("Configured column: " + column.getText());
        });

        paginated.getFilters().addAll(
                new StringFilter<>("Status", Matches::getStatus),
                new StringFilter<>("Location", Matches::getLocationMatch)
        );
        paginated.setCurrentPage(1);
        paginated.setRowsPerPage(10);
        paginated.setTableRowFactory(match -> {
            MFXTableRow<Matches> row = new MFXTableRow<>(paginated, match);
            row.setMinHeight(50);
            row.setPrefHeight(50);
            row.setMaxHeight(50);

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {  // Double-click to show matches for tournament
                    System.out.println("Clicked on tournament with ID: " + match.getIdTournoi());
                    showMatchesForTournament(match.getIdTournoi());
                } else if (event.getClickCount() == 1) {  // Single-click to show match details
                    System.out.println("Clicked on match with ID: " + match.getId());
                    showMatchDetailsDialog(match);
                }
            });

            return row;
        });
    }

    // Method to show matches for the selected tournament
    private void showMatchesForTournament(int tournamentId) {
        Stage matchesStage = new Stage();
        matchesStage.initModality(Modality.APPLICATION_MODAL);
        matchesStage.setTitle("Matches for Tournament ID: " + tournamentId);

        MFXTableView<Matches> matchesTable = new MFXTableView<>();
        setupMatchesTable(matchesTable, tournamentId);
        loadMatchesForTournament(matchesTable, tournamentId);

        VBox root = new VBox(matchesTable);
        Scene scene = new Scene(root, 600, 400);
        matchesStage.setScene(scene);
        matchesStage.show();
    }

    // Method to setup columns for the matches table
    private void setupMatchesTable(MFXTableView<Matches> matchesTable, int tournamentId) {
        // Define columns for matches table here
    }

    // Method to load matches for a specific tournament
    private void loadMatchesForTournament(MFXTableView<Matches> matchesTable, int tournamentId) {
        try {
            ObservableList<Matches> tournamentMatches = FXCollections.observableArrayList(matchesService.getMatchesByTournament(tournamentId));
            matchesTable.setItems(tournamentMatches);
        } catch (SQLException e) {
            System.err.println("Failed to load matches for tournament ID: " + tournamentId);
            e.printStackTrace();
        }
    }

    //********************************************************************************************************************************
    // team plear w scrore ne9ssin lenna
    private void showMatchDetailsDialog(Matches match) {
        // Determine border color based on match status
        String borderColor;
        switch (match.getStatus().toLowerCase()) {
            case "live":
                borderColor = "#008000"; // red
                break;
            case "upcoming":
                borderColor = "#0000ff"; // blue
                break;
            case "finished":
                borderColor = "#ff0000"; // green
                break;
            default:
                borderColor = "#ff9800"; // fallback (orange)
                break;
        }

        // Create a transparent stage to allow for an overlay effect.
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.TRANSPARENT);

        // Create a root StackPane with a semi-transparent overlay background.
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");

        // Main container with rounded corners, drop shadow, and dynamic border color.
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; " +
                "-fx-border-color: " + borderColor + "; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 4);");

        // ------------------------
        // Draggable Header Section
        // ------------------------
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        final Delta dragDelta = new Delta();

        header.setOnMousePressed(e -> {
            dragDelta.x = popupStage.getX() - e.getScreenX();
            dragDelta.y = popupStage.getY() - e.getScreenY();
        });
        header.setOnMouseDragged(e -> {
            popupStage.setX(e.getScreenX() + dragDelta.x);
            popupStage.setY(e.getScreenY() + dragDelta.y);
        });

        Label titleLabel = new Label("Match Details");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1B1B3B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MFXButton closeButton = new MFXButton("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-text-fill: #1B1B3B;");
        closeButton.setOnAction(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), mainContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> popupStage.close());
            fadeOut.play();
        });
        header.getChildren().addAll(titleLabel, spacer, closeButton);

        // ---------------------------
        // Teams Section
        // ---------------------------
        HBox teamsContainer = new HBox(40);
        teamsContainer.setAlignment(Pos.CENTER);
        teamsContainer.setPadding(new Insets(20, 0, 20, 0));
        teamsContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        VBox teamABox = createTeamBox(match.getTeamAName(), match.getScoreTeamA());

        // "VS" label with orange accent (remains unchanged or can be modified as needed)
        Label vsLabel = new Label("VS");
        vsLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #000000;");

        VBox teamBBox = createTeamBox(match.getTeamBName(), match.getScoreTeamB());
        teamsContainer.getChildren().addAll(teamABox, vsLabel, teamBBox);

        // -------------------------------
        // Match Information Section
        // -------------------------------
        VBox infoContainer = new VBox(15);
        infoContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-padding: 20;");

        // Status with dynamic color (this color can be independent from border color)
        String statusColor = switch (match.getStatus().toLowerCase()) {
            case "live" -> "#28a745";
            case "upcoming" -> "#007bff";
            case "finished" -> "#dc3545";
            default -> "#6c757d";
        };

        HBox statusBox = createInfoRow("Status", match.getStatus(), statusColor);
        HBox timeBox = createInfoRow("Time", match.getMatchTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        HBox locationBox = createInfoRow("Location", match.getLocationMatch());
        HBox tournamentBox = createInfoRow("Tournament", tournaments.stream()
                .filter(t -> t.getId() == match.getIdTournoi())
                .findFirst()
                .map(Tournois::getNom)
                .orElse("Unknown"));
        infoContainer.getChildren().addAll(statusBox, timeBox, locationBox, tournamentBox);

        // -------------------------------
        // Live Score Update for LIVE matches
        // -------------------------------
        if ("live".equalsIgnoreCase(match.getStatus())) {
            Timeline scoreUpdateTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(30), event -> {
                        try {
                            Matches updatedMatch = matchesService.getMatchById(match.getId());
                            if (updatedMatch != null) {
                                updateScoreDisplay(teamABox, teamBBox, updatedMatch);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    })
            );
            scoreUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
            scoreUpdateTimeline.play();
            popupStage.setOnHiding(e -> scoreUpdateTimeline.stop());
        }

        // -------------------------------
        // Additional Actions Section with Hover Effects
        // -------------------------------
        HBox actionsContainer = new HBox(10);
        actionsContainer.setAlignment(Pos.CENTER);
        actionsContainer.setPadding(new Insets(20, 0, 0, 0));

        MFXButton shareButton = new MFXButton("Share");
        shareButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px;");
        addHoverEffect(shareButton, "#0056b3", "#007bff");
        shareButton.setOnAction(e -> shareMatchDetails(match));

        MFXButton statsButton = new MFXButton("More Stats");
        statsButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px;");
        addHoverEffect(statsButton, "#1e7e34", "#28a745");
        statsButton.setOnAction(e -> showMoreStats(match));

        actionsContainer.getChildren().addAll(shareButton, statsButton);

        // -------------------------------
        // Combine All Sections
        // -------------------------------
        VBox content = new VBox(20, header, teamsContainer, infoContainer, actionsContainer);
        mainContainer.getChildren().add(content);
        root.getChildren().add(mainContainer);
        StackPane.setAlignment(mainContainer, Pos.CENTER);

        Scene scene = new Scene(root, Color.TRANSPARENT);
        popupStage.setScene(scene);

        // --------------
        // Show Animations
        // --------------
        mainContainer.setOpacity(0);
        mainContainer.setTranslateY(-20);
        Timeline showTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5),
                        new KeyValue(mainContainer.opacityProperty(), 1),
                        new KeyValue(mainContainer.translateYProperty(), 0)
                )
        );
        showTimeline.play();

        popupStage.show();
    }

    // Helper method to add hover effects to buttons.
    private void addHoverEffect(MFXButton button, String hoverColor, String normalColor) {
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle().replace(normalColor, hoverColor)));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace(hoverColor, normalColor)));
    }

    // Helper class for drag functionality
    private static class Delta {
        double x, y;
    }


    //************************************************************************************************
    private void shareMatchDetails(Matches match) {
        System.out.println("Sharing match details for match ID: " + match.getId());
        showAlert("Share Match Details", "Match details shared successfully!");
    }

    private VBox createTeamBox(String teamName, int score) {
        VBox teamBox = new VBox(10);
        teamBox.setAlignment(Pos.CENTER);
        teamBox.setPadding(new Insets(15));
        teamBox.setMinWidth(200);

        // Team Icon (placeholder circle)
        Region teamIcon = new Region();
        teamIcon.setMinSize(60, 60);
        teamIcon.setMaxSize(60, 60);
        teamIcon.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 30;");

        Label nameLabel = new Label(teamName);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1B1B3B;");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label scoreLabel = new Label(String.valueOf(score));
        scoreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");

        teamBox.getChildren().addAll(teamIcon, nameLabel, scoreLabel);
        return teamBox;
    }

    private HBox createInfoRow(String label, String value) {
        return createInfoRow(label, value, "#1B1B3B");
    }

    private HBox createInfoRow(String label, String value, String valueColor) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        labelNode.setMinWidth(100);

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + valueColor + ";");

        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    private void updateScoreDisplay(VBox teamABox, VBox teamBBox, Matches updatedMatch) {
        Platform.runLater(() -> {
            Label scoreALabel = (Label) teamABox.getChildren().get(2);
            Label scoreBLabel = (Label) teamBBox.getChildren().get(2);

            scoreALabel.setText(String.valueOf(updatedMatch.getScoreTeamA()));
            scoreBLabel.setText(String.valueOf(updatedMatch.getScoreTeamB()));
        });
    }


    private void setupButtons() {
        System.out.println("MatchesController: Setting up buttons");
       // addButton.setOnAction(event -> handleAddMatch());
        refreshButton.setOnAction(event -> refreshTable());
    }

    private void loadMatches() {
        System.out.println("MatchesController: Loading matches");
        try {
            matches.clear();
            var loadedMatches = matchesService.showAll();
            System.out.println("Loaded " + loadedMatches.size() + " matches from database");
            matches.addAll(loadedMatches);

            filteredMatches = new FilteredList<>(matches, p -> true);

            filteredMatches.addListener((ListChangeListener<Matches>) change -> {
                System.out.println("Filtered list changed, new size: " + filteredMatches.size());
            });

            paginated.setItems(filteredMatches);
            System.out.println("Set " + filteredMatches.size() + " matches to table");

        } catch (SQLException e) {
            System.err.println("MatchesController: Failed to load matches");
            e.printStackTrace();
            showErrorDialog("Error Loading Matches", "Failed to load matches from database.");
        }
    }

    private void refreshTournaments() {
        try {
            String currentSelection = tournamentFilter.getValue();

            tournaments.clear();
            tournaments.addAll(tournoisService.showAll());

            tournamentFilter.getItems().clear();
            tournamentFilter.getItems().add("All Tournaments");
            tournaments.forEach(tournament ->
                    tournamentFilter.getItems().add(tournament.getNom())
            );

            if (tournamentFilter.getItems().contains(currentSelection)) {
                tournamentFilter.setValue(currentSelection);
            } else {
                tournamentFilter.selectFirst();
            }

        } catch (SQLException e) {
            System.err.println("Failed to refresh tournaments: " + e.getMessage());
            showErrorDialog("Error Refreshing Tournaments",
                    "Failed to reload tournaments from database.");
        }
    }


    private void updateLiveCount() {
        long liveCount = matches.stream()
                .filter(m -> "live".equalsIgnoreCase(m.getStatus()))
                .count();
        System.out.println("Updated live count: " + liveCount);

        headerLabel.setText(String.format("    \u26BD   Live Matches (%d)      ", liveCount));

        // Remove previous animations if any
        headerLabel.getStyleClass().remove("live-label");
        headerLabel.getStyleClass().add("live-label");
        headerLabel.setStyle("-fx-text-fill: -mfx-purple;-fx-background-radius: 55px; -fx-background-color: whitesmoke; -fx-border-color: whitesmoke; -fx-border-width: 1px; -fx-border-radius: 55px; ");

//animated
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> headerLabel.setScaleX(1.0)),
                new KeyFrame(Duration.seconds(0), e -> headerLabel.setScaleY(1.0)),
                new KeyFrame(Duration.seconds(0.6), e -> headerLabel.setScaleX(0.9)),
                new KeyFrame(Duration.seconds(0.6), e -> headerLabel.setScaleY(0.9)),
                new KeyFrame(Duration.seconds(1), e -> headerLabel.setScaleX(1.0)),
                new KeyFrame(Duration.seconds(1), e -> headerLabel.setScaleY(1.0))
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    @FXML
    private void refreshTable() {
        System.out.println("MatchesController: Refreshing table");
        refreshTournaments();
        loadMatches();
    }




    private void showAlert(String title, String message) {
        Stage alertStage = new Stage();
        alertStage.initStyle(StageStyle.UNDECORATED);
        alertStage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1B1F3B; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        messageLabel.setWrapText(true);

        MFXButton okButton = new MFXButton("OK");
        okButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        okButton.setOnAction(e -> alertStage.close());

        root.getChildren().addAll(messageLabel, okButton);
        Scene scene = new Scene(root, 300, 450);
        alertStage.setScene(scene);
        alertStage.showAndWait();
    }


    public void deleteMatch(Matches match) {
        System.out.println("MatchesController: Deleting match with ID: " + match.getId());
        try {
            matchesService.delete(match);
            refreshTable();
        } catch (SQLException e) {
            System.err.println("MatchesController: Failed to delete match");
            e.printStackTrace();
            showErrorDialog("Error Deleting Match", "Failed to delete match from database.");
        }
    }

    private void showErrorDialog(String title, String content) {
        System.err.println("ERROR - " + title + ": " + content);
    }
    private void showMoreStats(Matches match) {
        // Generate random stats for the match
        Random random = new Random();
        int possessionTeamA = random.nextInt(101);
        int possessionTeamB = 100 - possessionTeamA;
        int shotsTeamA = random.nextInt(20) + 1;
        int shotsTeamB = random.nextInt(20) + 1;
        int foulsTeamA = random.nextInt(10) + 1;
        int foulsTeamB = random.nextInt(10) + 1;
    }

    }