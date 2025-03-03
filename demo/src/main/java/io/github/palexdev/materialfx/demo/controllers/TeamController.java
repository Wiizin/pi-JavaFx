package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.demo.model.ModeJeu;
import io.github.palexdev.materialfx.demo.model.Team;
import io.github.palexdev.materialfx.demo.model.TeamRanking;
import io.github.palexdev.materialfx.demo.services.TeamRankingService;
import io.github.palexdev.materialfx.demo.services.TeamService;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import io.github.palexdev.materialfx.filter.EnumFilter;
import io.github.palexdev.materialfx.filter.IntegerFilter;
import io.github.palexdev.materialfx.filter.StringFilter;
import io.github.palexdev.materialfx.utils.others.observables.When;
import io.github.palexdev.materialfx.filter.EnumFilter;
import io.github.palexdev.materialfx.filter.base.AbstractFilter;


import io.github.palexdev.mfxcore.controls.Label;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;// Import MFXHtmlFlow
import javafx.scene.paint.Color;
import io.github.palexdev.materialfx.validation.Constraint;
import io.github.palexdev.materialfx.validation.MFXValidator;
import io.github.palexdev.materialfx.validation.Validated;
import javafx.beans.binding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;
public class TeamController implements Initializable {
    @FXML
    private MFXPaginatedTableView<Team> paginated;
    @FXML
    private MFXPaginatedTableView<TeamRanking> paginated2;
    @FXML
    private GridPane grid;
    private boolean isListenerRegistered = false;
    private TeamService teamService = new TeamService();
    private TeamRankingService teamRankingService = new TeamRankingService();
    // ObservableList to hold the teams
    private ObservableList<Team> teams = FXCollections.observableArrayList();
    //ObservableList to hold the ranking
    private ObservableList<TeamRanking> teamsRanking = FXCollections.observableArrayList();
    private static final Logger logger = LoggerFactory.getLogger(TeamService.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupPaginated();
        paginated.autosizeColumnsOnInitialization();
        paginated.setPrefSize(900, 400);
        // Fetch data from the database and populate the table
        try {
            teams.setAll(teamService.showAll()); // Fetch and set data
        } catch (SQLException e) {
            logger.error("An error occurred while showing the teams: " + e.getMessage());
        }
        When.onChanged(paginated.currentPageProperty())
                .then((oldValue, newValue) -> paginated.autosizeColumns())
                .listen();
        paginated.setItems(teams);

    }
    private void initializePaginated2() {
        if (paginated2 == null) {
            paginated2 = new MFXPaginatedTableView<>();
            setupPaginated2();
            paginated2.autosizeColumnsOnInitialization();
            paginated2.setPrefSize(600, 400); // Set a fixed size for the table
            // Add paginated2 to the layout if needed
            grid.add(paginated2, 0, 1); // Example: Add to a GridPane
        }
    }

