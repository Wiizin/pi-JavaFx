package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.ModeJeu;
import io.github.palexdev.materialfx.demo.model.Team;
import io.github.palexdev.materialfx.demo.utils.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamService implements TeamCRUD<Team> {
    private Connection cnx = DbConnection.getInstance().getCnx();

    @Override
    public int insert(Team equipe) throws SQLException {
        String req = "INSERT INTO `Team`(`nom`, `categorie`, `modeJeu`, `nombreJoueurs`) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, equipe.getNom());
            ps.setString(2, equipe.getCategorie());
            ps.setString(3, equipe.getModeJeu().toString()); // Convertir l'enum en String
            ps.setInt(4, equipe.getNombreJoueurs());

            return ps.executeUpdate();
        }
    }

    @Override
    public int update(Team equipe) throws SQLException {
        // Implémentez la logique de mise à jour ici
        return 0;
    }

    @Override
    public int delete(Team equipe) throws SQLException {
        // Implémentez la logique de suppression ici
        return 0;
    }

    @Override
    public List<Team> showAll() throws SQLException {
        List<Team> equipes = new ArrayList<>();
        String req = "SELECT * FROM `Team`";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Team e = new Team(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("categorie"),
                        ModeJeu.valueOf(rs.getString("modeJeu"))
                );
                e.setNombreJoueurs(rs.getInt("nombreJoueurs"));
                equipes.add(e);
            }
        }
        return equipes;
    }
}