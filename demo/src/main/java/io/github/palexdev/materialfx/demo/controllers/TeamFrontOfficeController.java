package io.github.palexdev.materialfx.demo.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader;
import io.github.palexdev.materialfx.demo.model.*;
import io.github.palexdev.materialfx.demo.services.MatchesService;
import io.github.palexdev.materialfx.demo.services.TeamService;
import io.github.palexdev.materialfx.demo.services.TournoisService;
import io.github.palexdev.materialfx.demo.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class TeamFrontOfficeController implements Initializable {

    @FXML
    private GridPane grid;

    @FXML
    private Label featuredLeagueLabel;

    @FXML
    private ImageView featuredTeamALogo;

    @FXML
    private ImageView featuredTeamBLogo;

    @FXML
    private HBox featuredMatchTime;

    @FXML
    private HBox liveMatchesContainer;

    @FXML
    private HBox favouritePlayersContainer;

    @FXML
    private VBox upcomingMatchesContainer;
    private int currentMatchIndex = 0;
    private static final int MATCHES_PER_PAGE = 3;
    private TeamService teamService;
    private MatchesService matchesService;
    private User currentManager;
    private TournoisService tournoisService;
    private UserService userService;
    private  Team team = null;
    private Tournois tournois = null;
    private List<Matches> upcomingMatches;
    private List<Matches> liveMatches;
    private int currentLiveMatchIndex = 0;
    private static final String API_KEY = "479f93535e8f7b487ac4d5b41e8783bfcd8312bfc8791783a91c031cdbef96f3"; // Replace with your API key
    private static final String API_HOST = "apiv3.apifootball.com";
    int season = 2024; // Example: Season year
    LocalDate currentDate = LocalDate.now();
    // Define the desired date format
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate dateAfter7Days = currentDate.plusDays(7);
    // Format the current date as a string
    String date = currentDate.format(formatter);; // Start date
    String date2 = dateAfter7Days.format(formatter);
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentManager = UserSession.getInstance().getCurrentUser();
        teamService = new TeamService();
        matchesService=new MatchesService();
        tournoisService=new TournoisService();
        userService=new UserService();
        try {
            team = teamService.GetTeamById(currentManager.getIdteam());
            if (team == null) {
                System.err.println("No team found for the current manager.");
                return; // Exit initialization if no team is found
            }
            System.out.println(team.toString());

           } catch (SQLException e) {
            System.err.println("Error fetching team or tournament: " + e.getMessage());
            throw new RuntimeException("Failed to fetch team or tournament", e);
        }
        try {
            tournois = tournoisService.GetTournoisById(team.getIdtournoi());
            if (tournois == null) {
                System.err.println("No tournament found for the team.");
                return; // Exit initialization if no tournament is found
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tournament: " + e.getMessage());
            throw new RuntimeException("Failed to fetch tournament", e);
        }
        try {
            Tournois tournois=tournoisService.GetTournoisById(team.getIdtournoi());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            loadFixtureMatch(team.getNom(),tournois.getNom(),team.getId(),team.getIdtournoi());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        loadFeaturedMatch(0);
        loadLiveMatches();
        loadFavouritePlayers();
        loadUpcomingMatches();
        initializeUI();
    }
    private void loadFixtureMatch(String teamName, String leagueNAME, int idTeam, int idtournoi) throws SQLException {
        MatchesService matchesService = new MatchesService();
        Matches match1 = matchesService.getThelastMatchByTournoi(idtournoi);
        if (match1 != null && match1.getMatchTime().isBefore(currentDate.atStartOfDay())) {
            // Construct the API URL for fetching fixtures
            String apiUrl = "https://" + API_HOST + "/?action=get_events&from=" + date + "&to=" + date2 +
                    "&league_name=" + URLEncoder.encode(leagueNAME, StandardCharsets.UTF_8) +
                    "&APIkey=" + API_KEY;
            //System.out.println("API URL: " + apiUrl); // Debugging: Print the API URL

            try {
                // Make API request
                HttpResponse<String> response = makeApiRequest(apiUrl);

                if (response.statusCode() == 200) {
                    // Parse JSON response
                    JsonArray matchesArray = JsonParser.parseString(response.body()).getAsJsonArray();

                    // Initialize the service for creating matches
                   // MatchesService matchesService = new MatchesService();

                    // Iterate through the matches array
                    for (JsonElement matchElement : matchesArray) {
                        JsonObject matchObject = matchElement.getAsJsonObject();

                        // Extract match details
                        String homeTeamName = matchObject.get("match_hometeam_name").getAsString();
                        int homeTeamId = matchObject.get("match_hometeam_id").getAsInt();
                        String awayTeamName = matchObject.get("match_awayteam_name").getAsString();
                        int awayTeamId = matchObject.get("match_awayteam_id").getAsInt();
                        String status = matchObject.get("match_status").getAsString();
                        String location_match = matchObject.get("match_stadium").getAsString();
                        String homeTeamlogo=matchObject.get("team_home_badge").getAsString();
                        String awayTeamlogo=matchObject.get("team_away_badge").getAsString();
                        // Parse match date and time
                        LocalDateTime date = null;
                        String matchDate = matchObject.get("match_date").getAsString();
                        String matchTime = matchObject.get("match_time").getAsString();
                        String dateTimeString = matchDate + "T" + matchTime + ":00"; // Combine date and time
                        try {
                            date = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        } catch (Exception e) {
                            System.err.println("Failed to parse date: " + dateTimeString);
                            e.printStackTrace();
                        }

                        // Ensure teams exist in the database

                        int homeTeamDbId = ensureTeamExists(homeTeamName, homeTeamId, teamService, idtournoi,homeTeamlogo);
                        int awayTeamDbId = ensureTeamExists(awayTeamName, awayTeamId, teamService, idtournoi,awayTeamlogo);

                        // Filter out matches involving the included team
                        if (homeTeamName == teamName || awayTeamName == teamName) {
                            // Create a new Match object
                            Matches match = new Matches();
                            match.setTeamAName(homeTeamName);
                            match.setTeamBName(awayTeamName);
                            match.setMatchTime(date);
                            match.setStatus(status);
                            match.setTeamAId(homeTeamDbId);
                            match.setTeamBId(awayTeamDbId);
                            match.setLocationMatch(location_match);
                            if (homeTeamName == teamName) {
                                match.setTeamAId(idTeam);
                            } else {
                                match.setTeamBId(idTeam);
                            }
                            match.setIdTournoi(idtournoi);

                            try {
                                // Insert the match into the database
                                matchesService.insert2(match);
                            } catch (RuntimeException e) {
                                // Log the error and continue with the next match
                                System.err.println("Failed to insert match: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }else{

                            // Create a new Match object
                            Matches match = new Matches();
                            match.setTeamAName(homeTeamName);
                            match.setTeamBName(awayTeamName);
                            match.setTeamAId(homeTeamDbId);
                            match.setTeamBId(awayTeamDbId);
                            match.setMatchTime(date);
                            match.setIdTournoi(idtournoi);
                            match.setStatus(status);
                            match.setLocationMatch(location_match);

                            // Insert the match into the database
                            matchesService.insert2(match);
                        }
                    }

                    System.out.println("Matches added to SQL successfully for team: " + teamName);
                } else {
                    // Log failure with status code and response body
                    System.err.println("Failed to fetch matches. Status code: " + response.statusCode() + ", Response: " + response.body());
                }
            } catch (Exception e) {
                // Log any exceptions that occur during the API request or database operations
                System.err.println("Error fetching or adding matches: " + e.getMessage());
                e.printStackTrace();
            }
        }

    }
    private int ensureTeamExists(String teamName, int teamApiId, TeamService teamService,int idtournoi,String TeamNamelogo) {
        try {
            // Check if the team exists in the database
            Team team = teamService.GetTeamByName(teamName);
            if (team != null) {
                return team.getId(); // Return the existing team's ID
            } else {
                // Create a new team if it doesn't exist
                Team newTeam = new Team();
                newTeam.setNom(teamName);
                newTeam.setCategorie("Football"); // Default category
                newTeam.setModeJeu(ModeJeu.EN_GROUPE); // Default game mode
                newTeam.setIdtournoi(idtournoi); // Default tournament ID (update as needed)
                try {
                    // Download the photo to a temporary file
                    Path tempFilePath = Files.createTempFile("player_photo_", ".jpg");
                    downloadPhoto(TeamNamelogo, tempFilePath);

                    // Save the downloaded photo using the saveUploadedFile method
                    File tempFile = tempFilePath.toFile();
                    String savedFilePath = saveUploadedFile(tempFile,"team");

                    if (savedFilePath != null) {
                        System.out.println("Photo saved: " + savedFilePath);
                        newTeam.setLogoPath(savedFilePath); // Set the photo path in the Player object
                    } else {
                        System.err.println("Failed to save photo for team: " + teamName);
                    }

                    // Delete the temporary file
                    Files.deleteIfExists(tempFilePath);
                } catch (IOException e) {
                    System.err.println("Failed to download or save photo: " + e.getMessage());
                }

                // Insert the new team into the database
                int teamId = teamService.insert2(newTeam);
                System.out.println("New team added to database: " + teamName + " (ID: " + teamId + ")");
                return teamId;
            }
        } catch (SQLException e) {
            System.err.println("Error ensuring team exists: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to ensure team exists: " + teamName, e);
        }
    }
    private void downloadPhoto(String photoUrl, Path destinationPath) throws IOException {
        URL url = new URL(photoUrl);
        try (InputStream in = url.openStream()) {
            Files.copy(in, destinationPath, StandardCopyOption.REPLACE_EXISTING); // Save the file
        }
    }
    private static HttpResponse<String> makeApiRequest(String apiUrl) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    private String saveUploadedFile(File file, String type) {
        String subDirectory;
        if ("player".equals(type)) {
            subDirectory = "players/";
        } else if ("team".equals(type)) {
            subDirectory = "teams/";  // Fixed the directory name from "players" to "teams"
        } else {
            throw new IllegalArgumentException("Invalid type specified");
        }

        // Local file system path
        String uploadDir = "C:/xampp/htdocs/img/" + subDirectory;
        File dir = new File(uploadDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getName();
        File destFile = new File(uploadDir + fileName);

        try {
            Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return subDirectory + fileName;  // Returns "players/filename.jpg" or "teams/filename.jpg"
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void loadFeaturedMatch(int index) {
        if (team == null || tournois == null) {
            System.err.println("Team or tournament data is missing.");
            featuredLeagueLabel.setText("No featured match available.");
            return;
        }

        List<Matches> featuredMatches;
        try {
            System.out.println("team tournoi id:"+team.getIdtournoi());
            featuredMatches = matchesService.getMatchesByTournament(team.getIdtournoi());
        } catch (SQLException e) {
            System.err.println("Error fetching matches: " + e.getMessage());
            featuredLeagueLabel.setText("Error loading matches.");
            return;
        }

        if (featuredMatches == null || featuredMatches.isEmpty()) {
            System.err.println("No featured matches found.");
            featuredLeagueLabel.setText("No featured match available.");
            return;
        }

        Matches featuredMatch = featuredMatches.get(index);
        featuredLeagueLabel.setText(tournois.getNom());

        try {
            Team teamA = teamService.GetTeamByName(featuredMatch.getTeamAName());
            Team teamB = teamService.GetTeamByName(featuredMatch.getTeamBName());

            // Set team logos with null checks
            if (teamA != null && teamA.getLogoPath() != null) {
                String logoPath = teamA.getLogoPath().trim();
                if (!logoPath.isEmpty()) {
                    // Extract filename from path and construct absolute path
                    String fileName = logoPath.replace("\\", "/").substring(logoPath.lastIndexOf("/") + 1);
                    String absolutePath = "C:/xampp/htdocs/img/teams/" + fileName;
                    File logoFile = new File(absolutePath);

                    if (logoFile.exists() && logoFile.isFile()) {
                        try {
                            Image logoImage = new Image(logoFile.toURI().toString());
                            featuredTeamALogo.setImage(logoImage);
                        } catch (Exception e) {
                            System.err.println("Failed to load logo for " + teamA.getNom() + ": " + e.getMessage());
                            featuredTeamALogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
                        }
                    } else {
                        System.err.println("Logo file not found: " + absolutePath);
                        featuredTeamALogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
                    }
                } else {
                    System.err.println("Empty logo path for team: " + teamA.getNom());
                    featuredTeamALogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
                }
            } else {
                featuredTeamALogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
            }

            // Team B
            if (teamB != null && teamB.getLogoPath() != null) {
                String logoPath = teamB.getLogoPath().trim();
                if (!logoPath.isEmpty()) {
                    // Extract filename from path and construct absolute path
                    String fileName = logoPath.replace("\\", "/").substring(logoPath.lastIndexOf("/") + 1);
                    String absolutePath = "C:/xampp/htdocs/img/teams/" + fileName;
                    File logoFile = new File(absolutePath);

                    if (logoFile.exists() && logoFile.isFile()) {
                        try {
                            Image logoImage = new Image(logoFile.toURI().toString());
                            featuredTeamBLogo.setImage(logoImage);
                        } catch (Exception e) {
                            System.err.println("Failed to load logo for " + teamB.getNom() + ": " + e.getMessage());
                            featuredTeamBLogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
                        }
                    } else {
                        System.err.println("Logo file not found: " + absolutePath);
                        featuredTeamBLogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
                    }
                } else {
                    System.err.println("Empty logo path for team: " + teamB.getNom());
                    featuredTeamBLogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
                }
            } else {
                featuredTeamBLogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching team details: " + e.getMessage());
            featuredTeamALogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
            featuredTeamBLogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
        }
// Define formatters for date and time
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy"); // Example: 25 Oct 2023
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm"); // Example: 14:30

        // Format the date and time
        String formattedDate = featuredMatch.getMatchTime().format(dateFormatter);
        String formattedTime = featuredMatch.getMatchTime().format(timeFormatter);

        // Create labels for date and time
        Label matchDate = new Label(formattedDate);
        Label matchTime = new Label(formattedTime);
        matchDate.getStyleClass().add("match-time");
        matchTime.getStyleClass().add("match-time");
        VBox matchDateTime = new VBox(matchDate, matchTime);
        matchDateTime.setSpacing(5); // Add spacing between date and time
        matchDateTime.setAlignment(Pos.CENTER); // Center-align the content

        featuredMatchTime.getChildren().setAll(matchDateTime);
        featuredMatchTime.setSpacing(5);
        featuredMatchTime.setAlignment(Pos.CENTER);
    }

    private void loadLiveMatches() {
        // Fetch live matches (assuming no SQLException is thrown)
        System.out.println();
        liveMatches = matchesService.getLiveMatches(team.getId(),team.getIdtournoi());

        // Check if live matches are null or empty
        if (liveMatches == null || liveMatches.isEmpty()) {
            // Display a placeholder message
            Label placeholder = new Label("No live matches found.");
            placeholder.getStyleClass().add("placeholder-label");
            liveMatchesContainer.getChildren().add(placeholder);
        } else {
            // Display the live matches
            displayLiveMatches();
        }
    }

    private void displayLiveMatches() {
        // Clear the container before adding new content
        liveMatchesContainer.getChildren().clear();
        liveMatchesContainer.getStyleClass().add("live-matches");

        // Calculate the range of matches to display
        int startIndex = currentLiveMatchIndex;
        int endIndex = Math.min(currentLiveMatchIndex + MATCHES_PER_PAGE, liveMatches.size());

        // Add match cards for the current range
        for (int i = startIndex; i < endIndex; i++) {
            Matches match = liveMatches.get(i);
            HBox matchCard = createMatchCard(match);
            liveMatchesContainer.getChildren().add(matchCard);
        }
    }

    @FXML
    private void handleNextLiveMatches() {
        // Move to the next set of matches
        if (currentLiveMatchIndex + MATCHES_PER_PAGE < liveMatches.size()) {
            currentLiveMatchIndex += MATCHES_PER_PAGE;
            displayLiveMatches();
        }
    }

    @FXML
    private void handlePreviousLiveMatches() {
        // Move to the previous set of matches
        if (currentLiveMatchIndex - MATCHES_PER_PAGE >= 0) {
            currentLiveMatchIndex -= MATCHES_PER_PAGE;
            displayLiveMatches();
        }
    }

    private void loadFavouritePlayers() {
        List<Player> favouritePlayers = userService.getFavouritePlayers(currentManager);

        // Clear the container before adding new content
        favouritePlayersContainer.getChildren().clear();

        if (favouritePlayers == null || favouritePlayers.isEmpty()) {
            // Display a placeholder message or an empty card
            Label placeholder = new Label("No favourite players found.");
            placeholder.getStyleClass().add("placeholder-label");
            favouritePlayersContainer.getChildren().add(placeholder);
        } else {
            // Add player circles for each favourite player
            for (Player player : favouritePlayers) {
                VBox playerCircle = createPlayerCircle(player);
                favouritePlayersContainer.getChildren().add(playerCircle);
            }
        }
    }
    private void initializeUI() {
        // Assuming you have a reference to the VBox with the "+" icon
        VBox addPlayerVBox = (VBox) favouritePlayersContainer.getParent().getChildrenUnmodifiable().get(1);

        // Add an onClick listener to the VBox
        addPlayerVBox.setOnMouseClicked(event -> {
            // Create a dialog to display the list of players
            Dialog<Player> dialog = new Dialog<>();
            dialog.setTitle("Select a Player");

            // Set the button types (OK and Cancel)
            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            // Create a ListView to display the players
            ListView<Player> playerListView = new ListView<>();
            List<Player> playersList = userService.getAllPlayers(currentManager.getIdteam());
            ObservableList<Player> players = FXCollections.observableArrayList(playersList);
            playerListView.setItems(players);

            // Set a custom cell factory to display name, position, and rating
            playerListView.setCellFactory(param -> new ListCell<Player>() {
                @Override
                protected void updateItem(Player player, boolean empty) {
                    super.updateItem(player, empty);

                    if (empty || player == null) {
                        setText(null);
                    } else {
                        // Format the display text: Name - Position (Rating)
                        setText(player.getFirstname() + " " + player.getLastName() + " - " + player.getPosition() + " (" + player.getRating() + ")");
                    }
                }
            });

            // Add the ListView to the dialog
            dialog.getDialogPane().setContent(playerListView);

            // Set the result converter to return the selected player
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    return playerListView.getSelectionModel().getSelectedItem();
                }
                return null;
            });

            // Show the dialog and wait for the user's response
            dialog.showAndWait().ifPresent(selectedPlayer -> {
                selectedPlayer.setFavourite(true);
                // Add the selected player to the favorite players list
                userService.addFavouritePlayer(selectedPlayer);
                // Reload the favorite players to update the UI
                loadFavouritePlayers();
            });
        });
    }


    private void loadUpcomingMatches() {
        upcomingMatches = matchesService.getUpcomingMatches(team.getId());
        if (upcomingMatches == null || upcomingMatches.isEmpty()) {
            // Display a placeholder message
            Label placeholder = new Label("No upcoming matches found.");
            placeholder.getStyleClass().add("placeholder-label");
            upcomingMatchesContainer.getChildren().add(placeholder);
        } else {
            // Display the first set of matches
            displayUpcomingMatches();
        }
    }

    private void displayUpcomingMatches() {
        // Clear the container before adding new content
        upcomingMatchesContainer.getChildren().clear();
        upcomingMatchesContainer.getStyleClass().add("matches-container");

        // Calculate the range of matches to display
        int startIndex = currentMatchIndex;
        int endIndex = Math.min(currentMatchIndex + MATCHES_PER_PAGE, upcomingMatches.size());

        // Add match cards for the current range
        for (int i = startIndex; i < endIndex; i++) {
            Matches match = upcomingMatches.get(i);
            HBox matchCard = createMatchCard2(match);
            upcomingMatchesContainer.getChildren().add(matchCard);
        }
    }

    @FXML
    private void handleNextMatches() {
        // Move to the next set of matches
        if (currentMatchIndex + MATCHES_PER_PAGE < upcomingMatches.size()) {
            currentMatchIndex += MATCHES_PER_PAGE;
            displayUpcomingMatches();
        }
    }

    @FXML
    private void handlePreviousMatches() {
        // Move to the previous set of matches
        if (currentMatchIndex - MATCHES_PER_PAGE >= 0) {
            currentMatchIndex -= MATCHES_PER_PAGE;
            displayUpcomingMatches();
        }
    }

    private HBox createMatchCard(Matches match) {
        HBox matchCard = new HBox();
        matchCard.getStyleClass().add("match-card");

        // Left side: Live status, time, and minutes
        VBox leftSide = new VBox();
        leftSide.setAlignment(Pos.CENTER_LEFT);
        leftSide.setSpacing(0);

        VBox liveStatusBox = new VBox();
        liveStatusBox.setSpacing(5);
        LocalDateTime dateTime = LocalDateTime.now();
        Label liveIndicator = new Label("UNKNOWN"); // Initialize the label with a default value

        if (dateTime.isBefore(match.getMatchTime())) {
            liveIndicator.setText("UPCOMING"); // Update the text of the existing label
        } else {
            liveIndicator.setText("LIVE"); // Update the text of the existing label
        }

        liveIndicator.getStyleClass().add("live-indicator");
        LocalDateTime matchDateTime = match.getMatchTime();

        // Define formatters for date and time
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy"); // Example: 25 Oct 2023
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm"); // Example: 14:30

        // Format the date and time
        String formattedDate = matchDateTime.format(dateFormatter);
        String formattedTime = matchDateTime.format(timeFormatter);

        // Create labels for date and time
        Label matchDate = new Label(formattedDate);
        Label matchTime = new Label(formattedTime);

        // Add style classes to the labels
        matchDate.getStyleClass().add("match-time");
        matchTime.getStyleClass().add("match-time");
        liveStatusBox.getChildren().addAll(liveIndicator, matchDate,matchTime);



        leftSide.getChildren().addAll(liveStatusBox);

        // Right side: Teams, VS, and League
        VBox rightSide = new VBox();
        rightSide.setAlignment(Pos.CENTER);
        rightSide.setSpacing(5);
        HBox.setHgrow(rightSide, Priority.ALWAYS);

        HBox teamsBox = new HBox();
        teamsBox.setAlignment(Pos.CENTER);
        teamsBox.setSpacing(20);

        // Initialize ImageView objects for team logos
        ImageView teamALogo = new ImageView();
        ImageView teamBLogo = new ImageView();

        // Set team logos with null checks
        try {
            Team teamA = teamService.GetTeamByName(match.getTeamAName());
            Team teamB = teamService.GetTeamByName(match.getTeamBName());

            // Load team A logo
            teamALogo.setImage(loadTeamLogo(teamA));
            teamALogo.setFitWidth(30); // Set appropriate width
            teamALogo.setFitHeight(30); // Set appropriate height
            teamALogo.setPreserveRatio(true);

            // Load team B logo
            teamBLogo.setImage(loadTeamLogo(teamB));
            teamBLogo.setFitWidth(30); // Set appropriate width
            teamBLogo.setFitHeight(30); // Set appropriate height
            teamBLogo.setPreserveRatio(true);
        } catch (SQLException e) {
            System.err.println("Error fetching team details: " + e.getMessage());
            // Fallback to default logos
            teamALogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
            teamBLogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
        }

        // Team labels
        Label teamALabel = new Label(match.getTeamAName());
        teamALabel.getStyleClass().add("team-name");
        Label vsLabel = new Label("VS");
        vsLabel.getStyleClass().add("vs-label");
        Label teamBLabel = new Label(match.getTeamBName());
        teamBLabel.getStyleClass().add("team-name");
        VBox teamABox=new VBox(teamALogo,teamALabel);
        VBox teamBBox=new VBox(teamBLogo,teamBLabel);
        // Add logos and labels to the teamsBox
        teamsBox.getChildren().addAll(teamABox, vsLabel, teamBBox);

        // League label
        Label leagueLabel = new Label(tournois != null ? tournois.getNom() : "Unknown League");
        leagueLabel.getStyleClass().add("league-name");

        // Add teamsBox and leagueLabel to the rightSide
        rightSide.getChildren().addAll(teamsBox, leagueLabel);

        // Add left and right sides to the match card
        matchCard.getChildren().addAll(leftSide, rightSide);

        return matchCard;
    }
    private HBox createMatchCard2(Matches match) {
        HBox matchCard = new HBox();
        matchCard.getStyleClass().add("match-card2");

        // Left side: Live status, time, and minutes
        VBox leftSide = new VBox();
        leftSide.setAlignment(Pos.CENTER_LEFT);
        leftSide.setSpacing(0);

        VBox liveStatusBox = new VBox();
        liveStatusBox.setSpacing(5);
        LocalDateTime dateTime = LocalDateTime.now();
        Label liveIndicator = new Label("UNKNOWN"); // Initialize the label with a default value

        if (dateTime.isBefore(match.getMatchTime())) {
            liveIndicator.setText("UPCOMING"); // Update the text of the existing label
        } else {
            liveIndicator.setText("LIVE"); // Update the text of the existing label
        }

        liveIndicator.getStyleClass().add("live-indicator");
        LocalDateTime matchDateTime = match.getMatchTime();

        // Define formatters for date and time
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy"); // Example: 25 Oct 2023
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm"); // Example: 14:30

        // Format the date and time
        String formattedDate = matchDateTime.format(dateFormatter);
        String formattedTime = matchDateTime.format(timeFormatter);

        // Create labels for date and time
        Label matchDate = new Label(formattedDate);
        Label matchTime = new Label(formattedTime);

        // Add style classes to the labels
        matchDate.getStyleClass().add("match-time");
        matchTime.getStyleClass().add("match-time");
        liveStatusBox.getChildren().addAll(liveIndicator, matchDate,matchTime);



        leftSide.getChildren().addAll(liveStatusBox);

        // Right side: Teams, VS, and League
        VBox rightSide = new VBox();
        rightSide.setAlignment(Pos.CENTER);
        rightSide.setSpacing(5);
        HBox.setHgrow(rightSide, Priority.ALWAYS);

        HBox teamsBox = new HBox();
        teamsBox.setAlignment(Pos.CENTER);
        teamsBox.setSpacing(20);

        // Initialize ImageView objects for team logos
        ImageView teamALogo = new ImageView();
        ImageView teamBLogo = new ImageView();

        // Set team logos with null checks
        try {
            Team teamA = teamService.GetTeamByName(match.getTeamAName());
            Team teamB = teamService.GetTeamByName(match.getTeamBName());

            // Load team A logo
            teamALogo.setImage(loadTeamLogo(teamA));
            teamALogo.setFitWidth(30); // Set appropriate width
            teamALogo.setFitHeight(30); // Set appropriate height
            teamALogo.setPreserveRatio(true);

            // Load team B logo
            teamBLogo.setImage(loadTeamLogo(teamB));
            teamBLogo.setFitWidth(30); // Set appropriate width
            teamBLogo.setFitHeight(30); // Set appropriate height
            teamBLogo.setPreserveRatio(true);
        } catch (SQLException e) {
            System.err.println("Error fetching team details: " + e.getMessage());
            // Fallback to default logos
            teamALogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
            teamBLogo.setImage(new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true));
        }

        // Team labels
        Label teamALabel = new Label(match.getTeamAName());
        teamALabel.getStyleClass().add("team-name");
        Label vsLabel = new Label("VS");
        vsLabel.getStyleClass().add("vs-label");
        Label teamBLabel = new Label(match.getTeamBName());
        teamBLabel.getStyleClass().add("team-name");
        VBox teamABox=new VBox(teamALogo,teamALabel);
        VBox teamBBox=new VBox(teamBLogo,teamBLabel);
        // Add logos and labels to the teamsBox
        teamsBox.getChildren().addAll(teamABox, vsLabel, teamBBox);

        // League label
        Label leagueLabel = new Label(tournois != null ? tournois.getNom() : "Unknown League");
        leagueLabel.getStyleClass().add("league-name");

        // Add teamsBox and leagueLabel to the rightSide
        rightSide.getChildren().addAll(teamsBox, leagueLabel);

        // Add left and right sides to the match card
        matchCard.getChildren().addAll(leftSide, rightSide);

        return matchCard;
    }

    private Image loadTeamLogo(Team team) {
        if (team != null && team.getLogoPath() != null) {
            String logoPath = team.getLogoPath().trim();
            if (!logoPath.isEmpty()) {
                // Normalize path separators and extract filename
                String normalizedPath = logoPath.replace("\\", "/");
                String fileName = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1);

                // Construct absolute path for team logos
                String absolutePath = "C:/xampp/htdocs/img/teams/" + fileName;
                File logoFile = new File(absolutePath);

                if (logoFile.exists() && logoFile.isFile()) {
                    try {
                        return new Image(logoFile.toURI().toString());
                    } catch (Exception e) {
                        System.err.println("Failed to load logo: " + e.getMessage());
                    }
                } else {
                    System.err.println("Logo file not found at: " + absolutePath);
                }
            }
        }
        // Fallback to default logo with proper error handling
        try {
            return new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true);
        } catch (Exception e) {
            System.err.println("Failed to load default logo: " + e.getMessage());
            return null; // Or throw exception based on your requirements
        }
    }
    private Image loadPlayerLogo(Player player) {
        if (player != null && player.getProfilePicture() != null) {
            String logoPath = player.getProfilePicture().trim();
            if (!logoPath.isEmpty()) {
                // Normalize path and extract filename
                String normalizedPath = logoPath.replace("\\", "/");
                String fileName = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1);

                // Construct absolute path for player images
                String absolutePath = "C:/xampp/htdocs/img/players/" + fileName;
                File logoFile = new File(absolutePath);

                if (logoFile.exists() && logoFile.isFile()) {
                    try {
                        return new Image(logoFile.toURI().toString());
                    } catch (Exception e) {
                        System.err.println("Failed to load player image: " + e.getMessage());
                    }
                } else {
                    System.err.println("Player image not found at: " + absolutePath);
                }
            }
        }
        // Fallback to default with improved error handling
        try {
            return new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true);
        } catch (Exception e) {
            System.err.println("Failed to load default player image: " + e.getMessage());
            return null;
        }
    }
    private VBox createPlayerCircle(Player player) {
        VBox playerCircle = new VBox();
        playerCircle.getStyleClass().add("player-circle");

        // Add player image
        ImageView playerImage = new ImageView();
        playerImage.setImage(loadPlayerLogo(player));
        playerImage.setFitWidth(100);
        playerImage.setFitHeight(100);
        playerImage.setPreserveRatio(true);
        playerImage.getStyleClass().add("player-image");

        // Add player name
        Label playerName = new Label(player.getFirstname());
        playerName.getStyleClass().add("player-name");

        playerCircle.getChildren().addAll(playerImage, playerName);
        return playerCircle;
    }

    @FXML
    private void handleFullSchedule() {
        // Handle full schedule button click
    }

    @FXML
    private void handleMatchDetails() {
        // Handle match details button click
    }

    @FXML
    private void handleAddNewPlayer() {
        // Handle add new player click
    }

    @FXML
    private void handlePreviousPlayers() {
        // Handle previous players navigation
    }

    @FXML
    private void handleNextPlayers() {
        // Handle next players navigation
    }
}
