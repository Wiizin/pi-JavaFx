package io.github.palexdev.materialfx.demo.controllers;

import com.google.gson.*;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.demo.model.MatchInfo;
import io.github.palexdev.materialfx.demo.model.Matches;
import io.github.palexdev.materialfx.demo.model.Tournois;
import io.github.palexdev.materialfx.demo.services.MatchStatusEnum;
import io.github.palexdev.materialfx.demo.services.MatchesService;
import io.github.palexdev.materialfx.demo.services.TournoisService;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static io.github.palexdev.materialfx.demo.controllers.PlayerFrontController.FALLBACK_LOGO_PATH;
import static io.github.palexdev.materialfx.demo.controllers.PlayerFrontController.LOGGER;

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

    // API key used for API-Football endpoints.
    private final String apiKey = "b193a3d817a74a92655cf7d01e597f83febe3e9288821dcea782e1fab797451e";
    private javafx.animation.Timeline autoRefresh;        // <― holds the timer
    private static final int REFRESH_SECONDS = 90;        // every 1½ min

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
            return;
        }
        setupTable();
        setupFilters();
        setupButtons();
        startAutoRefresh();
        loadDbMatches();   // Load matches from DB
        loadApiMatches();  // Load matches from API
        updateLiveCount();
        System.out.println("MatchesController: Initialization completed");
    }

    // ---------- Setup Filters ----------
    private void setupFilters() {
        try {
            tournaments.clear();
            tournaments.addAll(tournoisService.showAll());
            tournamentFilter.getItems().clear();
            tournamentFilter.getItems().add("All Tournaments");
            tournaments.forEach(t -> tournamentFilter.getItems().add(t.getNom()));
            tournamentFilter.selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorDialog("Error Loading Tournaments", "Failed to load tournaments from database.");
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
        if (filteredMatches == null) return;
        String searchText = searchField.getText().toLowerCase();
        String tournamentName = tournamentFilter.getValue();
        String status = statusFilter.getValue();
        filteredMatches.setPredicate(match -> {
            boolean matchesSearch = searchText.isEmpty()
                    || match.getTeamAName().toLowerCase().contains(searchText)
                    || match.getTeamBName().toLowerCase().contains(searchText);
            boolean matchesTournament = tournamentName.equals("All Tournaments") ||
                    tournaments.stream()
                            .filter(t -> t.getNom().equals(tournamentName))
                            .findFirst()
                            .map(t -> match.getIdTournoi() == t.getId())
                            .orElse(false);
            boolean matchesStatus = status.equals("All Matches")
                    || match.getStatus().equalsIgnoreCase(status);
            return matchesSearch && matchesTournament && matchesStatus;
        });
        System.out.println("Filtered matches count: " + filteredMatches.size());
    }

    // ---------- Setup Table ----------
    private void setupTable() {
        MFXTableColumn<Matches> tournoiColumn = new MFXTableColumn<>("Tournament", false);
        tournoiColumn.setPrefWidth(180);
        tournoiColumn.setRowCellFactory(match -> new MFXTableRowCell<>(m ->
                tournaments.stream()
                        .filter(t -> t.getId() == m.getIdTournoi())
                        .findFirst()
                        .map(Tournois::getNom)
                        .orElse("Unknown Tournament")
        ));

        MFXTableColumn<Matches> teamAColumn = new MFXTableColumn<>("Team A", false);
        teamAColumn.setPrefWidth(140);
        teamAColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getTeamAName));

        MFXTableColumn<Matches> scoreAColumn = new MFXTableColumn<>("Score A", false, Comparator.comparing(Matches::getScoreTeamA));
        scoreAColumn.setPrefWidth(80);
        scoreAColumn.setRowCellFactory(match -> {
            MFXTableRowCell<Matches, Integer> cell = new MFXTableRowCell<>(Matches::getScoreTeamA);
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        MFXTableColumn<Matches> scoreBColumn = new MFXTableColumn<>("Score B", false, Comparator.comparing(Matches::getScoreTeamB));
        scoreBColumn.setPrefWidth(80);
        scoreBColumn.setRowCellFactory(match -> {
            MFXTableRowCell<Matches, Integer> cell = new MFXTableRowCell<>(Matches::getScoreTeamB);
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        MFXTableColumn<Matches> teamBColumn = new MFXTableColumn<>("Team B", false);
        teamBColumn.setPrefWidth(140);
        teamBColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getTeamBName));

        MFXTableColumn<Matches> statusColumn = new MFXTableColumn<>("Status", false, Comparator.comparing(Matches::getStatus));
        statusColumn.setPrefWidth(100);
        statusColumn.setRowCellFactory(match -> {
            MFXTableRowCell<Matches, String> cell = new MFXTableRowCell<>(Matches::getStatus);
            cell.setAlignment(Pos.CENTER);
            cell.styleProperty().bind(Bindings.createStringBinding(() -> {
                String s = cell.getText();
                if (s == null) return "";
                switch (s.toLowerCase()) {
                    case "live": return "status-live";
                    case "upcoming": return "status-upcoming";
                    case "finished": return "status-finished";
                    default: return "";
                }
            }, cell.textProperty()));
            return cell;
        });

        MFXTableColumn<Matches> timeColumn = new MFXTableColumn<>("Match Time", false, Comparator.comparing(Matches::getMatchTime));
        timeColumn.setPrefWidth(140);
        timeColumn.setRowCellFactory(match -> new MFXTableRowCell<>(m ->
                m.getMatchTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ));

        MFXTableColumn<Matches> locationColumn = new MFXTableColumn<>("Location", false, Comparator.comparing(Matches::getLocationMatch));
        locationColumn.setPrefWidth(180);
        locationColumn.setRowCellFactory(match -> new MFXTableRowCell<>(Matches::getLocationMatch));

        // --- Details column (old MaterialFX builds) ----------------------------
        MFXTableColumn<Matches> detailsColumn = new MFXTableColumn<>("Details", false);
        detailsColumn.setPrefWidth(100);

        detailsColumn.setRowCellFactory(col -> new MFXTableRowCell<Matches, Void>(m -> null) {

            private final Button btn = new Button("View Details");
            private Matches rowData;   // cached reference

            {
                btn.setStyle("-fx-font-size:12px;-fx-background-color:#007bff;-fx-text-fill:white;");
                btn.setOnAction(e -> {
                    if (rowData != null) showMatchDetailsDialog(rowData);
                });
                setAlignment(Pos.CENTER);
                setGraphic(btn);
            }

            @Override
            public void update(Matches item) {
                super.update(item);     // keeps the extractor happy
                rowData = item;         // cache the *current* row object
                setGraphic(item == null ? null : btn);
            }
        });


        paginated.getTableColumns().addAll(
                tournoiColumn, teamAColumn,
                scoreAColumn, scoreBColumn,
                teamBColumn, statusColumn,
                timeColumn, locationColumn,
                detailsColumn
        );
        paginated.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
    }

    // ---------- Data Loading ----------
    private void loadDbMatches() {
        try {
            matches.clear();
            List<Matches> loadedMatches = matchesService.showAll();
            System.out.println("Loaded " + loadedMatches.size() + " matches from database");
            matches.addAll(loadedMatches);
            filteredMatches = new FilteredList<>(matches, p -> true);
            paginated.setItems(filteredMatches);
        } catch (SQLException e) {
            System.err.println("MatchesController: Failed to load matches");
            e.printStackTrace();
            showErrorDialog("Error Loading Matches", "Failed to load matches from database.");
        }
    }


    /* ─────────────────────────────
       NEW loadApiMatches() – shorter & uses the helper
       ───────────────────────────── */
    private void loadApiMatches() {

        String today  = java.time.LocalDate.now().toString();
        String url = "https://apiv3.apifootball.com/?action=get_events"
                + "&from=" + today + "&to=" + today
                + "&APIkey=" + apiKey;

        Task<List<MatchInfo>> task = new Task<>() {
            @Override protected List<MatchInfo> call() throws Exception {

                JsonArray arr = io.github.palexdev.materialfx.demo.net.ApiFootball.getAsArray(url);
                List<MatchInfo> out = new java.util.ArrayList<>();

                for (JsonElement el : arr) {
                    JsonObject o = el.getAsJsonObject();

                    String h = o.get("match_hometeam_name").getAsString();
                    String a = o.get("match_awayteam_name").getAsString();

                    out.add(new MatchInfo(
                            h, a,
                            o.get("match_date").getAsString() + "T" + o.get("match_time").getAsString(),
                            o.get("league_name").getAsString(),
                            o.has("match_stadium") ? o.get("match_stadium").getAsString() : "",
                            "LIVE".equalsIgnoreCase(o.get("match_status").getAsString()),
                            h.substring(0,3).toUpperCase(),
                            a.substring(0,3).toUpperCase(),
                            o.get("match_hometeam_id").getAsInt(),
                            o.get("match_awayteam_id").getAsInt(),
                            o.get("match_id").getAsString(),
                            safe(o, "match_hometeam_score"),
                            safe(o, "match_awayteam_score"),
                            o.get("match_status").getAsString()
                    ));
                }
                return out;
            }
        };

        task.setOnSucceeded(e -> {
            List<MatchInfo> api = task.getValue();
            List<Matches>   db  = api.stream().map(this::convertMatchInfoToMatches).toList();

            Platform.runLater(() -> {
                matches.removeIf(m -> m.getFixtureId() != null);   // drop previous API rows
                matches.addAll(db);
                filterMatches();
                updateLiveCount();
            });
        });

        task.setOnFailed(e ->
                showErrorDialog("API error", task.getException().getMessage()));

        new Thread(task, "api-matches").start();
    }

    /* helper that copes with “” empty string */
    private static int safe(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e != null && !e.isJsonNull() && !e.getAsString().isEmpty() ? e.getAsInt() : 0;
    }

    /* ─────────────────────────────
       OPTIONAL – auto-refresh every N seconds
       ───────────────────────────── */
    private void startAutoRefresh() {
        autoRefresh = new javafx.animation.Timeline(
                new KeyFrame(javafx.util.Duration.seconds(REFRESH_SECONDS),
                        e -> loadApiMatches()));
        autoRefresh.setCycleCount(javafx.animation.Animation.INDEFINITE);
        autoRefresh.play();
    }

    private Matches convertMatchInfoToMatches(MatchInfo info) {
        Matches m = new Matches();
        m.setTeamAName(info.getHomeTeam());
        m.setTeamBName(info.getAwayTeam());
        try {
            m.setMatchTime(LocalDateTime.parse(info.getTime()));
        } catch (Exception e) {
            m.setMatchTime(LocalDateTime.now());
        }
        m.setLocationMatch(info.getVenue());
        m.setStatus(info.getStatus());
        m.setScoreTeamA(info.getHomeScore());
        m.setScoreTeamB(info.getAwayScore());
        m.setId(new Random().nextInt(10000));
        m.setIdTournoi(
                tournaments.stream()
                        .filter(t -> t.getNom().equalsIgnoreCase(info.getLeague()))
                        .findFirst()
                        .map(Tournois::getId)
                        .orElse(-1)
        );
        m.setFixtureId(info.getFixtureId());
        return m;
    }

    // ---------- Show Match Details Popup ----------
    private void showMatchDetailsDialog(Matches match) {
        Set<Integer> topLeagueIds = Set.of(61, 152, 207, 4, 302, 3, 56);

        // Fixed: no duplicate key entries
        Map<String, Integer> leagueNameToApiId = Map.ofEntries(
                Map.entry("Premier League", 152),
                Map.entry("La Liga", 302),
                Map.entry("Bundesliga", 195),
                Map.entry("Serie A", 207),
                Map.entry("Ligue 1", 61),
                Map.entry("Eredivisie", 3),
                Map.entry("Primeira Liga", 56)
        );

        // Match DB league name to API league ID
        String dbLeagueName = tournaments.stream()
                .filter(t -> t.getId() == match.getIdTournoi())
                .map(Tournois::getNom)
                .findFirst()
                .orElse(null);

        if (dbLeagueName != null && leagueNameToApiId.containsKey(dbLeagueName)) {
            int apiLeagueId = leagueNameToApiId.get(dbLeagueName);
            if ((match.getTeamAId() == 0 || match.getTeamBId() == 0) && topLeagueIds.contains(apiLeagueId)) {
                //call function to fetch team IDs from API
                // call function to get fixture ID of that match
                // fetchMatchDetailsFromAPI(match.getFixtureId(), match);
                fetchMatchDetailsFromAPI(match.getFixtureId(), match);
            }
        }
        String borderColor;
        switch (match.getStatus().toLowerCase()) {
            case "live": borderColor = "#008000"; break;
            case "upcoming": borderColor = "#0000ff"; break;
            case "finished": borderColor = "#ff0000"; break;
            default: borderColor = "#ff9800"; break;
        }
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.TRANSPARENT);
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");
        HBox combinedContainer = new HBox(20);
        combinedContainer.setAlignment(Pos.CENTER);
        combinedContainer.setPadding(new Insets(25));
        combinedContainer.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; " +
                "-fx-border-color: " + borderColor + "; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 4);");

        // Draggable header
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
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), combinedContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(evt -> popupStage.close());
            fadeOut.play();
        });
        header.getChildren().addAll(titleLabel, spacer, closeButton);

        // Teams container with logos and team info
        HBox teamsContainer = new HBox(40);
        teamsContainer.setAlignment(Pos.CENTER);
        teamsContainer.setPadding(new Insets(20, 0, 20, 0));
        teamsContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        VBox teamABox = createTeamBox(match.getTeamAName(), match.getScoreTeamA(), match.getTeamAId());
        Label vsLabel = new Label("VS");
        vsLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        VBox teamBBox = createTeamBox(match.getTeamBName(), match.getScoreTeamB(), match.getTeamBId());
        teamsContainer.getChildren().addAll(teamABox, vsLabel, teamBBox);

        // Info container
        VBox infoContainer = new VBox(15);
        infoContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-padding: 20;");
        HBox statusBox = createInfoRow("Status", match.getStatus(),
                "live".equalsIgnoreCase(match.getStatus()) ? "#28a745" :
                        "finished".equalsIgnoreCase(match.getStatus()) ? "#dc3545" : "#6c757d");
        HBox timeBox = createInfoRow("Time", match.getMatchTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        HBox locationBox = createInfoRow("Location", match.getLocationMatch());
        HBox tournamentBox = createInfoRow("Tournament", tournaments.stream()
                .filter(t -> t.getId() == match.getIdTournoi())
                .findFirst()
                .map(Tournois::getNom)
                .orElse("Unknown"));
        infoContainer.getChildren().addAll(statusBox, timeBox, locationBox, tournamentBox);

        // Substitutes area: two columns for home and away substitutes, facing each other.
        HBox subsHBox = new HBox(30);
        subsHBox.setAlignment(Pos.TOP_CENTER);
        subsHBox.setPadding(new Insets(10));
        VBox homeSubsBox = new VBox(5);
        homeSubsBox.setAlignment(Pos.TOP_RIGHT); // Right-aligned for home subs
        Label homeTitle = new Label("Home Substitutes");
        homeTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        homeSubsBox.getChildren().add(homeTitle);
        VBox awaySubsBox = new VBox(5);
        awaySubsBox.setAlignment(Pos.TOP_LEFT); // Left-aligned for away subs
        Label awayTitle = new Label("Away Substitutes");
        awayTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        awaySubsBox.getChildren().add(awayTitle);
        subsHBox.getChildren().addAll(homeSubsBox, awaySubsBox);
        loadSubstitutes(match.getFixtureId(), homeSubsBox, awaySubsBox);

        VBox matchDetailsContainer = new VBox(20, header, teamsContainer, infoContainer, subsHBox);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/io/github/palexdev/materialfx/demo/fxml/FootballPitch.fxml"));
            Parent footballPitch = loader.load();

            /* -------- NEW: really load the line-up -------- */
            FootballPitchController fp = loader.getController();
            String idStr = match.getFixtureId();
            if (idStr != null && idStr.matches("\\d+")) {
                fp.loadLineupForMatch(Integer.parseInt(idStr));
            } else {
                System.out.println("No valid fixture id – lineup skipped");
            }
            /* ---------------------------------------------- */

            VBox pitchContainer = new VBox(footballPitch);
            pitchContainer.setStyle("-fx-background-color:#f8f9fa; -fx-background-radius:10;");
            pitchContainer.setPadding(new Insets(20));

            HBox finalContainer = new HBox(pitchContainer, matchDetailsContainer);
            finalContainer.setAlignment(Pos.CENTER);
            finalContainer.setSpacing(20);
            combinedContainer.getChildren().add(finalContainer);
            root.getChildren().add(combinedContainer);
            StackPane.setAlignment(combinedContainer, Pos.CENTER);

            Scene scene = new Scene(root, Color.TRANSPARENT);
            popupStage.setScene(scene);
            combinedContainer.setOpacity(0);
            combinedContainer.setTranslateY(-20);
            Timeline showTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(0.5),
                            new KeyValue(combinedContainer.opacityProperty(), 1),
                            new KeyValue(combinedContainer.translateYProperty(), 0)
                    )
            );
            showTimeline.play();
            popupStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error loading FootballPitch.fxml: " + ex.getMessage());
        }
    }
    private void searchAndFetchMatchDetailsFromAPI(Matches match) {
        String date = match.getMatchTime().toLocalDate().toString(); // e.g. 2025-04-12
        String fromDate = date; // optionally: LocalDate.minusDays(1)
        String toDate = date;

        String url = String.format(
                "https://apiv3.apifootball.com/?action=get_events&from=%s&to=%s&APIkey=%s",
                fromDate, toDate, apiKey
        );

        Task<Void> fetchTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JsonArray matchesArray = JsonParser.parseString(response.body()).getAsJsonArray();
                for (JsonElement el : matchesArray) {
                    JsonObject obj = el.getAsJsonObject();
                    String home = obj.get("match_hometeam_name").getAsString();
                    String away = obj.get("match_awayteam_name").getAsString();
                    String stadium = obj.has("match_stadium") ? obj.get("match_stadium").getAsString() : "";

                    if (home.equalsIgnoreCase(match.getTeamAName()) &&
                            away.equalsIgnoreCase(match.getTeamBName()) &&
                            (match.getLocationMatch() == null || stadium.equalsIgnoreCase(match.getLocationMatch()))) {

                        int fixtureId = obj.get("match_id").getAsInt();
                        int teamAId = obj.get("match_hometeam_id").getAsInt();
                        int teamBId = obj.get("match_awayteam_id").getAsInt();

                        match.setFixtureId(String.valueOf(fixtureId));
                        match.setTeamAId(teamAId);
                        match.setTeamBId(teamBId);

                        System.out.println("✅ Matched fixture found: " + fixtureId);
                        break;
                    }
                }

                return null;
            }
        };

        fetchTask.setOnFailed(e -> {
            System.err.println("❌ Failed to search for fixture ID for match: " + match.getTeamAName() + " vs " + match.getTeamBName());
        });

        new Thread(fetchTask).start();
    }

    private void fetchMatchDetailsFromAPI(String fixtureId, Matches match) {
        String url = String.format("https://apiv3.apifootball.com/?action=get_events&match_id=%s&APIkey=%s", fixtureId, apiKey);

        Task<Void> fetchTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
                if (!jsonArray.isEmpty()) {
                    JsonObject obj = jsonArray.get(0).getAsJsonObject();
                    if (obj.has("match_hometeam_id") && obj.has("match_awayteam_id")) {
                        match.setTeamAId(obj.get("match_hometeam_id").getAsInt());
                        match.setTeamBId(obj.get("match_awayteam_id").getAsInt());
                        System.out.println("Team IDs fetched from API for fixture: " + fixtureId);
                    }
                }
                return null;
            }
        };

        fetchTask.setOnFailed(e -> {
            System.err.println("Failed to fetch match info for fixture: " + fixtureId);
        });

        new Thread(fetchTask).start();
    }

    // ---------- Helper Methods ----------
    private void loadSubstitutes(String fixtureIdStr, VBox homeSubsBox, VBox awaySubsBox) {
        int fid;
        try { fid = Integer.parseInt(fixtureIdStr); }
        catch (NumberFormatException ex) {
            homeSubsBox.getChildren().add(new Label("Invalid fixture ID."));
            awaySubsBox.getChildren().add(new Label("Invalid fixture ID."));
            return;
        }

        String url = String.format(
                "https://apiv3.apifootball.com/?action=get_lineups&fixture_id=%d&APIkey=%s",
                fid, apiKey
        );

        Task<JsonArray> subsTask = new Task<>() {
            @Override
            protected JsonArray call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest  req    = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                return extractResponseArray(resp.body());

            }
        };

        subsTask.setOnSucceeded(ev -> {
            JsonArray respArr = subsTask.getValue();
            if (respArr.size() < 2) {
                homeSubsBox.getChildren().add(new Label("No substitutes available."));
                awaySubsBox.getChildren().add(new Label("No substitutes available."));
                return;
            }
            for (JsonElement el : respArr.get(0).getAsJsonObject().getAsJsonArray("substitutes")) {
                String name = el.getAsJsonObject()
                        .getAsJsonObject("player")
                        .get("name").getAsString();
                homeSubsBox.getChildren().add(new Label("• " + name));
            }
            for (JsonElement el : respArr.get(1).getAsJsonObject().getAsJsonArray("substitutes")) {
                String name = el.getAsJsonObject()
                        .getAsJsonObject("player")
                        .get("name").getAsString();
                awaySubsBox.getChildren().add(new Label("• " + name));
            }
        });
        subsTask.setOnFailed(ev -> {
            String msg = subsTask.getException().getMessage();
            homeSubsBox.getChildren().add(new Label("Failed to load subs: " + msg));
            awaySubsBox.getChildren().add(new Label("Failed to load subs: " + msg));
        });

        new Thread(subsTask).start();
    }
    private static class Delta {
        double x, y;
    }

    private void addHoverEffect(MFXButton button, String hoverColor, String normalColor) {
        button.setOnMouseEntered(e -> {
            String currentStyle = button.getStyle();
            button.setStyle(currentStyle.replace(normalColor, hoverColor));
        });
        button.setOnMouseExited(e -> {
            String currentStyle = button.getStyle();
            button.setStyle(currentStyle.replace(hoverColor, normalColor));
        });
    }

    // Updated createTeamBox that includes team logo; logos are retrieved in createTeamLogo.
    private VBox createTeamBox(String teamName, int score, int teamId) {
        VBox teamBox = new VBox(10);
        teamBox.setAlignment(Pos.CENTER);
        teamBox.setPadding(new Insets(15));
        teamBox.setMinWidth(200);
        ImageView logo = createTeamLogo(teamId, true); // true for featured size
        Label nameLabel = new Label(teamName);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1B1B3B;");
        nameLabel.setWrapText(true);
        Label scoreLabel = new Label(String.valueOf(score));
        scoreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
        teamBox.getChildren().addAll(logo, nameLabel, scoreLabel);
        return teamBox;
    }

    // Helper method to create team logo from API-Sports using teamId.
    /** Delegates badge rendering to PlayerFrontController so we use one cache & one URL. */
    /** Delegates badge rendering to PlayerFrontController if it exists, otherwise uses fallback. */
    private ImageView createTeamLogo(int teamId, boolean featured) {
        PlayerFrontController pfc = PlayerFrontController.getInstance();
        if (pfc != null) {                       // ✅ PlayerFront already loaded
            return pfc.logo(teamId, featured);
        }

        // 🔙 fallback when PlayerFront not available
        int size = featured ? 40 : 20;
        ImageView iv = new ImageView(
                new Image("https://media.api-sports.io/football/teams/" + teamId + ".png", true));
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setClip(new Circle(size/2.0, size/2.0, size/2.0));
        iv.imageProperty().addListener((obs, oldV, newV) -> {
            if (newV.isError()) {
                iv.setImage(new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream(FALLBACK_LOGO_PATH))));
            }
        });
        return iv;
    }



    // Helper method to create an info row
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
        refreshButton.setOnAction(event -> refreshTable());
    }
    @FXML
    void handleAddMatch() {
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
    private void refreshTournaments() {
        try {
            String currentSelection = tournamentFilter.getValue();
            tournaments.clear();
            tournaments.addAll(tournoisService.showAll());
            tournamentFilter.getItems().clear();
            tournamentFilter.getItems().add("All Tournaments");
            tournaments.forEach(t -> tournamentFilter.getItems().add(t.getNom()));
            if (tournamentFilter.getItems().contains(currentSelection)) {
                tournamentFilter.setValue(currentSelection);
            } else {
                tournamentFilter.selectFirst();
            }
        } catch (SQLException e) {
            System.err.println("Failed to refresh tournaments: " + e.getMessage());
            showErrorDialog("Error Refreshing Tournaments", "Failed to reload tournaments from database.");
        }
    }

    @FXML
    private void refreshTable() {
        System.out.println("MatchesController: Refreshing table");
        refreshTournaments();
        loadDbMatches();
        loadApiMatches();
    }

    private void updateLiveCount() {
        long liveCount = matches.stream()
                .filter(m -> "live".equalsIgnoreCase(m.getStatus()))
                .count();
        System.out.println("Updated live count: " + liveCount);
        headerLabel.setText(String.format("    \u26BD   Live Matches (%d)      ", liveCount));
        headerLabel.getStyleClass().remove("live-label");
        headerLabel.getStyleClass().add("live-label");
        headerLabel.setStyle("-fx-text-fill: -mfx-purple; -fx-background-radius: 55px; -fx-background-color: whitesmoke; -fx-border-color: whitesmoke; -fx-border-width: 1px; -fx-border-radius: 55px;");
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

    private void showErrorDialog(String title, String content) {
        System.err.println("ERROR - " + title + ": " + content);
    }
    /**
     * The API sometimes returns:
     *   { "response":[ … ] }         // old
     * and sometimes:
     *   { "<fixtureId>":{ "lineup":{ … } } }   // new
     * This method extracts the array part regardless of the variant.
     */
    private static JsonArray extractResponseArray(String body) {
        JsonElement root = JsonParser.parseString(body);

        // old format ──────────────────────────────
        if (root.isJsonObject() && root.getAsJsonObject().has("response")) {
            return root.getAsJsonObject().getAsJsonArray("response");
        }

        // new format ──────────────────────────────
        if (root.isJsonObject()) {
            // take the first property value (they key it by fixture-id)
            JsonObject firstValue = root.getAsJsonObject()
                    .entrySet().iterator().next()   // safe: object not empty
                    .getValue().getAsJsonObject();

            // inside:  { "lineup":{ "home":…, "away":… } }
            if (firstValue.has("lineup")) {
                // you decide which array you need
                // home starting XI → starting_lineups
                JsonObject lineup = firstValue.getAsJsonObject("lineup");
                // hand the whole object to the caller, they can pick "home"/"away"
                JsonArray arr = new JsonArray();
                arr.add(lineup);
                return arr;
            }
        }

        // totally unexpected payload
        return null;
    }

}
