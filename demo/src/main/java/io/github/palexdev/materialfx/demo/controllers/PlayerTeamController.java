package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPaginatedTableView;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.demo.model.*;
import io.github.palexdev.materialfx.demo.services.TeamRankingService;
import io.github.palexdev.materialfx.demo.services.TeamService;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import io.github.palexdev.materialfx.filter.EnumFilter;
import io.github.palexdev.materialfx.filter.IntegerFilter;
import io.github.palexdev.materialfx.filter.StringFilter;
import io.github.palexdev.materialfx.utils.others.observables.When;
import io.github.palexdev.mfxcore.controls.Label;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import static io.github.palexdev.materialfx.demo.MFXDemoResourcesLoader.loadURL;

public class PlayerTeamController implements Initializable {
    @FXML
    private MFXPaginatedTableView<Team> paginated;
    @FXML
    private GridPane grid;
    private boolean isListenerRegistered = false;
    private TeamService teamService = new TeamService();
    // ObservableList to hold the teams
    private ObservableList<Team> teams = FXCollections.observableArrayList();
    private static final Logger logger = LoggerFactory.getLogger(TeamService.class);
    private UserService userService=new UserService();
    User currentUser = UserSession.getInstance().getCurrentUser();
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
    private void setupPaginated() {
        MFXTableColumn<Team> id = new MFXTableColumn<>("ID", false, Comparator.comparing(Team::getId));
        id.setAlignment(Pos.CENTER);
        id.setFont(Font.font("System", FontWeight.BOLD, 12));


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
        MFXTableColumn<Team> Join = new MFXTableColumn<>("Join", false, Comparator.comparing(Team::getId));
        Join.setAlignment(Pos.CENTER);
        Join.setFont(Font.font("System", FontWeight.BOLD, 12));
        // Set row cell factories for data columns
        id.setRowCellFactory(team -> new MFXTableRowCell<>(Team::getId){{
            setAlignment(Pos.CENTER);
        }});

        name.setRowCellFactory(team -> {
            MFXTableRowCell<Team, Void> cell = new MFXTableRowCell<>(null);
            // Create an HBox to hold the logo and team name
            HBox content = new HBox(10); // 10 is the spacing between the logo and the name
            content.setAlignment(Pos.CENTER_LEFT); // Align content to the left

            // Create an ImageView to display the logo
            ImageView logoView = new ImageView();
            logoView.setFitWidth(30); // Set appropriate width
            logoView.setFitHeight(30); // Set appropriate height
            logoView.setPreserveRatio(true);

            // Check if the team has a valid logo path
            String logoPath = team.getLogoPath();
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
            content.getChildren().addAll(logoView, new Label(team.getNom()));

            // Set the HBox as the graphic of the cell
            cell.setGraphic(content);
            cell.setAlignment(Pos.CENTER_LEFT); // Align the cell content to the left
            return cell;
        });
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

        // Set row cell factory for the Modify column
        Join.setRowCellFactory(team -> {
            MFXTableRowCell<Team, Void> cell = new MFXTableRowCell<>(null);

            // Create Modify button
            MFXButton modifyButton = new MFXButton("Join");
            modifyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            modifyButton.setOnAction(event -> {
                Team selectedTeam = team; // Use the 'team' parameter directly
                JoinTeam(selectedTeam); // Call method to handle modification
            });

            // Add button to the cell
            cell.setGraphic(modifyButton);
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        // Add columns to the table
        paginated.getTableColumns().addAll(id,name, categoire, NBPlayers, ModeJeu,Join);

        // Add filters
        paginated.getFilters().addAll(
                new IntegerFilter<>("ID", Team::getId),
                new StringFilter<>("Name", Team::getNom),
                new StringFilter<>("Categorie", Team::getCategorie),
                new IntegerFilter<>("NBPlayers", Team::getNombreJoueurs),
                new EnumFilter<>("ModeJeu", Team::getModeJeu, io.github.palexdev.materialfx.demo.model.ModeJeu.class)
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
    private void JoinTeam(Team team) {
        // Create a confirmation dialog
        MFXGenericDialog dialogContent = MFXGenericDialogBuilder.build()
                .setHeaderText("Join Team")
                .setContentText("Are you sure you want to join this team: " + team.getNom() + "?")
                .makeScrollable(true)
                .get();

        // Add buttons to the dialog
        MFXButton confirmButton = new MFXButton("Confirm");
        confirmButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // Green color for confirmation
        confirmButton.setOnAction(event -> {
            // Update the current user's team ID
            currentUser.setIdteam(team.getId());
            userService.update(currentUser);

            // Refresh the page
            try {
                URL fxmlLocation = getClass().getResource("/io/github/palexdev/materialfx/demo/fxml/PlayerHome.fxml");
                if (fxmlLocation == null) {
                    System.err.println("FXML file not found!");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlLocation);
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) grid.getScene().getWindow(); // Get the current stage
                stage.setScene(scene); // Set the new scene
                stage.show();
                System.out.println("Navigation to OrganizerHome.fxml successful."); // Debugging
            } catch (IOException e) {
                System.err.println("Failed to load FXML: " + e.getMessage());
                e.printStackTrace();
            }
            // Close the dialog
            ((MFXStageDialog) dialogContent.getScene().getWindow()).close();
        });

        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;"); // Red color for cancellation
        cancelButton.setOnAction(event -> ((MFXStageDialog) dialogContent.getScene().getWindow()).close());

        // Add buttons to the dialog
        dialogContent.addActions(confirmButton, cancelButton);

        // Create and show the dialog
        MFXStageDialog dialog = MFXGenericDialogBuilder.build(dialogContent)
                .toStageDialogBuilder()
                .initOwner(grid.getScene().getWindow()) // Set the owner window
                .setTitle("Join Team") // Corrected title
                .setScrimOwner(true)
                .setDraggable(true)
                .setOwnerNode(grid)
                .setScrimPriority(ScrimPriority.WINDOW)
                .get();

        dialog.showDialog();
    }
}
