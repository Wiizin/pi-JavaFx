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
        String req = "INSERT INTO `Team`(`nom`, `categorie`, `modeJeu`, `nombreJoueurs`, `logoPath`) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, equipe.getNom());
            ps.setString(2, equipe.getCategorie());
            ps.setString(3, equipe.getModeJeu().toString()); // Convert enum to String
            ps.setInt(4, equipe.getNombreJoueurs());
            ps.setString(5, equipe.getLogoPath()); // Set the logo path

            return ps.executeUpdate();
        }
    }

    @Override
    public int update(Team equipe) throws SQLException {
        String req = "UPDATE `Team` SET `nom` = ?, `categorie` = ?, `modeJeu` = ?, `nombreJoueurs` = ? WHERE `id` = ?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, equipe.getNom());
            ps.setString(2, equipe.getCategorie());
            ps.setString(3, equipe.getModeJeu().toString());
            ps.setInt(4, equipe.getNombreJoueurs());
            ps.setInt(5, equipe.getId());
            return ps.executeUpdate();
        }
    }

    @Override
    public int delete(Team equipe) {
        String req = "DELETE FROM `team` WHERE `id` = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, equipe.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging instead of printing
            return 0; // Indicate failure
        }
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
                        ModeJeu.valueOf(rs.getString("modeJeu")),
                        rs.getInt("nombreJoueurs"),
                        rs.getString("logoPath")
                );
                //e.setNombreJoueurs(rs.getInt("nombreJoueurs"));
                equipes.add(e);
            }
        }
        return equipes;
    }
    public Team GetTeamById(int id) throws SQLException {
        Team equipe = null;
        String req = "SELECT * FROM Team WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    equipe = new Team(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("categorie"),
                            ModeJeu.valueOf(rs.getString("modeJeu")),
                            rs.getInt("nombreJoueurs"),
                            rs.getString("logoPath")
                    );
                }
            }
        }
        return equipe; // Returns null if not found
    }

}