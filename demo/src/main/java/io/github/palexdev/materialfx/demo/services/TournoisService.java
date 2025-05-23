package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.ModeJeu;
import io.github.palexdev.materialfx.demo.model.Team;
import io.github.palexdev.materialfx.demo.model.Tournois;
import io.github.palexdev.materialfx.demo.utils.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class TournoisService implements CRUD<Tournois> {

    private final Connection cnx = DbConnection.getInstance().getCnx();
    private Statement st;
    private PreparedStatement ps;
    private static final Logger logger = LoggerFactory.getLogger(TournoisService.class);


    @Override
    public int insert(Tournois tournament) throws SQLException {
        String req = "INSERT INTO tournoi (nom, format, status, start_date, end_date, nbEquipe, tournoiLocation, reglements) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tournament.getNom());
            ps.setString(2, tournament.getFormat());
            ps.setString(3, tournament.getStatus());
            ps.setDate(4, Date.valueOf(tournament.getStartDate()));
            ps.setDate(5, Date.valueOf(tournament.getEndDate()));
            ps.setInt(6, tournament.getNbEquipe());
            ps.setString(7, tournament.getTournoisLocation());
            ps.setString(8, tournament.getReglements());

            int rowsAffected = ps.executeUpdate();

            // Retrieve and set the generated key
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    tournament.setId(generatedId);
                    System.out.println("Tournament inserted with ID: " + generatedId);
                } else {
                    throw new SQLException("Inserting tournament failed, no ID obtained.");
                }
            }

            return rowsAffected;
        }
    }
    public int insert2(Tournois tournament) throws SQLException {
        String req = "INSERT INTO tournoi (nom, format, status, start_date, end_date, nbEquipe, tournoiLocation, reglements,id_organizer) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tournament.getNom());
            ps.setString(2, tournament.getFormat());
            ps.setString(3, tournament.getStatus());
            ps.setDate(4, Date.valueOf(tournament.getStartDate()));
            ps.setDate(5, Date.valueOf(tournament.getEndDate()));
            ps.setInt(6, tournament.getNbEquipe());
            ps.setString(7, tournament.getTournoisLocation());
            ps.setString(8, tournament.getReglements());
            ps.setInt(9, tournament.getIdorganiser());
            int rowsAffected = ps.executeUpdate();

            // Check if the insertion was successful
            if (rowsAffected == 0) {
                throw new SQLException("Failed to insert tournoi: No rows affected.");
            }

            // Retrieve the generated team ID
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Return the generated id_team
                } else {
                    throw new SQLException("Failed to insert tournoi: No generated key returned.");
                }
            }
        }
    }

    @Override
    public int update(Tournois tournament) throws SQLException {
        String req = """
        UPDATE tournoi
           SET nom            = ?,
               format         = ?,
               status         = ?,
               start_date     = ?,
               end_date       = ?,
               nbEquipe       = ?,
               tournoiLocation= ?,
               reglements     = ?
         WHERE id = ?
        """;

        System.out.println("Updating tournament - ID: " + tournament.getId());

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            // Set parameters
            ps.setString(1, tournament.getNom());
            ps.setString(2, tournament.getFormat());
            ps.setString(3, tournament.getStatus());
            ps.setDate(4, Date.valueOf(tournament.getStartDate()));
            ps.setDate(5, Date.valueOf(tournament.getEndDate()));
            ps.setInt(6, tournament.getNbEquipe());
            ps.setString(7, tournament.getTournoisLocation());
            ps.setString(8, tournament.getReglements());
            ps.setInt(9, tournament.getId());

            int result = ps.executeUpdate();
            System.out.println("Update result: " + result + " rows affected");
            return result;
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            throw e; // Rethrow the exception after logging
        }
    }

    @Override
    public int delete(Tournois tournament) throws SQLException {
        String req = "DELETE FROM tournoi WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, tournament.getId());
            return ps.executeUpdate();
        }
    }

    public List<Tournois> showAll() throws SQLException {
        List<Tournois> tournaments = new ArrayList<>();
        String query = "SELECT id, nom, format, status, start_date, end_date, nbEquipe, tournoiLocation, reglements , id_organizer FROM tournoi";

        try (PreparedStatement ps = cnx.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Tournois tournament = new Tournois();
                tournament.setId(rs.getInt("id"));
                tournament.setNom(rs.getString("nom"));
                tournament.setFormat(rs.getString("format"));
                tournament.setStatus(rs.getString("status"));
                tournament.setStartDate(rs.getDate("start_date").toLocalDate());
                tournament.setEndDate(rs.getDate("end_date").toLocalDate());
                tournament.setNbEquipe(rs.getInt("nbEquipe"));
                tournament.setTournoisLocation(rs.getString("tournoiLocation"));
                tournament.setReglements(rs.getString("reglements"));
                tournament.setIdorganiser(rs.getInt("id_organizer"));
                tournaments.add(tournament);
            }
        }
        return tournaments;
    }
    public Tournois GetTournoisByName(String name) throws SQLException {
        Tournois tournament = null;
        String req = "SELECT * FROM tournoi WHERE nom = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, name);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    tournament.setId(rs.getInt("id"));
                    tournament.setNom(rs.getString("nom"));
                    tournament.setFormat(rs.getString("format"));
                    tournament.setStatus(rs.getString("status"));
                    tournament.setStartDate(rs.getDate("start_date").toLocalDate());
                    tournament.setEndDate(rs.getDate("end_date").toLocalDate());
                    tournament.setNbEquipe(rs.getInt("nbEquipe"));
                    tournament.setTournoisLocation(rs.getString("tournoiLocation"));
                    tournament.setReglements(rs.getString("reglements"));
                    tournament.setIdorganiser(rs.getInt("id_organizer"));
                }
            }
        }
        return tournament;
    }



        public Tournois GetTournoisById(int id) throws SQLException {
            Tournois tournament = null;
            String req = "SELECT * FROM tournoi WHERE id = ?";

            try (PreparedStatement pst = cnx.prepareStatement(req)) {
                pst.setInt(1, id);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        tournament = new Tournois();
                        tournament.setId(rs.getInt("id"));
                        tournament.setNom(rs.getString("nom"));
                        tournament.setFormat(rs.getString("format"));
                        tournament.setStatus(rs.getString("status"));
                        tournament.setStartDate(rs.getDate("start_date").toLocalDate());
                        tournament.setEndDate(rs.getDate("end_date").toLocalDate());
                        tournament.setNbEquipe(rs.getInt("nbEquipe"));
                        tournament.setTournoisLocation(rs.getString("tournoiLocation"));
                        tournament.setReglements(rs.getString("reglements"));
                        tournament.setIdorganiser(rs.getInt("id_organizer"));
                        logger.info("Tournament found with ID: {}", id);
                    } else {
                        logger.warn("No tournament found with ID: {}", id);
                    }
                }
            }

            return tournament;
        }
}
