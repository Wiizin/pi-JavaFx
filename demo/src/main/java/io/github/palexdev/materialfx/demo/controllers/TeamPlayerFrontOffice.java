package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader;
import io.github.palexdev.materialfx.demo.model.*;
import io.github.palexdev.materialfx.demo.services.MatchesService;
import io.github.palexdev.materialfx.demo.services.TeamService;
import io.github.palexdev.materialfx.demo.services.TournoisService;
import io.github.palexdev.materialfx.demo.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class TeamPlayerFrontOffice implements Initializable{

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
        private Team team = null;
        private Tournois tournois = null;
        private List<Matches> upcomingMatches;
        private List<Matches> liveMatches;
        private int currentLiveMatchIndex = 0;
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

            loadFeaturedMatch(0);
            loadLiveMatches();
            loadUpcomingMatches();

        }

        private void loadFeaturedMatch(int index) {
            if (team == null || tournois == null) {
                System.err.println("Team or tournament data is missing.");
                featuredLeagueLabel.setText("No featured match available.");
                return;
            }

            List<Matches> featuredMatches;
            try {
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
                String logoPath = team.getLogoPath();
                if (logoPath != null && !logoPath.trim().isEmpty()) {
                    File logoFile = new File(logoPath);
                    if (logoFile.exists() && logoFile.isFile()) {
                        try {
                            return new Image(logoFile.toURI().toString());
                        } catch (Exception e) {
                            System.err.println("Failed to load logo: " + e.getMessage());
                        }
                    } else {
                        System.err.println("Logo file does not exist or is not a valid file: " + logoFile.getAbsolutePath());
                    }
                } else {
                    System.err.println("Invalid logo path: " + logoPath);
                }
            }
            // Fallback to default logo
            return new Image(MFXDemoResourcesLoader.load("sportify.png"), 24, 24, true, true);
        }

        private VBox createPlayerCircle(Player player) {
            VBox playerCircle = new VBox();
            playerCircle.getStyleClass().add("player-circle");

            // Add player image
            ImageView playerImage = new ImageView(new Image(player.getProfilePicture()));
            playerImage.setFitWidth(50);
            playerImage.setFitHeight(50);
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