    private void setupPaginated() {
        MFXTableColumn<Team> id = new MFXTableColumn<>("ID", false, Comparator.comparing(Team::getId));
        id.setAlignment(Pos.CENTER);
        id.setFont(Font.font("System", FontWeight.BOLD, 12));

        MFXTableColumn<Team> logo = new MFXTableColumn<>("Logo", false, Comparator.comparing(Team::getLogoPath));
        logo.setAlignment(Pos.CENTER);
        logo.setFont(Font.font("System", FontWeight.BOLD, 12));

        MFXTableColumn<Team> name = new MFXTableColumn<>("Name", false, Comparator.comparing(Team::getNom));
        name.setAlignment(Pos.CENTER);
        name.setFont(Font.font("System", FontWeight.BOLD, 12));
        MFXTableColumn<Team> categoire = new MFXTableColumn<>("categoire", false, Comparator.comparing(Team::getCategorie));
        categoire.setAlignment(Pos.CENTER);
        categoire.setFont(Font.font("System", FontWeight.BOLD, 12));
        MFXTableColumn<Team> NBPlayers = new MFXTableColumn<>("NBPlayers", false, Comparator.comparing(Team::getNombreJoueurs));
        NBPlayers.setAlignment(Pos.CENTER);
        NBPlayers.setFont(Font.font("System", FontWeight.BOLD, 12));
        MFXTableColumn<Team> ModeJeu = new MFXTableColumn<>("ModeJeu", false, Comparator.comparing(Team::getModeJeu));
        ModeJeu.setAlignment(Pos.CENTER);
        ModeJeu.setFont(Font.font("System", FontWeight.BOLD, 12));
        // Add columns for actions (Modify and Delete)
        MFXTableColumn<Team> Modify = new MFXTableColumn<>("Modify", false, Comparator.comparing(Team::getId));
        Modify.setAlignment(Pos.CENTER);
        Modify.setFont(Font.font("System", FontWeight.BOLD, 12));
        MFXTableColumn<Team> Delete = new MFXTableColumn<>("Delete", false, Comparator.comparing(Team::getId));
        Delete.setAlignment(Pos.CENTER);
        Delete.setFont(Font.font("System", FontWeight.BOLD, 12));
        MFXTableColumn<Team> Ranking = new MFXTableColumn<>("Ranking", false, Comparator.comparing(Team::getId));
        Ranking.setAlignment(Pos.CENTER);
        Ranking.setFont(Font.font("System", FontWeight.BOLD, 12));
        // Set row cell factories for data columns
        id.setRowCellFactory(team -> new MFXTableRowCell<>(Team::getId){{
            setAlignment(Pos.CENTER);
        }});
        // Set row cell factory for the logo column
        logo.setRowCellFactory(team -> {
            MFXTableRowCell<Team, Void> cell = new MFXTableRowCell<>(null); // Use String instead of Void

            // Create an ImageView to display the logo
            ImageView logoView = new ImageView();
            logoView.setFitWidth(50); // Set appropriate width
            logoView.setFitHeight(50); // Set appropriate height
            logoView.setPreserveRatio(true);

            // Check if the team has a valid logo path
            String logoPath = team.getLogoPath();
            if (logoPath != null && !logoPath.trim().isEmpty()) {
                File logoFile = new File(logoPath);
                //System.out.println("Loading logo from: " + logoFile.getAbsolutePath());

                if (logoFile.exists() && logoFile.isFile()) {
                    try {
                        Image logoImage = new Image(logoFile.toURI().toString());
                        logoView.setImage(logoImage);
                    } catch (Exception e) {
                        System.err.println("Failed to load logo: " + e.getMessage());
                    }
                } else {
                    System.err.println("Logo file does not exist or is not a valid file: " + logoFile.getAbsolutePath());
                }
            } else {
                System.err.println("Invalid logo path: " + logoPath);
            }

            // Add the ImageView to the cell
            cell.setGraphic(logoView);
            cell.setAlignment(Pos.CENTER);
            return cell;
        });


        name.setRowCellFactory(team -> new MFXTableRowCell<>(Team::getNom){{
            setAlignment(Pos.CENTER);
        }});
        categoire.setRowCellFactory(team -> new MFXTableRowCell<>(Team::getCategorie) {{
            setAlignment(Pos.CENTER);
        }});
        NBPlayers.setRowCellFactory(team -> new MFXTableRowCell<>(Team::getNombreJoueurs){{
            setAlignment(Pos.CENTER);
        }});
        ModeJeu.setRowCellFactory(team -> new MFXTableRowCell<>(Team::getModeJeu){{
            setAlignment(Pos.CENTER);
        }});
        //set row cell factory for the ranking column
        Ranking.setRowCellFactory(team -> {
            MFXTableRowCell<Team, Void> cell = new MFXTableRowCell<>(null);

            // Create Modify button
            MFXButton rankingButton = new MFXButton("Consult");
            rankingButton.setStyle("-fx-background-color: royalblue; -fx-text-fill: white;");
            rankingButton.setOnAction(event -> {
                Team selectedTeam = team; // Use the 'team' parameter directly
                RankingTeam(selectedTeam); // Call method to handle modification
            });

            // Add button to the cell
            cell.setGraphic(rankingButton);
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        // Set row cell factory for the Modify column
        Modify.setRowCellFactory(team -> {
            MFXTableRowCell<Team, Void> cell = new MFXTableRowCell<>(null);

            // Create Modify button
            MFXButton modifyButton = new MFXButton("Modify");
            modifyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            modifyButton.setOnAction(event -> {
                Team selectedTeam = team; // Use the 'team' parameter directly
                modifyTeam(selectedTeam); // Call method to handle modification
            });

            // Add button to the cell
            cell.setGraphic(modifyButton);
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        // Set row cell factory for the Delete column
        Delete.setRowCellFactory(team -> {
            MFXTableRowCell<Team, Void> cell = new MFXTableRowCell<>(null);

            // Create Delete button
            MFXButton deleteButton = new MFXButton("Delete");
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            deleteButton.setOnAction(event -> {
                Team selectedTeam = team; // Use the 'team' parameter directly
                deleteTeam(selectedTeam); // Call method to handle deletion
            });

            // Add button to the cell
            cell.setGraphic(deleteButton);
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        // Add columns to the table
        paginated.getTableColumns().addAll(id,logo,name, categoire, NBPlayers, ModeJeu,Ranking,Modify, Delete);

        // Add filters
        paginated.getFilters().addAll(
            new IntegerFilter<>("ID", Team::getId),
            new StringFilter<>("Name", Team::getNom),
            new StringFilter<>("Categorie", Team::getCategorie), 
            new IntegerFilter<>("NBPlayers", Team::getNombreJoueurs),
            new EnumFilter<>("ModeJeu", Team::getModeJeu, ModeJeu.class)
        );

        // Create a FilteredList to handle the filtering
        FilteredList<Team> filteredData = new FilteredList<>(teams);
        // Add a listener to the filters collection
        paginated.getFilters().addListener((ListChangeListener<Object>) change -> {
            filteredData.setPredicate(team -> {
                if (paginated.getFilters().isEmpty()) {
                    return true;
                }
                try {
                for (var filter : paginated.getFilters()) {
                    if (!((Predicate<Team>) filter).test(team)) {
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
    private void setupPaginated2() {
        MFXTableColumn<TeamRanking> id = new MFXTableColumn<>("ID", false, Comparator.comparing(TeamRanking::getId));
        id.setAlignment(Pos.CENTER);
        id.setFont(Font.font("System", FontWeight.BOLD, 12)); // Set bold font
        MFXTableColumn<TeamRanking> teamName = new MFXTableColumn<>("Team Name", false,
                Comparator.comparing(TeamRanking -> TeamRanking.getTeam().getNom()));

        teamName.setAlignment(Pos.CENTER);
        teamName.setFont(Font.font("System", FontWeight.BOLD, 12)); // Set bold font
        MFXTableColumn<TeamRanking> position = new MFXTableColumn<>("position", false, Comparator.comparing(TeamRanking::getPosition));
        position.setAlignment(Pos.CENTER);
        position.setFont(Font.font("System", FontWeight.BOLD, 12)); // Set bold font
        MFXTableColumn<TeamRanking> points = new MFXTableColumn<>("points", false, Comparator.comparing(TeamRanking::getPoints));
        points.setAlignment(Pos.CENTER);
        points.setFont(Font.font("System", FontWeight.BOLD, 12)); // Set bold font
        MFXTableColumn<TeamRanking> wins = new MFXTableColumn<>("wins", false, Comparator.comparing(TeamRanking::getWins));
        wins.setAlignment(Pos.CENTER);
        wins.setFont(Font.font("System", FontWeight.BOLD, 12)); // Set bold font
        MFXTableColumn<TeamRanking> draws = new MFXTableColumn<>("draws", false, Comparator.comparing(TeamRanking::getDraws));
        draws.setAlignment(Pos.CENTER);
        draws.setFont(Font.font("System", FontWeight.BOLD, 12)); // Set bold font
        MFXTableColumn<TeamRanking> losses = new MFXTableColumn<>("losses", false, Comparator.comparing(TeamRanking::getLosses));
        losses.setAlignment(Pos.CENTER);
        losses.setFont(Font.font("System", FontWeight.BOLD, 12)); // Set bold font
        MFXTableColumn<TeamRanking> goals_Scored = new MFXTableColumn<>("goals_Scored", false, Comparator.comparing(TeamRanking::getGoalsScored));
        goals_Scored.setAlignment(Pos.CENTER);
        goals_Scored.setFont(Font.font("System", FontWeight.BOLD, 12)); // Set bold font
        MFXTableColumn<TeamRanking> goals_conceded = new MFXTableColumn<>("goals_conceded", false, Comparator.comparing(TeamRanking::getGoalsConceded));
        goals_conceded.setAlignment(Pos.CENTER);
        goals_conceded.setFont(Font.font("System", FontWeight.BOLD, 12)); // Set bold font
        MFXTableColumn<TeamRanking> goals_difference = new MFXTableColumn<>("goals_difference", false, Comparator.comparing(TeamRanking::getGoalDifference));
        goals_difference.setAlignment(Pos.CENTER);
        goals_difference.setFont(Font.font("System", FontWeight.BOLD, 12)); // Set bold font

        // Set row cell factories for data columns
        id.setRowCellFactory(teamRank -> new MFXTableRowCell<>(TeamRanking::getId){{
            setAlignment(Pos.CENTER);
        }});
        teamName.setRowCellFactory(teamRank -> {
            MFXTableRowCell<TeamRanking, Void> cell = new MFXTableRowCell<>(null);
            // Create an HBox to hold the logo and team name
            HBox content = new HBox(10); // 10 is the spacing between the logo and the name
            content.setAlignment(Pos.CENTER_LEFT); // Align content to the left

            // Create an ImageView to display the logo
            ImageView logoView = new ImageView();
            logoView.setFitWidth(30); // Set appropriate width
            logoView.setFitHeight(30); // Set appropriate height
            logoView.setPreserveRatio(true);

            // Check if the team has a valid logo path
            String logoPath = teamRank.getTeam().getLogoPath();
            if (logoPath != null && !logoPath.trim().isEmpty()) {
                File logoFile = new File(logoPath);
                if (logoFile.exists() && logoFile.isFile()) {
                    try {
                        Image logoImage = new Image(logoFile.toURI().toString());
                        logoView.setImage(logoImage);
                    } catch (Exception e) {
                        System.err.println("Failed to load logo: " + e.getMessage());
                    }
                } else {
                    System.err.println("Logo file does not exist or is not a valid file: " + logoFile.getAbsolutePath());
                }
            } else {
                System.err.println("Invalid logo path: " + logoPath);
            }

            // Add the logo and team name to the HBox
            content.getChildren().addAll(logoView, new Label(teamRank.getTeam().getNom()));

            // Set the HBox as the graphic of the cell
            cell.setGraphic(content);
            cell.setAlignment(Pos.CENTER_LEFT); // Align the cell content to the left
            return cell;
        });
        position.setRowCellFactory(teamRank -> new MFXTableRowCell<>(TeamRanking::getPosition){{
            setAlignment(Pos.CENTER);
        }});
        points.setRowCellFactory(teamRank -> new MFXTableRowCell<>(TeamRanking::getPoints) {{
            setAlignment(Pos.CENTER);
        }});
        wins.setRowCellFactory(teamRank -> new MFXTableRowCell<>(TeamRanking::getWins){{
            setAlignment(Pos.CENTER);
        }});
        draws.setRowCellFactory(teamRank -> new MFXTableRowCell<>(TeamRanking::getDraws){{
            setAlignment(Pos.CENTER);
        }});
        losses.setRowCellFactory(teamRank -> new MFXTableRowCell<>(TeamRanking::getLosses){{
            setAlignment(Pos.CENTER);
        }});
        goals_Scored.setRowCellFactory(teamRank -> new MFXTableRowCell<>(TeamRanking::getGoalsScored){{
            setAlignment(Pos.CENTER);
        }});
        goals_conceded.setRowCellFactory(teamRank -> new MFXTableRowCell<>(TeamRanking::getGoalsConceded){{
            setAlignment(Pos.CENTER);
        }});
        goals_difference.setRowCellFactory(teamRank -> new MFXTableRowCell<>(TeamRanking::getGoalDifference){{
            setAlignment(Pos.CENTER);
        }});

        // Add columns to the table
        paginated2.getTableColumns().addAll(id, teamName, position, points, wins,draws,losses, goals_Scored, goals_conceded, goals_difference);

        // Add filters
        paginated2.getFilters().addAll(
                new IntegerFilter<>("ID", TeamRanking::getId),
                new StringFilter<>("teamName", teamRanking -> teamRanking.getTeam().getNom()),
                new IntegerFilter<>("position", TeamRanking::getPosition),
                new IntegerFilter<>("Points", TeamRanking::getPoints),
                new IntegerFilter<>("wins", TeamRanking::getWins),
                new IntegerFilter<>("draws", TeamRanking::getDraws),
                new IntegerFilter<>("losses", TeamRanking::getLosses),
                new IntegerFilter<>("Goals_Scored", TeamRanking::getGoalsScored),
                new IntegerFilter<>("Goals_Conceded", TeamRanking::getGoalsConceded),
                new IntegerFilter<>("Goals_Difference", TeamRanking::getGoalDifference)
        );
        // Create a FilteredList to handle the filtering
        FilteredList<TeamRanking> filteredData = new FilteredList<>(teamsRanking);
        paginated2.setItems(filteredData);
        
        // Add a listener to the filters collection
        paginated2.getFilters().addListener((ListChangeListener<Object>) change -> {
            filteredData.setPredicate(teamRank -> {
                if (paginated2.getFilters().isEmpty()) {
                    return true;
                }
                
                try {
                    for (var filter : paginated2.getFilters()) {
                        if (!((Predicate<TeamRanking>) filter).test(teamRank)) {
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
    @FXML
    private void showAddTeamDialog(javafx.event.ActionEvent event) {
        // Create a dialog
        MFXGenericDialog dialogContent = MFXGenericDialogBuilder.build()
                .setHeaderText("Add New Team")
                .makeScrollable(true)
                .get();
        // Create form fields
        final MFXTextField nameField = new MFXTextField();
        nameField.setFloatingText("Team Name");
        nameField.getStyleClass().add("custom-text-field");
        nameField.setStyle("-fx-pref-width: 450;\n" +
                "    -fx-border-radius: 10;\n" +
                "    -fx-start-margin:  75;");


        MFXTextField categoryField = new MFXTextField();
        categoryField.setFloatingText("Category");
        categoryField.setStyle("-fx-pref-width: 450;\n" +
                "    -fx-border-radius: 10;\n" +
                "    -fx-padding: 5;");

        MFXTextField nbPlayersField = new MFXTextField();
        nbPlayersField.setFloatingText("Number of Players");
        nbPlayersField.setStyle("-fx-pref-width: 450;\n" +
                "    -fx-border-radius: 10;\n" +
                "    -fx-padding: 5;");

        MFXComboBox<ModeJeu> modeJeuComboBox = new MFXComboBox<>();
        modeJeuComboBox.setFloatingText("Game Mode");
        modeJeuComboBox.setItems(FXCollections.observableArrayList(ModeJeu.values()));
        modeJeuComboBox.setStyle("-fx-pref-width: 450;\n" +
                "    -fx-border-radius: 10;\n" +
                "    -fx-padding: 5;");

        // Add a file upload field
        MFXButton uploadButton = new MFXButton("Upload Logo");
        uploadButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        Label fileLabel = new Label("No file selected");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        fileLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px; -fx-font-family: 'Roboto';");
        uploadButton.setOnAction(e -> {
            File selectedFile = fileChooser.showOpenDialog(dialogContent.getScene().getWindow());
            if (selectedFile != null) {
                fileLabel.setText(selectedFile.getAbsolutePath());
            }
        });

        // Add validators to the dialog
        nameField.getValidator().constraint("Team name is required", nameField.textProperty().length().greaterThanOrEqualTo(1));
        categoryField.getValidator().constraint("Category is required", categoryField.textProperty().length().greaterThanOrEqualTo(1));
        nbPlayersField.getValidator().constraint("Number of players is required", nbPlayersField.textProperty().length().greaterThanOrEqualTo(1));
        modeJeuComboBox.getValidator().constraint("Game mode is required", modeJeuComboBox.valueProperty().isNotNull());
        VBox form = new VBox(10,
                wrapNodeForValidation(nameField),
                wrapNodeForValidation(categoryField),
                wrapNodeForValidation(nbPlayersField),
                wrapNodeForValidation(modeJeuComboBox),
                uploadButton,
                fileLabel );

        dialogContent.setContent(form);

        // Add buttons to the dialog
        MFXButton saveButton = new MFXButton("Save");
        saveButton.getStyleClass().add("custom-button");
        saveButton.setOnAction(e -> {
            // Validate fields
            boolean allValid = true;

            // Loop through all form fields and validate
            for (Node node : form.getChildren()) {
                if (node instanceof VBox vbox) {
                    Node fieldNode = vbox.getChildren().get(0); // First child is the field
                    if (fieldNode instanceof Validated validatedNode) {
                        MFXValidator validator = validatedNode.getValidator();
                        List<Constraint> validate = validator.validate(); // Trigger validation

                        // Retrieve the error label from the node's properties
                        Label errorLabel = (Label) fieldNode.getProperties().get("errorLabel");

                        if (!validate.isEmpty()) {
                            // Show error message
                            errorLabel.setText(validate.get(0).getMessage());
                            errorLabel.setVisible(true);
                            errorLabel.setManaged(true);
                            allValid = false;
                        } else {
                            // Hide error message if valid
                            errorLabel.setText("");
                            errorLabel.setVisible(false);
                            errorLabel.setManaged(false);
                        }
                    }
                }
            }

            if (!allValid) {
                System.out.println("Validation failed! Errors are now visible.");
                return;
            }

            try {
                int nbPlayers = Integer.parseInt(nbPlayersField.getText());
                if (nbPlayers <= 0) {
                 //   showAlert("Validation Error", "Number of players must be greater than 0.");
                    return;
                }
            } catch (NumberFormatException ex) {
               // showAlert("Validation Error", "Number of players must be a valid integer.");
                return;
            }
            // Handle file upload
            String logoPath = null;
            if (fileLabel.getText() != null && !fileLabel.getText().equals("No file selected")) {
                File selectedFile = new File(fileLabel.getText());
                if (selectedFile.exists()) {
                    logoPath = saveUploadedFile(selectedFile); // Save the file and get its path
                } else {
                    System.err.println("Selected file does not exist: " + selectedFile);
                }
            }

            Team newTeam = new Team();
            newTeam.setNom(nameField.getText());
            newTeam.setCategorie(categoryField.getText());
            newTeam.setNombreJoueurs(Integer.parseInt(nbPlayersField.getText()));
            newTeam.setModeJeu(modeJeuComboBox.getValue());
            newTeam.setLogoPath(logoPath);
            try {

                teamService.insert(newTeam);
                teams.setAll(teamService.showAll());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            ((MFXStageDialog) dialogContent.getScene().getWindow()).close();
        });

        MFXButton cancelButton = new MFXButton("Cancel");
        saveButton.getStyleClass().add("custom-button");
        cancelButton.setOnAction(e -> ((MFXStageDialog) dialogContent.getScene().getWindow()).close());

        dialogContent.addActions(saveButton, cancelButton);

        // Create and show the dialog
        MFXStageDialog dialog = MFXGenericDialogBuilder.build(dialogContent)
                .toStageDialogBuilder()
                .initOwner(grid.getScene().getWindow()) // Set the owner window
                .setTitle("Add Team")
                .setScrimOwner(true)
                .setDraggable(true)
                .setOwnerNode(grid)
                .setScrimPriority(ScrimPriority.WINDOW)
                .get();
        Stage stage = (Stage) dialog.getScene().getWindow();
        stage.setWidth(550); // Set the width of the dialog
        stage.setHeight(400); // Set the height of the dialog
        dialog.showDialog();
    }
    private String saveUploadedFile(File file) {
        String uploadDir = "src/main/resources/io/github/palexdev/materialfx/demo/uploads/"; // Directory to save uploaded files
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs(); // Create the directory if it doesn't exist
        }

        String fileName = System.currentTimeMillis() + "_" + file.getName(); // Unique file name
        File destFile = new File(uploadDir + fileName);

        try {
            Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return "src/main/resources/io/github/palexdev/materialfx/demo/uploads/"+fileName; // Return the file path
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void modifyTeam(Team team) {
        // Create a dialog for modifying the team
        MFXGenericDialog dialogContent = MFXGenericDialogBuilder.build()
                .setHeaderText("Modify Team")
                .makeScrollable(true)
                .get();

        // Create form fields with the current team data
        MFXTextField nameField = new MFXTextField();
        nameField.setFloatingText("Team Name");
        nameField.setText(team.getNom());
        nameField.setStyle("-fx-pref-width: 450; -fx-border-radius: 10; -fx-start-margin: 75;");

        MFXTextField categoryField = new MFXTextField();
        categoryField.setFloatingText("Category");
        categoryField.setText(team.getCategorie());
        categoryField.setStyle("-fx-pref-width: 450; -fx-border-radius: 10; -fx-start-margin: 75;");

        MFXTextField nbPlayersField = new MFXTextField();
        nbPlayersField.setFloatingText("Number of Players");
        nbPlayersField.setText(String.valueOf(team.getNombreJoueurs()));
        nbPlayersField.setStyle("-fx-pref-width: 450; -fx-border-radius: 10; -fx-start-margin: 75;");

        MFXComboBox<ModeJeu> modeJeuComboBox = new MFXComboBox<>();
        modeJeuComboBox.setFloatingText("Game Mode");
        modeJeuComboBox.setItems(FXCollections.observableArrayList(ModeJeu.values()));
        modeJeuComboBox.setValue(team.getModeJeu());
        modeJeuComboBox.setStyle("-fx-pref-width: 450; -fx-border-radius: 10; -fx-start-margin: 75;");

        // Create validators for each field
        nameField.getValidator().constraint("Team name is required", nameField.textProperty().length().greaterThanOrEqualTo(1));
        categoryField.getValidator().constraint("Category is required", categoryField.textProperty().length().greaterThanOrEqualTo(1));
        nbPlayersField.getValidator().constraint("Number of players is required", nbPlayersField.textProperty().length().greaterThanOrEqualTo(1));
        modeJeuComboBox.getValidator().constraint("Game mode is required", modeJeuComboBox.valueProperty().isNotNull());

        // Add fields to the dialog
        VBox form = new VBox(20, wrapNodeForValidation(nameField), wrapNodeForValidation(categoryField), wrapNodeForValidation(nbPlayersField), wrapNodeForValidation(modeJeuComboBox));
        form.setAlignment(Pos.TOP_CENTER);
        dialogContent.setContent(form);

        // Add buttons to the dialog
        MFXButton saveButton = new MFXButton("Save");
        saveButton.setOnAction(event -> {
            boolean allValid = true;

            // Loop through all form fields and validate
            for (Node node : form.getChildren()) {
                if (node instanceof VBox vbox) {
                    Node fieldNode = vbox.getChildren().get(0); // First child is the field
                    if (fieldNode instanceof Validated validatedNode) {
                        MFXValidator validator = validatedNode.getValidator();
                        List<Constraint> validate = validator.validate(); // Trigger validation

                        // Retrieve the error label from the node's properties
                        Label errorLabel = (Label) fieldNode.getProperties().get("errorLabel");

                        if (!validate.isEmpty()) {
                            // Show error message
                            errorLabel.setText(validate.get(0).getMessage());
                            errorLabel.setVisible(true);
                            errorLabel.setManaged(true);
                            allValid = false;
                        } else {
                            // Hide error message if valid
                            errorLabel.setText("");
                            errorLabel.setVisible(false);
                            errorLabel.setManaged(false);
                        }
                    }
                }
            }

            if (!allValid) {
                System.out.println("Validation failed! Errors are now visible.");
                return;
            }

            // Update the team with the new values
            team.setNom(nameField.getText());
            team.setCategorie(categoryField.getText());
            team.setNombreJoueurs(Integer.parseInt(nbPlayersField.getText()));
            team.setModeJeu(modeJeuComboBox.getValue());

            try {
                teamService.update(team); // Assuming update method exists in TeamService
                teams.setAll(teamService.showAll()); // Refresh the table
            } catch (SQLException e) {
                logger.error("An error occurred while updating the team: " + e.getMessage());
            }
            ((MFXStageDialog) dialogContent.getScene().getWindow()).close();
        });

        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setOnAction(event -> ((MFXStageDialog) dialogContent.getScene().getWindow()).close());

        dialogContent.addActions(saveButton, cancelButton);

        // Create and show the dialog
        MFXStageDialog dialog = MFXGenericDialogBuilder.build(dialogContent)
                .toStageDialogBuilder()
                .initOwner(grid.getScene().getWindow()) // Set the owner window
                .setTitle("Modify Team")
                .setScrimOwner(true)
                .setDraggable(true)
                .setOwnerNode(grid)
                .setScrimPriority(ScrimPriority.WINDOW)
                .get();

        // Access the underlying Stage and set its size
        Stage stage = (Stage) dialog.getScene().getWindow();
        stage.setWidth(550); // Set the width of the dialog
        stage.setHeight(400); // Set the height of the dialog

        dialog.showDialog();
    }

    // Helper method to wrap a node with an error label
    private <T extends Node & Validated> Node wrapNodeForValidation(T node) {
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setStyle("-fx-font-size: 12; -fx-opacity: 1;");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false); // Hidden by default

        // Wrap field with error message
        VBox wrap = new VBox(2, node, errorLabel);
        wrap.setAlignment(Pos.TOP_CENTER);

        // Store the error label in the Node's properties for later use in Save
        node.getProperties().put("errorLabel", errorLabel);

        return wrap;
    }


    private void deleteTeam(Team team) {
        // Create a confirmation dialog
        MFXGenericDialog dialogContent = MFXGenericDialogBuilder.build()
                .setHeaderText("Delete Team")
                .setContentText("Are you sure you want to delete this team: "+team.getNom()+" ?")
                .makeScrollable(true)
                .get();

        // Add buttons to the dialog
        MFXButton confirmButton = new MFXButton("Confirm");
        confirmButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        confirmButton.setOnAction(event -> {
            try {
                teamService.delete(team);
                teams.setAll(teamService.showAll()); // Refresh the table
            } catch (SQLException e) {
                e.printStackTrace();
            }
            ((MFXStageDialog) dialogContent.getScene().getWindow()).close();
        });

        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setOnAction(event -> ((MFXStageDialog) dialogContent.getScene().getWindow()).close());

        dialogContent.addActions(confirmButton, cancelButton);

        // Create and show the dialog
        MFXStageDialog dialog = MFXGenericDialogBuilder.build(dialogContent)
                .toStageDialogBuilder()
                .initOwner(grid.getScene().getWindow()) // Set the owner window
                .setTitle("Delete Team")
                .setScrimOwner(true)
                .setDraggable(true)
                .setOwnerNode(grid)
                .setScrimPriority(ScrimPriority.WINDOW)
                .get();

        dialog.showDialog();
    }
    public void RankingTeam(Team team) {
        // Create a dialog for the Table of Team Ranking
        MFXGenericDialog dialogContent = MFXGenericDialogBuilder.build()
                .setHeaderText("Team Ranking")
                .makeScrollable(true)
                .get();
        // Fetch data from the database and populate the table
        try {

            teamsRanking.clear(); // Clear the list before adding new data
            teamsRanking.setAll(teamRankingService.showTeamRanking(team.getId())); // Refresh the table

            // Check if the team has joined any tournaments
            if (teamsRanking.isEmpty()) {
                // If no tournaments found, display a message instead of the table
                Label noTournamentLabel = new Label("This team didn't join any tournament yet.");
                noTournamentLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-alignment: center;");
                dialogContent.setContent(noTournamentLabel);

            } else {
                // If tournaments are found, display the table
                // Add a listener to autosize columns when the page changes (only if not already registered)
                // Initialize and set up the paginated2 table
                initializePaginated2();
                if (!isListenerRegistered) {
                    When.onChanged(paginated2.currentPageProperty())
                            .then((oldValue, newValue) -> paginated2.autosizeColumns())
                            .listen();
                    isListenerRegistered = true; // Mark the listener as registered
                }

                // Set the items for the paginated2 table
                paginated2.setItems(teamsRanking);

                // Add the paginated2 table to the dialog content
                dialogContent.setContent(paginated2);
            }
        } catch (SQLException e) {
            logger.error("An error occurred while fetching the team ranking: " + e.getMessage());

            // Display an error message if there's an SQL exception
            Label errorLabel = new Label("An error occurred while fetching data. Please try again.");
            errorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-alignment: center;");
            dialogContent.setContent(errorLabel);
        }

        // Add a "Close" button to the dialog
        MFXButton closeButton = new MFXButton("Close");
        closeButton.getStyleClass().add("custom-button");
        closeButton.setOnAction(event -> ((MFXStageDialog) dialogContent.getScene().getWindow()).close());
        dialogContent.addActions(closeButton);

        // Create and show the dialog
        MFXStageDialog dialog = MFXGenericDialogBuilder.build(dialogContent)
                .toStageDialogBuilder()
                .initOwner(grid.getScene().getWindow()) // Set the owner window
                .setTitle("Team Ranking")
                .setScrimOwner(true)
                .setDraggable(true)
                .setOwnerNode(grid)
                .setScrimPriority(ScrimPriority.WINDOW)
                .get();

        // Access the underlying Stage and set its size
        Stage stage = (Stage) dialog.getScene().getWindow();
        stage.setWidth(1050); // Set the width of the dialog
        stage.setHeight(700); // Set the height of the dialog

        // Reset the state when the dialog is closed
        stage.setOnCloseRequest(event -> {
            paginated2.setPrefSize(600, 400); // Reset the table size
            paginated2.setItems(FXCollections.observableArrayList()); // Clear the table items
            isListenerRegistered = false; // Reset the listener flag
        });

        dialog.showDialog();
    }
}
