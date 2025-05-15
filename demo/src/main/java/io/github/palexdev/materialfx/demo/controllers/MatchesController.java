package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.demo.model.Matches;
import io.github.palexdev.materialfx.demo.model.Tournois;
import io.github.palexdev.materialfx.demo.services.MatchesService;
import io.github.palexdev.materialfx.demo.services.TournoisService;
import io.github.palexdev.materialfx.filter.StringFilter;
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

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;
import io.github.palexdev.materialfx.demo.services.MatchStatusEnum;
import javafx.scene.layout.HBox;

import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Insets;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class MatchesController implements Initializable {

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
    public MatchesController() {
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
            // Create a cell with no text, only a graphic node
            MFXTableRowCell<Matches, String> cell = new MFXTableRowCell<>(m -> "");

            // Create an HBox to hold both buttons with a spacing of 5 pixels
            HBox buttonContainer = new HBox(10);
            buttonContainer.setPadding(new Insets(2));  // Add some padding
            buttonContainer.setAlignment(Pos.CENTER);

            // Create the Update button with inline style
            MFXButton updateButton = new MFXButton("Update");
            updateButton.getStyleClass().add("Add-button");
            updateButton.setOnAction(e -> {
                // Call your update method for this match
                updateMatch(match);
            });

            // Create the Delete button with inline style
            MFXButton deleteButton = new MFXButton("Delete");
            deleteButton.getStyleClass().add("Delete-button");
            deleteButton.setOnAction(e -> {
                // Optionally add confirmation here before deleting zidhaaa
                deleteMatch(match);
            });

            // Add both buttons to the HBox container
            buttonContainer.getChildren().addAll(updateButton, deleteButton);
            cell.setGraphic(buttonContainer);

            return cell;
        });


        paginated.getTableColumns().addAll(
                tournoiColumn,  teamAColumn,
                scoreAColumn,
                scoreBColumn,
                teamBColumn,
                statusColumn,
                timeColumn, locationColumn,  actionColumn
        );

        paginated.getTableColumns().forEach(column -> {
            column.setMinWidth(100);
            System.out.println("Configured column: " + column.getText());
        });

        paginated.getFilters().addAll(
                new StringFilter<>("Status", Matches::getStatus),
                new StringFilter<>("Location", Matches::getLocationMatch)
        );
        paginated.setTableRowFactory(match -> {
            MFXTableRow<Matches> row = new MFXTableRow<>(paginated, match);
            row.setMinHeight(50);  // Adjust row height
            row.setPrefHeight(50);
            row.setMaxHeight(50);
            return row;
        });

        paginated.setCurrentPage(1);
        paginated.setRowsPerPage(10);

        paginated.setTableRowFactory(match -> {
            MFXTableRow<Matches> row = new MFXTableRow<>(paginated, match);
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    System.out.println("Double-clicked on match with ID: " + match.getId());
                    updateMatch(match);
                }
            });
            return row;
        });
    }



    private void setupButtons() {
        System.out.println("MatchesController: Setting up buttons");
        addButton.setOnAction(event -> handleAddMatch());
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

    private void handleAddMatch() {
        System.out.println("MatchesController: Add Match button clicked");

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.UNDECORATED);

        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-border-color: #ff9800; -fx-border-width: 2;");

        String textFieldStyle = "-fx-text-fill: #1B1B3B; -fx-prompt-text-fill: #1B1B3B; -fx-background-color: transparent; -fx-border-color: #2A2F4FFF;";

        Label titleLabel = new Label("Add New Match");
        titleLabel.setStyle("-fx-text-fill: #1B1B3B; -fx-font-size: 18px;");

        // Team A
        MFXTextField teamAField = new MFXTextField();
        teamAField.setFloatingText("Team A");
        teamAField.setStyle(textFieldStyle);
        teamAField.setPrefWidth(250);

        // Team B
        MFXTextField teamBField = new MFXTextField();
        teamBField.setFloatingText("Team B");
        teamBField.setStyle(textFieldStyle);
        teamBField.setPrefWidth(250);

        // Scores
        MFXTextField scoreAField = new MFXTextField("0");
        scoreAField.setFloatingText("Score Team A");
        scoreAField.setStyle(textFieldStyle);
        scoreAField.setPrefWidth(250);

        MFXTextField scoreBField = new MFXTextField("0");
        scoreBField.setFloatingText("Score Team B");
        scoreBField.setStyle(textFieldStyle);
        scoreBField.setPrefWidth(250);

        // Status Combo
        MFXComboBox<String> statusCombo = new MFXComboBox<>();
        statusCombo.setFloatingText("Status");
        statusCombo.getItems().addAll(
                Arrays.stream(MatchStatusEnum.values())
                        .map(MatchStatusEnum::getDisplayValue)
                        .collect(Collectors.toList())
        );
        statusCombo.setValue(MatchStatusEnum.UPCOMING.getDisplayValue());
        statusCombo.setStyle(textFieldStyle);
        statusCombo.setPrefWidth(250);

        // Date and Time
        MFXDatePicker datePicker = new MFXDatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.setFloatingText("Match Date");
        datePicker.setStyle(textFieldStyle);
        datePicker.setPrefWidth(250);

        MFXTextField timeField = new MFXTextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeField.setFloatingText("Match Time (HH:mm)");
        timeField.setStyle(textFieldStyle);
        timeField.setPrefWidth(250);

        // Location
        MFXTextField locationField = new MFXTextField();
        locationField.setFloatingText("Location");
        locationField.setStyle(textFieldStyle);
        locationField.setPrefWidth(250);

        // Tournament Combo
        MFXComboBox<String> tournamentCombo = new MFXComboBox<>();
        tournamentCombo.setFloatingText("Tournament");
        tournamentCombo.getItems().addAll(
                tournaments.stream()
                        .map(Tournois::getNom)
                        .collect(Collectors.toList())
        );
        if (!tournaments.isEmpty()) {
            tournamentCombo.setValue(tournaments.get(0).getNom());
        }
        tournamentCombo.setStyle(textFieldStyle);
        tournamentCombo.setPrefWidth(250);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        MFXButton saveButton = new MFXButton("Save");
        saveButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        saveButton.setPrefWidth(100);

        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        cancelButton.setPrefWidth(100);

        buttonBox.getChildren().addAll(saveButton, cancelButton);

        form.getChildren().addAll(
                titleLabel,
                teamAField,
                teamBField,
                scoreAField,
                scoreBField,
                statusCombo,
                datePicker,
                timeField,
                locationField,
                tournamentCombo,
                buttonBox
        );

        // Save Handler
        saveButton.setOnAction(e -> {
            try {
                // Validate inputs
                if (teamAField.getText().isEmpty() || teamBField.getText().isEmpty() ||
                        locationField.getText().isEmpty() || datePicker.getValue() == null ||
                        timeField.getText().isEmpty() || tournamentCombo.getValue() == null) {
                    showAlert("Missing Information", "Please fill in all required fields.");
                    return;
                }

                // Create new match object
                Matches newMatch = new Matches();
                newMatch.setTeamAName(teamAField.getText());
                newMatch.setTeamBName(teamBField.getText());
                newMatch.setScoreTeamA(Integer.parseInt(scoreAField.getText()));
                newMatch.setScoreTeamB(Integer.parseInt(scoreBField.getText()));
                newMatch.setStatus(statusCombo.getValue());

                LocalDate date = datePicker.getValue();
                LocalTime time = LocalTime.parse(timeField.getText());
                newMatch.setMatchTime(LocalDateTime.of(date, time));

                newMatch.setLocationMatch(locationField.getText());

                // Get selected tournament ID
                String selectedTournament = tournamentCombo.getValue();
                int tournamentId = tournaments.stream()
                        .filter(t -> t.getNom().equals(selectedTournament))
                        .findFirst()
                        .map(Tournois::getId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid tournament"));
                newMatch.setIdTournoi((int) tournamentId);

                // Add to database
                matchesService.insert(newMatch);
                refreshTable();
                popupStage.close();
            } catch (DateTimeParseException ex) {
                showAlert("Invalid Time Format", "Please enter time in HH:mm format (e.g., 14:30)");
            } catch (NumberFormatException ex) {
                showAlert("Invalid Number", "Please enter valid numbers for scores");
            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to add match: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                showAlert("Invalid Tournament", ex.getMessage());
            }
        });

        cancelButton.setOnAction(e -> popupStage.close());

        Scene scene = new Scene(form ,340,600);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    @FXML
    private void refreshTable() {
        System.out.println("MatchesController: Refreshing table");
        refreshTournaments();
        loadMatches();
    }


    private void showUpdateMatchDialog(Matches match) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.UNDECORATED);

        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-border-color: #ff9800; -fx-border-width: 2;");

        String textFieldStyle = "-fx-text-fill: #1B1B3B; -fx-prompt-text-fill: #1B1B3B; -fx-background-color: transparent; -fx-border-color: #2A2F4FFF;";

        Label titleLabel = new Label("Update Match");
        titleLabel.setStyle("-fx-text-fill: #1B1B3B; -fx-font-size: 18px;");

        // Team A
        MFXTextField teamAField = new MFXTextField(match.getTeamAName());
        teamAField.setFloatingText("Team A");
        teamAField.setStyle(textFieldStyle);
        teamAField.setPrefWidth(300);

        // Team B
        MFXTextField teamBField = new MFXTextField(match.getTeamBName());
        teamBField.setFloatingText("Team B");
        teamBField.setStyle(textFieldStyle);
        teamBField.setPrefWidth(300);

        // Scores
        MFXTextField scoreAField = new MFXTextField(String.valueOf(match.getScoreTeamA()));
        scoreAField.setFloatingText("Score Team A");
        scoreAField.setStyle(textFieldStyle);
        scoreAField.setPrefWidth(300);

        MFXTextField scoreBField = new MFXTextField(String.valueOf(match.getScoreTeamB()));
        scoreBField.setFloatingText("Score Team B");
        scoreBField.setStyle(textFieldStyle);
        scoreBField.setPrefWidth(300);

        // Status Combo
        MFXComboBox<String> statusCombo = new MFXComboBox<>();
        statusCombo.setFloatingText("Status");
        statusCombo.getItems().addAll(
                Arrays.stream(MatchStatusEnum.values())
                        .map(MatchStatusEnum::getDisplayValue)
                        .collect(Collectors.toList())
        );
        statusCombo.setValue(match.getStatus());
        statusCombo.setStyle(textFieldStyle);
        statusCombo.setPrefWidth(300);

        // Date and Time
        MFXDatePicker datePicker = new MFXDatePicker();
        datePicker.setValue(match.getMatchTime().toLocalDate());
        datePicker.setFloatingText("Match Date");
        datePicker.setStyle(textFieldStyle);
        datePicker.setPrefWidth(300);

// Time Field (Text input for time)
        MFXTextField timeField = new MFXTextField(match.getMatchTime().toLocalTime().toString());
        timeField.setFloatingText("Match Time (HH:mm)");
        timeField.setStyle(textFieldStyle);
        timeField.setPrefWidth(300);


        // Location
        MFXTextField locationField = new MFXTextField(match.getLocationMatch());
        locationField.setFloatingText("Location");
        locationField.setStyle(textFieldStyle);
        locationField.setPrefWidth(300);

        // Tournament Combo
        MFXComboBox<String> tournamentCombo = new MFXComboBox<>();
        tournamentCombo.setFloatingText("Tournament");
        tournamentCombo.getItems().addAll(
                tournaments.stream()
                        .map(Tournois::getNom)
                        .collect(Collectors.toList())
        );
        String currentTournament = tournaments.stream()
                .filter(t -> t.getId() == match.getIdTournoi())
                .findFirst()
                .map(Tournois::getNom)
                .orElse("");
        tournamentCombo.setValue(currentTournament);
        tournamentCombo.setStyle(textFieldStyle);
        tournamentCombo.setPrefWidth(300);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        MFXButton saveButton = new MFXButton("Save");
        saveButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        saveButton.setPrefWidth(100);

        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
        cancelButton.setPrefWidth(100);

        buttonBox.getChildren().addAll(saveButton, cancelButton);

        form.getChildren().addAll(
                titleLabel,
                teamAField,
                teamBField,
                scoreAField,
                scoreBField,
                statusCombo,
                datePicker,
                timeField,
                locationField,
                tournamentCombo,
                buttonBox
        );

        // Save Handler
        saveButton.setOnAction(e -> {
            try {
                // Update match object
                match.setTeamAName(teamAField.getText());
                match.setTeamBName(teamBField.getText());
                match.setScoreTeamA(Integer.parseInt(scoreAField.getText()));
                match.setScoreTeamB(Integer.parseInt(scoreBField.getText()));
                match.setStatus(statusCombo.getValue());

                LocalDate date = datePicker.getValue();
                LocalTime time = LocalTime.parse(timeField.getText());
                match.setMatchTime(LocalDateTime.of(date, time));

                match.setLocationMatch(locationField.getText());

                // Get selected tournament ID
                String selectedTournament = tournamentCombo.getValue();
                int tournamentId = tournaments.stream()
                        .filter(t -> t.getNom().equals(selectedTournament))
                        .findFirst()
                        .map(Tournois::getId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid tournament"));
                match.setIdTournoi((int) tournamentId);

                // Update in database
                matchesService.update(match);
                refreshTable();
                popupStage.close();
            } catch (DateTimeParseException ex) {
                showAlert("Invalid Time Format", "Please enter time in HH:mm format (e.g., 14:30)");
            } catch (NumberFormatException ex) {
                showAlert("Invalid Number", "Please enter valid numbers for scores");
            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to update match: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                showAlert("Invalid Tournament", ex.getMessage());
            }
        });

        cancelButton.setOnAction(e -> popupStage.close());

        Scene scene = new Scene(form, 340, 600);
        popupStage.setScene(scene);
        popupStage.showAndWait();
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
    public void updateMatch(Matches match) {
        showUpdateMatchDialog(match);
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
}