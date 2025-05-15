package io.github.palexdev.materialfx.demo.services;


import io.github.palexdev.materialfx.demo.model.ModeJeu;
import io.github.palexdev.materialfx.demo.model.Team;
import io.github.palexdev.materialfx.demo.model.TeamRanking;
import io.github.palexdev.materialfx.demo.utils.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamRankingService implements TeamCRUD<TeamRanking> {
    private Connection cnx = DbConnection.getInstance().getCnx();
    private TeamService teamS=new TeamService();
    @Override
    public int insert(TeamRanking teamRanking) throws SQLException {
        return 0;
    }

    @Override
    public int update(TeamRanking teamRanking) throws SQLException {
        return 0;
    }

    @Override
    public int delete(TeamRanking teamRanking) throws SQLException {
        return 0;
    }

    @Override
    public List<TeamRanking> showAll() throws SQLException {
        return List.of();
    }


    public List<TeamRanking> showTeamRanking(int id_team) throws SQLException {
        String req1 = "SELECT id_tournoi FROM ranking WHERE id_team = ?";
        int id_tournoi = -1;

        // Retrieve id_tournoi from ranking table
        try (PreparedStatement pstmt = cnx.prepareStatement(req1)) {
            pstmt.setInt(1, id_team);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    id_tournoi = rs.getInt("id_tournoi");
                }
            }
        }

        // If no tournament is found for the given team, return an empty list
        if (id_tournoi == -1) {
            return new ArrayList<>();
        }

        List<TeamRanking> rankingList = new ArrayList<>();
        String req2 = "SELECT * FROM ranking WHERE id_tournoi = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req2)) {
            pst.setInt(1, id_tournoi);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    // Ensure GetTeamById() is optimized (it can be costly inside loops)
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
