package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.Matches;
import io.github.palexdev.materialfx.demo.utils.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchesService implements CRUD<Matches> {

    private final Connection cnx;

    public MatchesService() {
        System.out.println("MatchesService: Initializing service");
        this.cnx = DbConnection.getInstance().getCnx();
        if (this.cnx == null) {
            System.err.println("MatchesService: Database connection is null!");
        } else {
            System.out.println("MatchesService: Database connection established successfully");
        }
    }
    public int getTeamIdByName(String teamName) {
        String query = "SELECT id FROM team WHERE nom = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, teamName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id"); // Return the found team ID
                } else {
                    throw new RuntimeException("Team not found: " + teamName);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching team ID for: " + teamName, e);
        }
    }


    @Override
    public int insert(Matches match) throws SQLException {
        // Debug logging
        int teamAId = getTeamIdByName(match.getTeamAName());
        int teamBId = getTeamIdByName(match.getTeamBName());

        // Verify teams exist first
        String verifyTeam = "SELECT id, nom FROM team WHERE id IN (?, ?)";
        try (PreparedStatement verifyPs = cnx.prepareStatement(verifyTeam)) {
            verifyPs.setInt(1, match.getTeamAId());
            verifyPs.setInt(2, match.getTeamBId());
            ResultSet rs = verifyPs.executeQuery();

            System.out.println("Verifying teams existence:");
            while (rs.next()) {
                System.out.println("Found team - ID: " + rs.getInt("id") + ", Name: " + rs.getString("nom"));
            }
        }
        String req = "INSERT INTO matches (id_TeamA, id_TeamB, score_TeamA, score_TeamB, status, match_Time, location_Match, id_tournoi) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";



        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, teamAId);
            ps.setInt(2, teamBId);
            ps.setInt(3, match.getScoreTeamA());
            ps.setInt(4, match.getScoreTeamB());
            ps.setString(5, match.getStatus());
            ps.setTimestamp(6, Timestamp.valueOf(match.getMatchTime()));
            ps.setString(7, match.getLocationMatch());
            ps.setLong(8, match.getIdTournoi());

            int rowsAffected = ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    match.setId(generatedId);
                    System.out.println("Match inserted with ID: " + generatedId);
                } else {
                    throw new SQLException("Inserting match failed, no ID obtained.");
                }
            }

            return rowsAffected;
        }
    }

    @Override
    public int update(Matches match) throws SQLException {
        String req = "UPDATE matches SET id_TeamA=?, id_TeamB=?, score_TeamA=?, score_TeamB=?, status=?, match_Time=?, location_Match=?, id_tournoi=? WHERE id=?";

        System.out.println("Updating match - ID: " + match.getId());

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, match.getTeamAId());
            ps.setInt(2, match.getTeamBId());
            ps.setInt(3, match.getScoreTeamA());
            ps.setInt(4, match.getScoreTeamB());
            ps.setString(5, match.getStatus());
            ps.setTimestamp(6, Timestamp.valueOf(match.getMatchTime()));
            ps.setString(7, match.getLocationMatch());
            ps.setLong(8, match.getIdTournoi());
            ps.setInt(9, match.getId());

            int result = ps.executeUpdate();
            System.out.println("Update result: " + result + " rows affected");
            return result;
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int delete(Matches match) throws SQLException {
        String req = "DELETE FROM matches WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, match.getId());
            return ps.executeUpdate();
        }
    }

    @Override
    public List<Matches> showAll() throws SQLException {
        List<Matches> matches = new ArrayList<>();
        String query = """
            SELECT m.id, 
                   m.id_TeamA, tA.nom as teamA_name,
                   m.id_TeamB, tB.nom as teamB_name,
                   m.score_TeamA, m.score_TeamB, 
                   m.status, m.match_Time, 
                   m.location_Match, m.id_tournoi
            FROM matches m
            LEFT JOIN team tA ON m.id_TeamA = tA.id
            LEFT JOIN team tB ON m.id_TeamB = tB.id
            """;

        try (PreparedStatement ps = cnx.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Matches match = new Matches();
                match.setId(rs.getInt("id"));
                match.setTeamAId(rs.getInt("id_TeamA"));
                match.setTeamAName(rs.getString("teamA_name"));
                match.setTeamBId(rs.getInt("id_TeamB"));
                match.setTeamBName(rs.getString("teamB_name"));
                match.setScoreTeamA(rs.getInt("score_TeamA"));
                match.setScoreTeamB(rs.getInt("score_TeamB"));
                match.setStatus(rs.getString("status"));
                match.setMatchTime(rs.getTimestamp("match_Time").toLocalDateTime());
                match.setLocationMatch(rs.getString("location_Match"));
                match.setIdTournoi(rs.getInt("id_tournoi"));
                matches.add(match);
            }
        }
        return matches;
    }


    public List<Matches> getMatchesByTournament(int tournamentId) throws SQLException {
        List<Matches> matches = new ArrayList<>();
        String query = """
        SELECT m.id, 
               m.id_TeamA, tA.nom as teamA_name,
               m.id_TeamB, tB.nom as teamB_name,
               m.score_TeamA, m.score_TeamB, 
               m.status, m.match_Time, 
               m.location_Match, m.id_tournoi
        FROM matches m
        LEFT JOIN team tA ON m.id_TeamA = tA.id
        LEFT JOIN team tB ON m.id_TeamB = tB.id
        WHERE m.id_tournoi = ?
        """;

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Matches match = new Matches();
                    match.setId(rs.getInt("id"));
                    match.setTeamAId(rs.getInt("id_TeamA"));
                    match.setTeamAName(rs.getString("teamA_name"));
                    match.setTeamBId(rs.getInt("id_TeamB"));
                    match.setTeamBName(rs.getString("teamB_name"));
                    match.setScoreTeamA(rs.getInt("score_TeamA"));
                    match.setScoreTeamB(rs.getInt("score_TeamB"));
                    match.setStatus(rs.getString("status"));
                    match.setMatchTime(rs.getTimestamp("match_Time").toLocalDateTime());
                    match.setLocationMatch(rs.getString("location_Match"));
                    match.setIdTournoi(rs.getInt("id_tournoi"));
                    matches.add(match);
                }
            }
        }
        return matches;
    }
    public Matches getMatchById(int matchId) throws SQLException {
        // Implement the logic to retrieve a match by its ID from the database
        // For example:
        String query = "SELECT * FROM matches WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, matchId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Matches(
                        rs.getInt("id"),
                        rs.getString("teamAName"),
                        rs.getString("teamBName"),
                        rs.getInt("scoreTeamA"),
                        rs.getInt("scoreTeamB"),
                        rs.getString("status"),
                        rs.getTimestamp("matchTime").toLocalDateTime(),
                        rs.getString("locationMatch"),
                        rs.getInt("idTournoi")
                );
            }
        }
        return null;
    }



    public int insert2(Matches match) throws SQLException {
        // SQL query to insert a match
        String req = "INSERT INTO matches (id_TeamA, id_TeamB, score_TeamA, score_TeamB, status, match_Time, location_Match, id_tournoi,teamAName,teamBName) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?,?)";

        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            // Set parameters for the PreparedStatement
            ps.setInt(1, match.getTeamAId()); // id_TeamA
            ps.setInt(2, match.getTeamBId()); // id_TeamB
            ps.setInt(3, match.getScoreTeamA()); // score_TeamA
            ps.setInt(4, match.getScoreTeamB()); // score_TeamB
            ps.setString(5, match.getStatus()); // status
            ps.setTimestamp(6, Timestamp.valueOf(match.getMatchTime())); // match_Time
            ps.setString(7, match.getLocationMatch()); // location_Match
            ps.setLong(8, match.getIdTournoi()); // id_tournoi
            ps.setString(9,match.getTeamAName());
            ps.setString(10, match.getTeamBName());

            // Execute the query
            int rowsAffected = ps.executeUpdate();

            // Retrieve the generated keys (if any)
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1); // Get the generated ID
                    match.setId(generatedId); // Set the ID in the Matches object
                    System.out.println("Match inserted with ID: " + generatedId);
                } else {
                    throw new SQLException("Inserting match failed, no ID obtained.");
                }
            }

            return rowsAffected; // Return the number of rows affected
        }
    }

}

