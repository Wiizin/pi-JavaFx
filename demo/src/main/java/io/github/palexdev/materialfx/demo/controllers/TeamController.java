package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXPaginatedTableView;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.demo.model.ModeJeu;
import io.github.palexdev.materialfx.demo.model.Team;
import io.github.palexdev.materialfx.demo.services.TeamService;
import io.github.palexdev.materialfx.filter.EnumFilter;
import io.github.palexdev.materialfx.filter.IntegerFilter;
import io.github.palexdev.materialfx.filter.StringFilter;
import io.github.palexdev.materialfx.utils.others.observables.When;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;

import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.ResourceBundle;

public class TeamController implements Initializable {
    @FXML
    private MFXPaginatedTableView<Team> paginated;
    private TeamService teamService = new TeamService();
    // ObservableList to hold the teams
    private ObservableList<Team> teams = FXCollections.observableArrayList();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupPaginated();
        paginated.autosizeColumnsOnInitialization();
        // Fetch data from the database and populate the table
        try {
            teams.setAll(teamService.showAll()); // Fetch and set data
        } catch (SQLException e) {
            e.printStackTrace();
        }
        When.onChanged(paginated.currentPageProperty())
                .then((oldValue, newValue) -> paginated.autosizeColumns())
                .listen();
        paginated.setItems(teams);
    }
    private void setupPaginated() {
        MFXTableColumn<Team> id = new MFXTableColumn<>("ID", false, Comparator.comparing(Team::getId));
        MFXTableColumn<Team> name = new MFXTableColumn<>("Name", false, Comparator.comparing(Team::getNom));
        MFXTableColumn<Team> categoire = new MFXTableColumn<>("categoire", false, Comparator.comparing(Team::getCategorie));
        MFXTableColumn<Team> NBPlayers = new MFXTableColumn<>("NBPlayers", false, Comparator.comparing(Team::getNombreJoueurs));
        MFXTableColumn<Team> ModeJeu = new MFXTableColumn<>("ModeJeu", false, Comparator.comparing(Team::getModeJeu));

        id.setRowCellFactory(team -> new MFXTableRowCell<>(Team::getId));
        name.setRowCellFactory(team -> new MFXTableRowCell<>(Team::getNom));
        categoire.setRowCellFactory(team -> new MFXTableRowCell<>(Team::getCategorie) {{
            setAlignment(Pos.CENTER_RIGHT);
        }});
        NBPlayers.setRowCellFactory(team -> new MFXTableRowCell<>(Team::getNombreJoueurs));
        ModeJeu.setRowCellFactory(team -> new MFXTableRowCell<>(Team::getModeJeu));


        paginated.getTableColumns().addAll(id, name, categoire, NBPlayers, ModeJeu);
        paginated.getFilters().addAll(
                new IntegerFilter<>("ID", Team::getId),
                new StringFilter<>("Name", Team::getNom),
                new StringFilter<>("Categorie", Team::getCategorie),
                new IntegerFilter<>("NBPlayers", Team::getNombreJoueurs),
                new EnumFilter<>("ModeJeu", Team::getModeJeu, ModeJeu.class)
        );

    }
}
