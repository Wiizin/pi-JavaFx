package io.github.palexdev.materialfx.demo.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.MFXStepper.MFXStepperEvent;
import io.github.palexdev.materialfx.controls.base.MFXLabeled;
import io.github.palexdev.materialfx.controls.cell.MFXCheckListCell;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.demo.model.*;
import io.github.palexdev.materialfx.demo.services.*;
import io.github.palexdev.materialfx.filter.EnumFilter;
import io.github.palexdev.materialfx.filter.IntegerFilter;
import io.github.palexdev.materialfx.filter.StringFilter;
import io.github.palexdev.materialfx.utils.others.FunctionalStringConverter;
import io.github.palexdev.materialfx.utils.others.observables.When;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.materialfx.validation.Constraint;
import io.github.palexdev.materialfx.validation.MFXValidator;
import io.github.palexdev.materialfx.validation.Validated;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.Match;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

import io.github.palexdev.materialfx.filter.StringFilter;

import static io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader.loadURL;

public class TeamSelection implements Initializable {

    private final MFXTextField loginField;
    private final MFXPasswordField passwordField;
    private final MFXTextField firstNameField;
    private final MFXTextField lastNameField;
    private final MFXComboBox<String> genderCombo;
    private final MFXCheckbox checkbox;
    private final MFXCheckbox checkbox2;
    private final MFXComboBox<String> TeamTypeCombo;
    private final MFXTextField nameField;
    private final MFXTextField categoryField;
    private final MFXTextField nbPlayersField;
    private final MFXComboBox<ModeJeu> modeJeuComboBox;
    private final MFXButton uploadButton;
    private final Label fileLabel;
    private final MFXListView<User> custList;
    private final ObservableList<Player> Players;
    private final MFXCheckListView<User> checkList;
    private final ObservableList<User> checkedPlayers = FXCollections.observableArrayList();
    private MFXPaginatedTableView<Player> paginated;
    @FXML
    private MFXButton unlock;

