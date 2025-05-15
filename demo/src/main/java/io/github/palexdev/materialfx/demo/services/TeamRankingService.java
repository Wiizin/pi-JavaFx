package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.Matches;
import io.github.palexdev.materialfx.demo.model.Team;
import io.github.palexdev.materialfx.demo.model.TeamRanking;
import io.github.palexdev.materialfx.demo.utils.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TeamRankingService implements TeamCRUD<TeamRanking> {
    private final Connection cnx = DbConnection.getInstance().getCnx();
    private final TeamService teamS = new TeamService();

    public TeamRanking getRankingByTeamAndTournament(int teamId, int tournamentId) {
        String query = "SELECT * FROM ranking WHERE id_team = ? AND id_tournoi = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, teamId);
            pstmt.setInt(2, tournamentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRankingFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching ranking for team " + teamId + ": " + e.getMessage());
        }
        return null;
    }

    private TeamRanking mapRankingFromResultSet(ResultSet rs) throws SQLException {
        Team team = teamS.GetTeamById(rs.getInt("id_team"));
        return new TeamRanking(
                rs.getInt("id"),
                rs.getInt("id_team"),
                rs.getInt("points"),
                rs.getInt("position"),
                rs.getInt("id_tournoi"),
                rs.getInt("wins"),
                rs.getInt("draws"),
                rs.getInt("losses"),
                rs.getInt("goals_scored"),
                rs.getInt("goals_conceded"),
                rs.getInt("goal_difference"),
                team
        );
    }

    @Override
    public int insert(TeamRanking teamRanking) throws SQLException {
        String sql = "INSERT INTO ranking (id_team, points, position, id_tournoi, wins, draws, losses, goals_scored, goals_conceded, goal_difference) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, teamRanking.getIdTeam());
            pstmt.setInt(2, teamRanking.getPoints());
            pstmt.setInt(3, teamRanking.getPosition());
            pstmt.setInt(4, teamRanking.getIdTournoi());
            pstmt.setInt(5, teamRanking.getWins());
            pstmt.setInt(6, teamRanking.getDraws());
            pstmt.setInt(7, teamRanking.getLosses());
            pstmt.setInt(8, teamRanking.getGoalsScored());
            pstmt.setInt(9, teamRanking.getGoalsConceded());
            pstmt.setInt(10, teamRanking.getGoalDifference());

            int affectedRows = pstmt.executeUpdate();

            // Retrieve and set the generated ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    teamRanking.setId(generatedKeys.getInt(1));
                }
            }
            return affectedRows;
        }
    }

    @Override
    public int update(TeamRanking teamRanking) throws SQLException {
        String sql = "UPDATE ranking SET " +
                "points = ?, position = ?, wins = ?, draws = ?, losses = ?, " +
                "goals_scored = ?, goals_conceded = ?, goal_difference = ? " +
                "WHERE id_team = ? AND id_tournoi = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            pstmt.setInt(1, teamRanking.getPoints());
            pstmt.setInt(2, teamRanking.getPosition());
            pstmt.setInt(3, teamRanking.getWins());
            pstmt.setInt(4, teamRanking.getDraws());
            pstmt.setInt(5, teamRanking.getLosses());
            pstmt.setInt(6, teamRanking.getGoalsScored());
            pstmt.setInt(7, teamRanking.getGoalsConceded());
            pstmt.setInt(8, teamRanking.getGoalDifference());
            pstmt.setInt(9, teamRanking.getIdTeam());
            pstmt.setInt(10, teamRanking.getIdTournoi());

            return pstmt.executeUpdate();
        }
    }

    @Override
    public int delete(TeamRanking teamRanking) throws SQLException {
        // Implement deletion logic here
        return 0;
    }

    @Override
    public List<TeamRanking> showAll() throws SQLException {
        // Implement logic to retrieve all team rankings
        return List.of();
    }

    public void updateRankingTableByTeam(int teamId) throws SQLException {
        String req1 = "SELECT id_tournoi FROM ranking WHERE id_team = ?";
        int idTournoi = -1;

        // Retrieve id_tournoi from ranking table
        try (PreparedStatement pstmt = cnx.prepareStatement(req1)) {
            pstmt.setInt(1, teamId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idTournoi = rs.getInt("id_tournoi");
                }
            }
        }

        // If no tournament is found for the given team, exit the method
        if (idTournoi == -1) {
            return;
        }

        List<Matches> matchesList = new ArrayList<>();
        String req3 = "SELECT * FROM matches WHERE id_tournoi = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req3)) {
            pst.setInt(1, idTournoi);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Matches m = new Matches(
                            rs.getInt("id"),
                            rs.getString("teamAName"),
                            rs.getString("teamBName"),
                            rs.getInt("score_TeamA"),
                            rs.getInt("score_TeamB"),
                            rs.getString("status"),
                            rs.getTimestamp("match_Time").toLocalDateTime(),
                            rs.getString("location_Match"),
                            rs.getInt("id_tournoi"),
                            rs.getInt("id_TeamA"),
                            rs.getInt("id_TeamB")
                    );
                    matchesList.add(m);
                }
            }
        }

        // Update ranking table based on matches
        for (Matches match : matchesList) {
            if (match.getMatchTime() != null && match.getMatchTime().isBefore(LocalDateTime.now())) {
                if (match.getScoreTeamA() > match.getScoreTeamB()) {
                    updateTeamInRankingTable(match.getTeamAId(), match.getIdTournoi(), match.getScoreTeamA(), match.getScoreTeamB(), 3);
                    updateTeamInRankingTable(match.getTeamBId(), match.getIdTournoi(), match.getScoreTeamB(), match.getScoreTeamA(), 0);
                    updateRankingTablePosition(idTournoi);
                } else if (match.getScoreTeamB() > match.getScoreTeamA()) {
                    updateTeamInRankingTable(match.getTeamAId(), match.getIdTournoi(), match.getScoreTeamA(), match.getScoreTeamB(), 0);
                    updateTeamInRankingTable(match.getTeamBId(), match.getIdTournoi(), match.getScoreTeamB(), match.getScoreTeamA(), 3);
                    updateRankingTablePosition(idTournoi);
                } else {
                    updateTeamInRankingTable(match.getTeamAId(), match.getIdTournoi(), match.getScoreTeamA(), match.getScoreTeamB(), 1);
                    updateTeamInRankingTable(match.getTeamBId(), match.getIdTournoi(), match.getScoreTeamB(), match.getScoreTeamA(), 1);
                    updateRankingTablePosition(idTournoi);
                }
            }
        }
    }
    public void updateRankingTablePosition(int idTournoi) throws SQLException {
        String req2 = "SELECT * FROM ranking WHERE id_tournoi = ? ORDER BY points DESC, goal_difference DESC";
        try (PreparedStatement pst = cnx.prepareStatement(req2)) {
            pst.setInt(1, idTournoi);
            try (ResultSet rs = pst.executeQuery()) {
                int position = 1; // Start ranking from position 1
                while (rs.next()) {
                    // Retrieve current ranking data
                    int idTeam = rs.getInt("id_team");

                    // Update the ranking table with the new position
                    String updateQuery = "UPDATE ranking SET position = ? WHERE id_tournoi = ? AND id_team = ?";
                    try (PreparedStatement updateStmt = cnx.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, position); // Set the new position
                        updateStmt.setInt(2, idTournoi); // Filter by tournament ID
                        updateStmt.setInt(3, idTeam); // Filter by team ID

                        updateStmt.executeUpdate();
                    }

                    position++; // Increment the position for the next team
                }
            }
        }
    }
    public void updateTeamInRankingTable(int idTeam, int idTournoi, int scoreTeam, int scoreOpponent, int points) throws SQLException {
        String req2 = "SELECT * FROM ranking WHERE id_tournoi = ? AND id_team = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req2)) {
            pst.setInt(1, idTournoi);
            pst.setInt(2, idTeam);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Retrieve current ranking data
                    int currentPoints = rs.getInt("points");
                    int currentWins = rs.getInt("wins");
                    int currentDraws = rs.getInt("draws");
                    int currentLosses = rs.getInt("losses");
                    int currentGoalsScored = rs.getInt("goals_scored");
                    int currentGoalsConceded = rs.getInt("goals_conceded");
                    int currentGoalDifference = rs.getInt("goal_difference");

                    // Update ranking based on match result
                    int newPoints = currentPoints + points;
                    int newWins;
                    int newDraws;
                    int newLosses;
                    if(points==3){
                         newWins = currentWins + 1;
                         newDraws = currentDraws ;
                         newLosses = currentLosses;
                    } else if (points==1) {
                         newWins = currentWins ;
                         newDraws = currentDraws + 1;
                         newLosses = currentLosses;
                    }else{
                         newWins = currentWins ;
                         newDraws = currentDraws ;
                         newLosses = currentLosses + 1;
                    }

                    int newGoalsScored = currentGoalsScored + scoreTeam;
                    int newGoalsConceded = currentGoalsConceded + scoreOpponent;
                    int newGoalDifference = currentGoalDifference+(newGoalsScored - newGoalsConceded);

                    // Update the ranking table
                    String updateQuery = "UPDATE ranking SET points = ?, wins = ?, draws = ?, losses = ?, " +
                            "goals_scored = ?, goals_conceded = ?, goal_difference = ? " +
                            "WHERE id_tournoi = ? AND id_team = ?";

                    try (PreparedStatement updateStmt = cnx.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, newPoints);
                        updateStmt.setInt(2, newWins);
                        updateStmt.setInt(3, newDraws);
                        updateStmt.setInt(4, newLosses);
                        updateStmt.setInt(5, newGoalsScored);
                        updateStmt.setInt(6, newGoalsConceded);
                        updateStmt.setInt(7, newGoalDifference);
                        updateStmt.setInt(8, idTournoi);
                        updateStmt.setInt(9, idTeam);

                        updateStmt.executeUpdate();
                    }
                }
            }
        }
    }

    public List<TeamRanking> showTeamRanking(int idTeam) throws SQLException {
        String req1 = "SELECT id_tournoi FROM ranking WHERE id_team = ?";
        int idTournoi = -1;

        // Retrieve id_tournoi from ranking table
        try (PreparedStatement pstmt = cnx.prepareStatement(req1)) {
            pstmt.setInt(1, idTeam);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idTournoi = rs.getInt("id_tournoi");
                }
            }
        }

        // If no tournament is found for the given team, return an empty list
        if (idTournoi == -1) {
            return new ArrayList<>();
        }

        List<TeamRanking> rankingList = new ArrayList<>();
        String req2 = "SELECT * FROM ranking WHERE id_tournoi = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req2)) {
            pst.setInt(1, idTournoi);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Team team = teamS.GetTeamById(rs.getInt("id_team"));

                    TeamRanking e = new TeamRanking(
                            rs.getInt("id"),
                            rs.getInt("id_team"),
                            rs.getInt("points"),
                            rs.getInt("position"),
                            rs.getInt("id_tournoi"),
                            rs.getInt("wins"),
                            rs.getInt("draws"),
                            rs.getInt("losses"),
                            rs.getInt("goals_scored"),
                            rs.getInt("goals_conceded"),
                            rs.getInt("goal_difference"),
                            team
                    );
                    rankingList.add(e);
                }
            }
        }
        return rankingList;
    }
}