    @FXML
    private MFXStepper stepper;
    private final Map<String, Integer> leagueIdMap = new HashMap<>();
    private final Map<String, Integer> teamIdMap = new HashMap<>();
    private static final String API_KEY = "479f93535e8f7b487ac4d5b41e8783bfcd8312bfc8791783a91c031cdbef96f3"; // Replace with your API key
    private static final String API_HOST = "apiv3.apifootball.com";
    int season = 2024; // Example: Season year
    private MFXComboBox<String> TeamComboBox;
    private MFXComboBox<String> LeagueComboBox;
    private boolean constraintsInitialized = false;
    TeamService teamService=new TeamService();
    UserService userService=new UserService();
    LocalDate currentDate = LocalDate.now();
    // Define the desired date format
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate dateAfter7Days = currentDate.plusDays(7);
    // Format the current date as a string
    String date = currentDate.format(formatter);; // Start date
    String date2 = dateAfter7Days.format(formatter);
    public TeamSelection() {
        loginField = new MFXTextField();
        passwordField = new MFXPasswordField();
        firstNameField = new MFXTextField();
        lastNameField = new MFXTextField();
        genderCombo = new MFXComboBox<>();
        TeamTypeCombo = new MFXComboBox<>();
        checkbox = new MFXCheckbox("Confirm Data?");
        checkbox2= new MFXCheckbox("Confirm Data?");
        nameField = new MFXTextField();
        categoryField = new MFXTextField();
        nbPlayersField = new MFXTextField();
        modeJeuComboBox = new MFXComboBox<>();
        uploadButton = new MFXButton("Upload Logo");
        fileLabel = new Label("No file selected");
        custList = new MFXListView<>();
        Players = FXCollections.observableArrayList();
        checkList = new MFXCheckListView<>();
        paginated = new MFXPaginatedTableView<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UserService userService = new UserService();
        Players.setAll(userService.getAvailablePlayers());

        // Initialize fields
        loginField.setPromptText("Username...");
        loginField.getValidator().constraint("The username must be at least 6 characters long", loginField.textProperty().length().greaterThanOrEqualTo(6));
        loginField.setLeadingIcon(new MFXIconWrapper("fas-user", 16, Color.web("#4D4D4D"), 24));

        passwordField.setPromptText("Password...");
        passwordField.getValidator().constraint("The password must be at least 8 characters long", passwordField.textProperty().length().greaterThanOrEqualTo(8));

        firstNameField.setPromptText("First Name...");
        lastNameField.setPromptText("Last Name...");

        genderCombo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));

        TeamTypeCombo.setItems(FXCollections.observableArrayList("Existed Team", "Create new Team"));

        nameField.setPromptText("Team Name...");
        nameField.getStyleClass().add("custom-text-field");
        nameField.setStyle("-fx-pref-width: 450; -fx-border-radius: 10; -fx-start-margin: 75;");

        categoryField.setPromptText("Category...");
        categoryField.setStyle("-fx-pref-width: 450; -fx-border-radius: 10; -fx-padding: 5;");

        nbPlayersField.setPromptText("Number of Players...");
        nbPlayersField.setStyle("-fx-pref-width: 450; -fx-border-radius: 10; -fx-padding: 5;");

        modeJeuComboBox.setPromptText("Game Mode...");
        modeJeuComboBox.setItems(FXCollections.observableArrayList(ModeJeu.values()));
        modeJeuComboBox.setStyle("-fx-pref-width: 450; -fx-border-radius: 10; -fx-padding: 5;");

        // Add a file upload field
        uploadButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        fileLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px; -fx-font-family: 'Roboto';");
        uploadButton.setOnAction(e -> {
            File selectedFile = fileChooser.showOpenDialog(stepper.getScene().getWindow());
            if (selectedFile != null) {
                fileLabel.setText(selectedFile.getAbsolutePath());
            }
        });

        // Add validators to the fields
        nameField.getValidator().constraint("Team name is required", nameField.textProperty().length().greaterThanOrEqualTo(1));
        categoryField.getValidator().constraint("Category is required", categoryField.textProperty().length().greaterThanOrEqualTo(1));
        nbPlayersField.getValidator().constraint("Number of players is required", nbPlayersField.textProperty().length().greaterThanOrEqualTo(1));
        modeJeuComboBox.getValidator().constraint("Game mode is required", modeJeuComboBox.valueProperty().isNotNull());
        // Initialize stepper with only Step 1
        initializeStep1();

        // Add listener to TeamTypeCombo to dynamically update steps
        TeamTypeCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                initializeStep1();
                updateSteps(newValue);
                constraintsInitialized = true;
            }
        });



        // Bind unlock button visibility to stepper's mouse transparency
        unlock.visibleProperty().bind(stepper.mouseTransparentProperty());
        unlock.setOnAction(event -> stepper.setMouseTransparent(false));

    }
    private void setupPaginated() {
        // Existing columns
        MFXTableColumn<Player> id = new MFXTableColumn<>("ID", false, Comparator.comparing(Player::getId));
        id.setAlignment(Pos.CENTER);
        id.setFont(Font.font("System", FontWeight.BOLD, 12));

        MFXTableColumn<Player> firstname = new MFXTableColumn<>("firstName", false, Comparator.comparing(Player::getFirstname));
        firstname.setAlignment(Pos.CENTER);
        firstname.setFont(Font.font("System", FontWeight.BOLD, 12));

        MFXTableColumn<Player> lastname = new MFXTableColumn<>("lastname", false, Comparator.comparing(Player::getLastName));
        lastname.setAlignment(Pos.CENTER);
        lastname.setFont(Font.font("System", FontWeight.BOLD, 12));

        MFXTableColumn<Player> Rating = new MFXTableColumn<>("Rating", false, Comparator.comparing(Player::getRating));
        Rating.setAlignment(Pos.CENTER);
        Rating.setFont(Font.font("System", FontWeight.BOLD, 12));

        MFXTableColumn<Player> position = new MFXTableColumn<>("position", false, Comparator.comparing(Player::getPosition));
        position.setAlignment(Pos.CENTER);
        position.setFont(Font.font("System", FontWeight.BOLD, 12));


        // Row cell factories for existing columns
        id.setRowCellFactory(player -> new MFXTableRowCell<>(Player::getId) {{
            setAlignment(Pos.CENTER);
        }});

        firstname.setRowCellFactory(player -> new MFXTableRowCell<>(Player::getFirstname) {{
            setAlignment(Pos.CENTER);
        }});

        lastname.setRowCellFactory(player -> new MFXTableRowCell<>(Player::getLastName) {{
            setAlignment(Pos.CENTER);
        }});
        Rating.setRowCellFactory(player -> new MFXTableRowCell<>(Player::getRating) {{
            setAlignment(Pos.CENTER);
        }});
        position.setRowCellFactory(player -> new MFXTableRowCell<>(Player::getPosition) {{
            setAlignment(Pos.CENTER);
        }});

        // Add a new column for the checkbox
        MFXTableColumn<Player> selectColumn = new MFXTableColumn<>("Select", false);
        selectColumn.setAlignment(Pos.CENTER);
        selectColumn.setFont(Font.font("System", FontWeight.BOLD, 12));

        selectColumn.setRowCellFactory(player -> {
            // Create a checkbox for each row
            MFXCheckbox checkbox = new MFXCheckbox();

            // Bind the checkbox to the player's selected property
            checkbox.selectedProperty().bindBidirectional(player.selectedProperty());

            // Add a listener to update the checkedPlayers list
            checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    checkedPlayers.add(player); // Add player to the list if checked
                } else {
                    checkedPlayers.remove(player); // Remove player from the list if unchecked
                }
            });


            // Create a cell that displays only the checkbox
            return new MFXTableRowCell<>(row -> "") {{
                setAlignment(Pos.CENTER);
                setGraphic(checkbox); // Set the checkbox as the graphic of the cell
                setText(null); // Ensure no text is displayed
            }};
        });

        // Add all columns to the table
        paginated.getTableColumns().addAll(id, firstname, lastname,position,Rating,selectColumn);

        // Add filters
        paginated.getFilters().addAll(
                new StringFilter<>("FirstName", Player::getFirstname),
                new StringFilter<>("LastName", Player::getLastName),
                new IntegerFilter<>("Rating", Player::getRating),
                new StringFilter<>("Position", Player::getPosition)
        );

        // Create a FilteredList to handle the filtering
        FilteredList<Player> filteredData = new FilteredList<>(Players);
        // Add a listener to the filters collection
        paginated.getFilters().addListener((ListChangeListener<Object>) change -> {
            filteredData.setPredicate(user -> {
                if (paginated.getFilters().isEmpty()) {
                    return true;
                }
                try {
                    for (var filter : paginated.getFilters()) {
                        if (!((Predicate<User>) filter).test(user)) {
                            return false;
                        }
                    }
                    return true;
                } catch (IllegalArgumentException e) {
                    // Handle invalid range values
                    System.out.println("Invalid filter range: " + e.getMessage());
                    return true; // Show all items when filter is invalid
                } catch (Exception e) {
                    System.out.println("Filter error: " + e.getMessage());
                    return true; // Show all items on error
                }
            });
        });
    }

    private void initializeStep1() {
        // Create Step 1
        MFXStepperToggle step1 = new MFXStepperToggle("Step 1", new MFXFontIcon("fas-futbol", 16, Color.web("#f1c40f")));
        VBox step1Box = new VBox(20, wrapNodeForValidation(TeamTypeCombo));
        step1Box.setAlignment(Pos.CENTER);
        step1.setContent(step1Box);

        // Add Step 1 to the stepper
        stepper.getStepperToggles().setAll(step1);
    }

    private void updateSteps(String teamType) {
        // Clear existing steps and validators
        stepper.getStepperToggles().clear();

        // Add new steps based on the selected team type
        List<MFXStepperToggle> steps;
        if ("Create new Team".equals(teamType)) {
            steps = createNewTeamSteps();
        } else {
            steps = manageExistingTeamSteps();
        }

        // Add the new steps to the stepper
        stepper.getStepperToggles().addAll(steps);
    }


    private List<MFXStepperToggle> createNewTeamSteps() {
        MFXStepperToggle step1 = new MFXStepperToggle("Step 1", new MFXFontIcon("fas-futbol", 16, Color.web("#f1c40f")));
        VBox step1Box = new VBox(20, wrapNodeForValidation(TeamTypeCombo));
        step1Box.setAlignment(Pos.CENTER);
        step1.setContent(step1Box);

        MFXStepperToggle step2 = new MFXStepperToggle("Step 2", new MFXFontIcon("fas-trophy", 16, Color.web("#f1c40f")));
        VBox step2Box = new VBox(10,
                wrapNodeForValidation(nameField),
                wrapNodeForValidation(categoryField),
                wrapNodeForValidation(nbPlayersField),
                wrapNodeForValidation(modeJeuComboBox),
                uploadButton,
                fileLabel
        );
        step2Box.setAlignment(Pos.CENTER);
        step2.setContent(step2Box);

        // Add validators to Step 2
        step2.getValidator().dependsOn(nameField.getValidator())
                .dependsOn(categoryField.getValidator())
                .dependsOn(nbPlayersField.getValidator())
                .dependsOn(modeJeuComboBox.getValidator());

        MFXStepperToggle step3 = new MFXStepperToggle("Step 3", new MFXFontIcon("fas-users", 16, Color.web("#f1c40f")));
        // Ensure paginated is initialized before setting it up
        if (paginated == null) {
            paginated = new MFXPaginatedTableView<>();
        }
        setupPaginated();
        paginated.autosizeColumnsOnInitialization();
        paginated.setPrefSize(360, 160);

        // Fetch data from the database and populate the table
        When.onChanged(paginated.currentPageProperty())
                .then((oldValue, newValue) -> paginated.autosizeColumns())
                .listen();
        paginated.setItems(Players);

        // Add MFXListView and MFXCheckListView to Step 3
        VBox step3Box = new VBox(20, paginated);
        step3Box.setAlignment(Pos.CENTER);
        step3.setContent(step3Box);

        MFXStepperToggle step4 = new MFXStepperToggle("Step 4", new MFXFontIcon("fas-check", 16, Color.web("#85CB33")));
        Node step4Grid = createGrid();
        step4.setContent(step4Grid);
        if (!constraintsInitialized) {
            constraintsInitialized = true;
            step4.getValidator().constraint("Data must be confirmed", checkbox.selectedProperty());
        }
        checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Checkbox state changed: " + newValue); // Debugging
        });

        // Handle stepper navigation logic
        // Handle stepper navigation logic
        stepper.setOnNext(event -> {
            int currentStepIndex = stepper.getCurrentIndex();

            if (currentStepIndex == 2) { // Step 3 is index 2
                System.out.println("Checkbox selected: " + checkbox.isSelected());

                // Check if the checkbox is selected before proceeding to Step 4
                if (!checkbox.isSelected()) {
                    System.out.println("Checkbox is not selected. Please confirm the data before proceeding."); // Debugging
                    event.consume(); // Prevent moving to the next step
                    return;
                }

                // Proceed to Step 4 logic
                System.out.println("Proceeding to Step 4 logic..."); // Debugging

            } else if (currentStepIndex == 3) { // Step 4 is index 3
                System.out.println("Step 4 navigation triggered."); // Debugging

                // Capture selected players
                ObservableList<User> selectedPlayers = FXCollections.observableArrayList(checkedPlayers);
                System.out.println("Selected Players: " + selectedPlayers); // Debugging

                // Handle logo file upload
                String logoPath = null;
                if (fileLabel.getText() != null && !fileLabel.getText().equals("No file selected")) {
                    File selectedFile = new File(fileLabel.getText());
                    if (selectedFile.exists()) {
                        logoPath = saveUploadedFile(selectedFile,"team"); // Save the file and get its relative path
                    } else {
                        System.err.println("Selected file does not exist: " + selectedFile);
                    }
                }

                // Step 4 Logic: Add team, update players and manager, and navigate to another page
                Team newTeam = new Team();
                newTeam.setNom(nameField.getText());
                newTeam.setCategorie(categoryField.getText());
                newTeam.setNombreJoueurs(Integer.parseInt(nbPlayersField.getText()));
                newTeam.setModeJeu(modeJeuComboBox.getValue());
                System.out.println("modeJeuValue: " + modeJeuComboBox.getValue());
                newTeam.setLogoPath(logoPath);

                // Insert the team into the database and get the generated id_team
                int idTeam = 0;
                try {
                    idTeam = teamService.insert2(newTeam);
                    System.out.println("Team inserted with ID: " + idTeam); // Debugging
                } catch (SQLException e) {
                    System.err.println("Failed to insert team: " + e.getMessage());
                    e.printStackTrace();
                    return; // Exit if insertion fails
                }

                // Update id_team for selected players
                if (checkedPlayers != null) {
                    for (User player : checkedPlayers) {
                        player.setIdteam(idTeam);
                        userService.update(player);
                        System.out.println("Updated player: " + player.getFirstname() + " " + player.getLastName()); // Debugging
                    }
                }

                // Update id_team for the current manager
                User currentManager = UserSession.getInstance().getCurrentUser();
                if (currentManager != null) {
                    currentManager.setIdteam(idTeam);
                    userService.update(currentManager);
                    System.out.println("Updated manager: " + currentManager.getFirstname()); // Debugging
                }

                // Navigate to the OrganizerHome page
                try {
                    URL fxmlLocation = loadURL("fxml/OrganizerHome.fxml");
                    if (fxmlLocation == null) {
                        System.err.println("FXML file not found!");
                        return;
                    }
                    FXMLLoader loader = new FXMLLoader(fxmlLocation);
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) stepper.getScene().getWindow(); // Get the current stage
                    stage.setScene(scene); // Set the new scene
                    stage.show();
                    System.out.println("Navigation to OrganizerHome.fxml successful."); // Debugging
                } catch (IOException e) {
                    System.err.println("Failed to load FXML: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        return List.of(step1, step2, step3, step4);
    }

    private List<MFXStepperToggle> manageExistingTeamSteps() {
        // Step 1: Choose Team Type
        MFXStepperToggle step1 = new MFXStepperToggle("Step 1", new MFXFontIcon("fas-futbol", 16, Color.web("#f1c40f")));
        VBox step1Box = new VBox(20, wrapNodeForValidation(TeamTypeCombo));
        step1Box.setAlignment(Pos.CENTER);
        step1.setContent(step1Box);

        // Step 2: Fetch and Display Real-Life Teams
        MFXStepperToggle step2 = new MFXStepperToggle("Step 2", new MFXFontIcon("fas-users", 16, Color.web("#49a6d7")));
        LeagueComboBox = new MFXComboBox<>(); // ComboBox to display leagues
        TeamComboBox = new MFXComboBox<>(); // ComboBox to display teams
        VBox step2Box = new VBox(20, LeagueComboBox, TeamComboBox);
        step2Box.setAlignment(Pos.CENTER);
        step2.setContent(step2Box);

        // Step 3: Confirm Selection
        MFXStepperToggle step3 = new MFXStepperToggle("Step 3", new MFXFontIcon("fas-check", 16, Color.web("#85CB33")));
        Node step3Grid = createGrid2();
        step3.setContent(step3Grid);

        MFXStepperToggle step4 = new MFXStepperToggle("Step 4", new MFXFontIcon("fas-trophy", 16, Color.web("#f1c40f")));
        Label congrats = new Label("Congrats!");
        congrats.setStyle("-fx-font-size: 18px; -fx-text-fill: #4CAF50;");
        VBox step4Box = new VBox(20, congrats);
        step4Box.setAlignment(Pos.CENTER);
        step4.setContent(step4Box);
        // Initialize constraints if not already initialized
        if (!constraintsInitialized) {
            constraintsInitialized = true;
            step3.getValidator().constraint("Data must be confirmed", checkbox2.selectedProperty());
        }
        checkbox2.selectedProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Checkbox state changed: " + newValue); // Debugging
        });
        // Add listener to LeagueComboBox to fetch teams when a league is selected
        LeagueComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    // Fetch teams for the selected league using its ID
                    int leagueId = leagueIdMap.get(newValue); // Get league ID from the map
                    fetchTeamsForLeague(leagueId);
                }
            }
        });

        // Fetch Teams from API-Football when moving from Step 1 to Step 2
        stepper.setOnNext(event -> {
            int currentStepIndex = stepper.getCurrentIndex();

            if (currentStepIndex == 1) {
                // Step 1: Fetch and populate top leagues
                fetchAndPopulateTopLeagues();
            } else if (currentStepIndex == 3) {
                // Step 3: Handle team and tournament creation
                handleTeamAndTournamentCreation();
            }
        });

        return List.of(step1, step2, step3,step4);
    }
    private void fetchAndPopulateTopLeagues() {
        Set<Integer> topLeagueIds = Set.of(61, 152, 207, 4, 302, 3, 56);
        Set<String> uniqueLeagues = new HashSet<>();

        // Construct the API URL for fetching leagues
        String apiUrl = "https://" + API_HOST + "?action=get_leagues&season=" + season + "&APIkey=" + API_KEY;

        try {
            HttpResponse<String> response = makeApiRequest(apiUrl);

            if (response.statusCode() == 200) {
                // Parse JSON response
                JsonArray leaguesArray = JsonParser.parseString(response.body()).getAsJsonArray();

                // Clear the ComboBox and add fetched leagues
                LeagueComboBox.getItems().clear();
                leagueIdMap.clear(); // Clear the league ID map
                System.out.println("Leagues Array: " + leaguesArray);

                // Iterate through the leagues array
                for (JsonElement leagueElement : leaguesArray) {
                    JsonObject leagueObject = leagueElement.getAsJsonObject();

                    // Check if the required keys exist in the JSON object
                    if (leagueObject.has("league_id") && leagueObject.has("league_name")) {
                        int leagueId = leagueObject.get("league_id").getAsInt();

                        // Check if the league is one of the top leagues
                        if (topLeagueIds.contains(leagueId)) {
                            String leagueName = leagueObject.get("league_name").getAsString();
                            System.out.println("leagueId: " + leagueId + " league name: " + leagueName);

                            // Add league name to the set if it's not already present
                            if (uniqueLeagues.add(leagueName)) {
                                // Add league name to ComboBox and store league ID in the map
                                LeagueComboBox.getItems().add(leagueName);
                                leagueIdMap.put(leagueName, leagueId);
                            }
                        }
                    } else {
                        System.err.println("Missing required keys in JSON object: " + leagueObject);
                    }
                }

                // Log success
                System.out.println("Successfully fetched and populated top leagues for season: " + season);
            } else {
                // Log failure with status code and response body
                System.err.println("Failed to fetch leagues. Status code: " + response.statusCode() + ", Response: " + response.body());
            }
        } catch (Exception e) {
            // Log any exceptions that occur during the API request
            System.err.println("Error fetching leagues: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Method to handle team and tournament creation
    private void handleTeamAndTournamentCreation() {
        String selectedLeague = LeagueComboBox.getValue();
        String selectedTeam = TeamComboBox.getValue();

        if (selectedLeague == null || selectedTeam == null) {
            // Show error if no league or team is selected
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Selection Error");
            errorAlert.setHeaderText("No league or team selected.");
            errorAlert.setContentText("Please select a league and team before confirming.");
            errorAlert.showAndWait();
            return;
        }

            System.out.println("Selected League: " + selectedLeague); // Debugging
            System.out.println("Selected Team: " + selectedTeam); // Debugging
            // Proceed to Step 3 logic
            System.out.println("Proceeding to Step 3 logic..."); // Debugging
        if (selectedLeague != null && selectedTeam != null) {
            // Check if the team already has a manager in the SQL database
            Team t = null;
            Organizer o = null;
            try {
                t = teamService.GetTeamByName(selectedTeam);
                System.out.println("Team fetched from database: " + (t != null ? t.getNom() : "null")); // Debugging

                if (t != null) {
                    o = userService.getManager(t.getId());
                }
            } catch (SQLException e) {
                System.err.println("Error fetching team: " + e.getMessage()); // Debugging
                throw new RuntimeException(e);
            }

            if (t != null && o != null) {
                // Show alert if the team already has a manager
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Team Already Managed");
                alert.setHeaderText("This team already has a manager.");
                alert.setContentText("Please select another team.");
                alert.showAndWait();
            } else {
                // If the team does not exist, create a new team
                if (t == null) {
                    t = new Team();
                    t.setNom(selectedTeam);
                    t.setCategorie("Football");
                    t.setModeJeu(ModeJeu.EN_GROUPE);
                    System.out.println("New team created: " + t.getNom()); // Debugging
                }

                TournoisService tournoisService = new TournoisService();
                Tournois tournois = null;
                try {
                    tournois = tournoisService.GetTournoisByName(selectedLeague);
                    System.out.println("Tournament fetched from database: " + (tournois != null ? tournois.getNom() : "null")); // Debugging
                } catch (SQLException e) {
                    System.err.println("Error fetching tournament: " + e.getMessage()); // Debugging
                    throw new RuntimeException(e);
                }

                int idtournoi = 0;
                if (tournois == null) {
                    tournois = new Tournois();
                    // Create tournament if it doesn't exist
                    tournois.setNom(selectedLeague);
                    tournois.setStartDate(LocalDate.now()); // Set a valid start date
                    tournois.setEndDate(LocalDate.now().plusDays(7)); // Set a valid end date
                    User currentManager = UserSession.getInstance().getCurrentUser();
                    if (currentManager != null) {
                        tournois.setIdorganiser(currentManager.getId());
                    } else {
                        tournois.setIdorganiser(0);
                    }

                    try {
                        idtournoi = tournoisService.insert2(tournois);
                        System.out.println("Tournament inserted: " + tournois.getNom()); // Debugging
                    } catch (SQLException e) {
                        System.err.println("Error inserting tournament: " + e.getMessage()); // Debugging
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        idtournoi = tournoisService.GetTournoisByName(selectedLeague).getId();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                // Set the tournament ID for the team
                t.setIdtournoi(idtournoi);

                // Insert the team into the database if it doesn't already exist
                int idTeam = 0;
                if (t.getId() == 0) { // Check if the team is new (ID not set)
                    try {
                        idTeam = teamService.insert2(t);
                        System.out.println("Team inserted with ID: " + idTeam); // Debugging
                    } catch (SQLException e) {
                        System.err.println("Error inserting team: " + e.getMessage()); // Debugging
                        throw new RuntimeException(e);
                    }
                } else {
                    idTeam = t.getId();
                }

                int idteamapi = teamIdMap.get(selectedTeam);
                int idleague = leagueIdMap.get(selectedLeague);
                //Fetch standing From Api and insert into SQl
                fetchAndProcessStandings(tournois,selectedLeague,idleague);
                System.out.println("Standing fetched and added to SQL for teams: " + selectedTeam); // Debugging

                // Fetch players from API and insert into SQL
                fetchAndAddPlayersToSQL(selectedTeam, idteamapi, idTeam);
                System.out.println("Players fetched and added to SQL for team: " + selectedTeam); // Debugging

                // Fetch and add upcoming matches
                fetchAndAddUpcomingTeamMatchesToSQL(selectedTeam, idleague, idteamapi, idTeam, idtournoi);
                System.out.println("Upcoming matches fetched and added to SQL for team: " + selectedTeam);

                //fetchAndAddUpcomingRestOfLeagueMatchesToSQL(idleague, idteamapi, idtournoi);
                //System.out.println("The rest of the upcoming matches fetched and added to SQL: " + selectedTeam);

                // Update the id_team of the current user
                User currentManager = UserSession.getInstance().getCurrentUser();
                if (currentManager != null) {
                    System.out.println("Current manager: " + currentManager.getFirstname()); // Debugging
                    currentManager.setIdteam(idTeam);
                    userService.update(currentManager);
                    System.out.println("Updated manager: " + currentManager.getFirstname()); // Debugging
                } else {
                    System.err.println("Current manager is null."); // Debugging
                }

                // Reload the page or show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Team and players added successfully.");
                successAlert.setContentText("The page will now reload.");
                Optional<ButtonType> result = successAlert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Reload the page or navigate to another view
                    try {
                        URL fxmlLocation = loadURL("fxml/OrganizerHome.fxml");
                        if (fxmlLocation == null) {
                            System.err.println("FXML file not found!"); // Debugging
                            return;
                        }
                        FXMLLoader loader = new FXMLLoader(fxmlLocation);
                        Parent root = loader.load();
                        Scene scene = new Scene(root);
                        Stage stage = (Stage) stepper.getScene().getWindow(); // Get the current stage
                        stage.setScene(scene); // Set the new scene
                        stage.show();
                        System.out.println("Navigation to OrganizerHome.fxml successful."); // Debugging
                    } catch (IOException e) {
                        System.err.println("Failed to load FXML: " + e.getMessage()); // Debugging
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("Selected League or Team is null."); // Debugging
            // Show error if no league or team is selected
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Selection Error");
            errorAlert.setHeaderText("No league or team selected.");
            errorAlert.setContentText("Please select a league and team before confirming.");
            errorAlert.showAndWait();
        }

    }

    // Method to navigate to OrganizerHome.fxml
    private void navigateToOrganizerHome() {
        try {
            URL fxmlLocation = loadURL("fxml/OrganizerHome.fxml");
            if (fxmlLocation == null) {
                System.err.println("FXML file not found!"); // Debugging
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) stepper.getScene().getWindow(); // Get the current stage
            stage.setScene(scene); // Set the new scene
            stage.show();
            System.out.println("Navigation to OrganizerHome.fxml successful."); // Debugging
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + e.getMessage()); // Debugging
            e.printStackTrace();
        }
    }
    private void fetchAndAddUpcomingTeamMatchesToSQL(String teamName, int idleague, int idteamapi, int idTeam, int idtournoi) {
        // Construct the API URL for fetching fixtures
        String apiUrl = "https://" + API_HOST + "/?action=get_events&from="+date+"&to="+date2+"&league_id=" + idleague + "&APIkey=" + API_KEY;

        //System.out.println("API URL: " + apiUrl); // Debugging: Print the API URL

        try {
            // Make API request
            HttpResponse<String> response = makeApiRequest(apiUrl);

            if (response.statusCode() == 200) {
                // Parse JSON response
                JsonArray matchesArray = JsonParser.parseString(response.body()).getAsJsonArray();

                // Initialize the service for creating matches
                MatchesService matchesService = new MatchesService();

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
                    if (homeTeamId == idteamapi || awayTeamId == idteamapi) {
                        // Create a new Match object
                        Matches match = new Matches();
                        match.setTeamAName(homeTeamName);
                        match.setTeamBName(awayTeamName);
                        match.setMatchTime(date);
                        match.setStatus(status);
                        match.setIdTeamA(homeTeamDbId);
                        match.setIdTeamB(awayTeamDbId);
                        match.setLocationMatch(location_match);
                        if (homeTeamId == idteamapi) {
                            match.setIdTeamA(idTeam);
                        } else {
                            match.setIdTeamB(idTeam);
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
                        match.setIdTeamA(homeTeamDbId);
                        match.setIdTeamB(awayTeamDbId);
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

    private void fetchAndProcessStandings(Tournois tournament, String leagueName,int leagueid) {
        String apiUrl = "https://" + API_HOST + "/?action=get_standings&league_id=" +leagueid+ "&APIkey=" + API_KEY;

        try {
            HttpResponse<String> response = makeApiRequest(apiUrl);

            if (response.statusCode() == 200) {
                JsonArray standingsArray = JsonParser.parseString(response.body()).getAsJsonArray();
                TeamRankingService rankingService = new TeamRankingService();

                for (JsonElement standingElement : standingsArray) {
                    JsonObject standingData = standingElement.getAsJsonObject();

                    // Extract team information
                    String teamName = standingData.get("team_name").getAsString();
                    int teamApiId = standingData.get("team_id").getAsInt();

                    // Ensure team exists in database
                    int teamDbId = ensureTeamExists(
                            teamName,
                            teamApiId,
                            teamService,
                            tournament.getId(),
                            standingData.get("team_badge").getAsString()
                    );

                    // Check if ranking exists for this specific team and tournament
                    TeamRanking existingRanking = rankingService.getRankingByTeamAndTournament(
                            teamDbId,
                            tournament.getId()
                    );

                    if (existingRanking == null) {
                        processNewRanking(standingData, teamDbId, tournament, rankingService);
                    } else {
                        updateExistingRanking(existingRanking, standingData, rankingService);
                    }
                }
                System.out.println("Standings processed successfully for league: " + leagueName);
            }
        } catch (Exception e) {
            System.err.println("Error processing standings for " + leagueName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processNewRanking(JsonObject standingData, int teamId, Tournois tournament, TeamRankingService service) {
        try {
            TeamRanking ranking = new TeamRanking();
            ranking.setIdTeam(teamId);
            ranking.setIdTournoi(tournament.getId());
            updateRankingFromJson(ranking, standingData);
            service.insert(ranking);
        } catch (SQLException e) {
            System.err.println("Error creating new ranking: " + e.getMessage());
        }
    }

    private void updateExistingRanking(TeamRanking existingRanking, JsonObject standingData, TeamRankingService service) {
        try {
            updateRankingFromJson(existingRanking, standingData);
            service.update(existingRanking);
        } catch (SQLException e) {
            System.err.println("Error updating ranking: " + e.getMessage());
        }
    }
    private void updateRankingFromJson(TeamRanking ranking, JsonObject data) {
        ranking.setPosition(data.get("overall_league_position").getAsInt());
        ranking.setWins(data.get("overall_league_W").getAsInt());
        ranking.setDraws(data.get("overall_league_D").getAsInt());
        ranking.setLosses(data.get("overall_league_L").getAsInt());
        ranking.setPoints(data.get("overall_league_PTS").getAsInt());

        int goalsFor = data.get("overall_league_GF").getAsInt();
        int goalsAgainst = data.get("overall_league_GA").getAsInt();
        ranking.setGoalsScored(goalsFor);
        ranking.setGoalsConceded(goalsAgainst);
        ranking.setGoalDifference(goalsFor - goalsAgainst);
    }
    private void fetchAndAddPlayersToSQL(String teamName, int idteamapi, int idTeam) {
        // Construct the API URL for fetching teams using the team ID
        String apiUrl = "https://" + API_HOST + "/?action=get_teams&team_id=" + idteamapi+"&season"+season+ "&APIkey=" + API_KEY;

        try {
            // Make API request
            HttpResponse<String> response = makeApiRequest(apiUrl);

            if (response.statusCode() == 200) {
                // Parse JSON response
                JsonElement responseElement = JsonParser.parseString(response.body());
                System.out.println("API Response: " + responseElement); // Debugging

                // Check if the response is an error object
                if (responseElement.isJsonObject()) {
                    JsonObject responseObject = responseElement.getAsJsonObject();
                    if (responseObject.has("error")) {
                        int errorCode = responseObject.get("error").getAsInt();
                        String errorMessage = responseObject.get("message").getAsString();
                        System.err.println("API Error: " + errorCode + " - " + errorMessage);
                        return; // Exit the method if there's an error
                    }
                }

                // Check if the response is a JSON array
                if (!responseElement.isJsonArray()) {
                    System.err.println("Unexpected response format: " + responseElement);
                    return;
                }

                // Parse teams response
                JsonArray teamsArray = responseElement.getAsJsonArray();

                // Initialize the service for creating players
                UserService userService = new UserService();

                // Iterate through the teams array
                for (JsonElement teamElement : teamsArray) {
                    JsonObject teamObject = teamElement.getAsJsonObject();

                    // Check if the team has players
                    if (teamObject.has("players")) {
                        JsonArray playersArray = teamObject.getAsJsonArray("players");

                        // Iterate through the players array
                        for (JsonElement playerElement : playersArray) {
                            JsonObject playerObject = playerElement.getAsJsonObject();

                            // Extract player details
                            String playerName = playerObject.get("player_name").getAsString();
                            String[] nameParts = playerName.split(" ", 2); // Split into first name and last name
                            String playerFirstName = nameParts.length > 0 ? nameParts[0] : "";
                            String playerLastName = nameParts.length > 1 ? nameParts[1] : "";

                            String photoUrl = playerObject.get("player_image").getAsString(); // Extract the photo URL
                            String playerType = playerObject.get("player_type").getAsString(); // Extract the player type

                            // Handle date of birth
                            LocalDate dateOfBirth = null;
                            String birthdateString = playerObject.get("player_birthdate").getAsString();
                            if (birthdateString != null && !birthdateString.isEmpty()) {
                                dateOfBirth = LocalDate.parse(birthdateString); // Convert to LocalDate
                            }

                            // Create a new Player object
                            Player player = new Player();
                            player.setFirstname(playerFirstName);
                            player.setLastName(playerLastName);
                            player.setDateOfBirth(dateOfBirth); // Can be null
                            player.setRole("player");
                            player.setIdteam(idTeam);
                            player.setPosition(playerType);

                            try {
                                // Download the photo to a temporary file
                                Path tempFilePath = Files.createTempFile("player_photo_", ".jpg");
                                downloadPhoto(photoUrl, tempFilePath);

                                // Save the downloaded photo using the saveUploadedFile method
                                File tempFile = tempFilePath.toFile();
                                String savedFilePath = saveUploadedFile(tempFile,"player");

                                if (savedFilePath != null) {
                                    System.out.println("Photo saved: " + savedFilePath);
                                    player.setProfilePicture(savedFilePath); // Set the photo path in the Player object
                                } else {
                                    System.err.println("Failed to save photo for player: " + playerFirstName + " " + playerLastName);
                                }

                                // Delete the temporary file
                                Files.deleteIfExists(tempFilePath);
                            } catch (IOException e) {
                                System.err.println("Failed to download or save photo: " + e.getMessage());
                            }

                            // Insert the player into the database
                            userService.createPlayer(player);
                        }
                    } else {
                        System.err.println("No players found for team: " + teamName);
                    }
                }

                System.out.println("Players added to SQL successfully for team: " + teamName);
            } else {
                // Log failure with status code and response body
                System.err.println("Failed to fetch teams. Status code: " + response.statusCode() + ", Response: " + response.body());
            }
        } catch (Exception e) {
            // Log any exceptions that occur during the API request or database operations
            System.err.println("Error fetching or adding players: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void downloadPhoto(String photoUrl, Path destinationPath) throws IOException {
        URL url = new URL(photoUrl);
        try (InputStream in = url.openStream()) {
            Files.copy(in, destinationPath, StandardCopyOption.REPLACE_EXISTING); // Save the file
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
                    Path tempFilePath = Files.createTempFile("team_photo_", ".jpg");
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
    private void fetchTeamsForLeague(int leagueId) {
        try {
            // Construct the API URL for fetching fixtures in the selected league and date range
            String fixturesUrl = "https://" + API_HOST + "/?action=get_events&from="+date+"&to="+date2+"&league_id=" + leagueId + "&APIkey=" + API_KEY;

            // Fetch fixtures
            HttpResponse<String> fixturesResponse = makeApiRequest(fixturesUrl);
            if (fixturesResponse.statusCode() != 200) {
                System.err.println("Failed to fetch fixtures. Status code: " + fixturesResponse.statusCode() + ", Response: " + fixturesResponse.body());
                return;
            }

            // Print the API response for debugging
            System.out.println("API Response: " + fixturesResponse.body());

            // Parse the response
            JsonElement responseElement = JsonParser.parseString(fixturesResponse.body());

            // Check if the response is an error object
            if (responseElement.isJsonObject()) {
                JsonObject responseObject = responseElement.getAsJsonObject();
                if (responseObject.has("error")) {
                    int errorCode = responseObject.get("error").getAsInt();
                    String errorMessage = responseObject.get("message").getAsString();
                    System.err.println("API Error: " + errorCode + " - " + errorMessage);
                    return; // Exit the method if there's an error
                }
            }

            // Check if the response is a JSON array
            if (!responseElement.isJsonArray()) {
                System.err.println("Unexpected response format: " + responseElement);
                return;
            }

            // Parse fixtures response and extract unique team IDs with fixtures
            JsonArray fixturesArray = responseElement.getAsJsonArray();
            Map<Integer, String> teamsMap = new HashMap<>(); // Store team ID and team name

            for (JsonElement fixtureElement : fixturesArray) {
                JsonObject fixtureObject = fixtureElement.getAsJsonObject();

                // Debug: Print the fixture object
                System.out.println("Fixture Object: " + fixtureObject);

                // Check if required fields exist in the fixture object
                if (fixtureObject.has("match_hometeam_id") && fixtureObject.has("match_hometeam_name") &&
                        fixtureObject.has("match_awayteam_id") && fixtureObject.has("match_awayteam_name")) {

                    int homeTeamId = fixtureObject.get("match_hometeam_id").getAsInt(); // Correct field name
                    String homeTeamName = fixtureObject.get("match_hometeam_name").getAsString();
                    int awayTeamId = fixtureObject.get("match_awayteam_id").getAsInt(); // Correct field name
                    String awayTeamName = fixtureObject.get("match_awayteam_name").getAsString();

                    // Add teams to the map
                    teamsMap.put(homeTeamId, homeTeamName);
                    teamsMap.put(awayTeamId, awayTeamName);
                } else {
                    System.err.println("Fixture object is missing required fields: match_hometeam_id, match_hometeam_name, match_awayteam_id, or match_awayteam_name");
                }
            }

            // If no teams are found, log and return
            if (teamsMap.isEmpty()) {
                System.out.println("No teams with fixtures found for the given date range and league.");
                return;
            }

            // Clear the ComboBox and add teams
            TeamComboBox.getItems().clear();
            for (Map.Entry<Integer, String> entry : teamsMap.entrySet()) {
                TeamComboBox.getItems().add(entry.getValue()); // Add team name to ComboBox
                teamIdMap.put(entry.getValue(), entry.getKey()); // Store team name and ID in the map
            }

            // Log success
            System.out.println("Successfully fetched and populated teams with fixtures for league ID: " + leagueId);
        } catch (Exception e) {
            // Log any exceptions that occur during the API request
            System.err.println("Error fetching teams: " + e.getMessage());
            e.printStackTrace();
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

    private <T extends Node & Validated> Node wrapNodeForValidation(T node) {
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setManaged(false);

        stepper.addEventHandler(MFXStepperEvent.VALIDATION_FAILED_EVENT, event -> {
            MFXValidator validator = node.getValidator();
            List<Constraint> validate = validator.validate();
            if (!validate.isEmpty()) {
                errorLabel.setText(validate.get(0).getMessage());
            }
        });

        stepper.addEventHandler(MFXStepperEvent.NEXT_EVENT, event -> errorLabel.setText(""));

        VBox wrap = new VBox(3, node, errorLabel);
        wrap.setAlignment(Pos.CENTER);
        return wrap;
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
    private Node createGrid() {
        // Header label for the team creation
        MFXTextField selectedTeamLabel = MFXTextField.asLabel("Selected Team:");
        selectedTeamLabel.getStyleClass().add("header-label");

        // Team Name
        MFXTextField teamNameLabel = createLabel("Team Name: ");
        MFXTextField teamNameValueLabel = createLabel("");
        teamNameValueLabel.textProperty().bind(nameField.textProperty());

        // Category
        MFXTextField categoryLabel = createLabel("Category: ");
        MFXTextField categoryValueLabel = createLabel("");
        categoryValueLabel.textProperty().bind(categoryField.textProperty());

        // Number of Players
        MFXTextField nbPlayersLabel = createLabel("Number of Players: ");
        MFXTextField nbPlayersValueLabel = createLabel("");
        nbPlayersValueLabel.textProperty().bind(nbPlayersField.textProperty());

        // Game Mode
        MFXTextField gameModeLabel = createLabel("Game Mode: ");
        MFXTextField gameModeValueLabel = createLabel("");
        gameModeValueLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> modeJeuComboBox.getValue() != null ? String.valueOf(modeJeuComboBox.getValue()) : "",
                        modeJeuComboBox.valueProperty()
                )
        );

        // Add styles to labels
        teamNameLabel.getStyleClass().add("header-label");
        categoryLabel.getStyleClass().add("header-label");
        nbPlayersLabel.getStyleClass().add("header-label");
        gameModeLabel.getStyleClass().add("header-label");

        // Selected Players
        MFXTextField selectedPlayersLabel = MFXTextField.asLabel("Selected Players:");
        selectedPlayersLabel.getStyleClass().add("header-label");

        VBox selectedPlayersBox = new VBox(5);
        selectedPlayersBox.setAlignment(Pos.CENTER_LEFT);

        // Update selected players when moving to the next step
        stepper.setOnBeforeNext(event -> {
            if (stepper.getCurrentIndex() == 2) { // Step 3 is index 2
                selectedPlayersBox.getChildren().clear();

                if (checkedPlayers != null) {
                    for (User player : checkedPlayers) {
                        MFXTextField playerLabel = MFXTextField.asLabel(player.getFirstname() + " " + player.getLastName());
                        selectedPlayersBox.getChildren().add(playerLabel);
                    }
                }
            }
        });

        // Completed label
        MFXTextField completedLabel = MFXTextField.asLabel("Completed!");
        completedLabel.getStyleClass().add("completed-label");

        // Create HBox containers for labels and values
        HBox teamNameBox = createHBox(teamNameLabel, teamNameValueLabel);
        HBox categoryBox = createHBox(categoryLabel, categoryValueLabel);
        HBox nbPlayersBox = createHBox(nbPlayersLabel, nbPlayersValueLabel);
        HBox gameModeBox = createHBox(gameModeLabel, gameModeValueLabel);

        // Main VBox container
        VBox box = new VBox(10, selectedTeamLabel, teamNameBox, categoryBox, nbPlayersBox, gameModeBox, selectedPlayersLabel, selectedPlayersBox, checkbox);
        box.setAlignment(Pos.CENTER);
        StackPane.setAlignment(box, Pos.CENTER);

        // Handle last step (completion)
        stepper.setOnLastNext(event -> {
            box.getChildren().setAll(completedLabel);
            stepper.setMouseTransparent(true);
        });

        // Handle going back from the last step
        stepper.setOnBeforePrevious(event -> {
            if (stepper.isLastToggle()) {
                checkbox.setSelected(false);
                box.getChildren().setAll(selectedTeamLabel, teamNameBox, categoryBox, nbPlayersBox, gameModeBox, selectedPlayersLabel, selectedPlayersBox, checkbox);
            }
        });

        return box;
    }
    private Node createGrid2() {
        // Header label for the team creation
        MFXTextField selectedTeamLabel = MFXTextField.asLabel("Selected Team:");
        selectedTeamLabel.getStyleClass().add("header-label");

        // Team Name
        MFXTextField LeagueNameLabel = createLabel("League Name: ");
        MFXTextField LeagueValueLabel = createLabel("");
        LeagueValueLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> LeagueComboBox.getValue() != null ? String.valueOf(LeagueComboBox.getValue()) : "",
                        LeagueComboBox.valueProperty()
                )
        );

        // Game Mode
        MFXTextField TeamLabel = createLabel("Team Name:");
        MFXTextField TeamValueLabel = createLabel("");
        TeamValueLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> TeamComboBox.getValue() != null ? String.valueOf(TeamComboBox.getValue()) : "",
                        TeamComboBox.valueProperty()
                )
        );

        // Add styles to labels
        LeagueNameLabel.getStyleClass().add("header-label");
        TeamLabel.getStyleClass().add("header-label");

        // Selected Players
        MFXTextField selectedPlayersLabel = MFXTextField.asLabel("Selected Players:");
        selectedPlayersLabel.getStyleClass().add("header-label");

        VBox selectedPlayersBox = new VBox(5);
        selectedPlayersBox.setAlignment(Pos.CENTER_LEFT);
        // Completed label
        MFXTextField completedLabel = MFXTextField.asLabel("Completed!");
        completedLabel.getStyleClass().add("completed-label");

        // Create HBox containers for labels and values
        HBox LeagueBox = createHBox(LeagueNameLabel, LeagueValueLabel);
        HBox TeamBox = createHBox(TeamLabel, TeamValueLabel);

        // Main VBox container
        VBox box = new VBox(10, LeagueBox, TeamBox, checkbox2);
        box.setAlignment(Pos.CENTER);
        StackPane.setAlignment(box, Pos.CENTER);

        // Handle last step (completion)
        stepper.setOnLastNext(event -> {
            box.getChildren().setAll(completedLabel);
            stepper.setMouseTransparent(false);
        });

        // Handle going back from the last step
        stepper.setOnBeforePrevious(event -> {
            if (stepper.isLastToggle()) {
                checkbox2.setSelected(false);
                box.getChildren().setAll(selectedTeamLabel,  LeagueBox, TeamBox, checkbox2);
            }
        });

        return box;
    }

    // Helper method to create an HBox with consistent styling
    private HBox createHBox(Node... children) {
        HBox hbox = new HBox(children);
        hbox.setMaxWidth(Region.USE_PREF_SIZE);
        return hbox;
    }

    private MFXTextField createLabel(String text) {
        MFXTextField label = MFXTextField.asLabel(text);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setPrefWidth(200);
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setMaxWidth(Region.USE_PREF_SIZE);
        return label;
    }
}