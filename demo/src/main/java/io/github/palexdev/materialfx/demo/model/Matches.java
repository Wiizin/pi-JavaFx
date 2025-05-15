package io.github.palexdev.materialfx.demo.model;

import java.time.LocalDateTime;

/**
 * Matches model which now includes a fixtureId field for storing the API's unique match identifier.
 */
public class Matches {
    private int id; // Your internal database ID
    private String fixtureId; // The API fixture ID
    private String teamAName;
    private int teamAId;
    private String teamBName;
    private int teamBId;
    private int scoreTeamA;
    private int scoreTeamB;
    private String status;
    private LocalDateTime matchTime;
    private String locationMatch;
    private int idTournoi;

    public Matches() {
    }


    // Constructor including fixtureId and team IDs (for when you retrieve data from the API)
    public Matches(int id, String fixtureId, String teamAName, int teamAId, String teamBName, int teamBId, int scoreTeamA, int scoreTeamB, String status, LocalDateTime matchTime, String locationMatch, int idTournoi) {
        this.id = id;
        this.fixtureId = fixtureId;
        this.teamAName = teamAName;
        this.teamAId = teamAId;
        this.teamBName = teamBName;
        this.teamBId = teamBId;
        this.scoreTeamA = scoreTeamA;
        this.scoreTeamB = scoreTeamB;
        this.status = status;
        this.matchTime = matchTime;
        this.locationMatch = locationMatch;
        this.idTournoi = idTournoi;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFixtureId() {
        return fixtureId;
    }

    public void setFixtureId(String fixtureId) {
        this.fixtureId = fixtureId;
    }

    public int getTeamAId() {
        return this.teamAId;
    }

    public void setTeamAId(int teamAId) {
        this.teamAId = teamAId;
    }

    public int getTeamBId() {
        return this.teamBId;
    }

    public void setTeamBId(int teamBId) {
        this.teamBId = teamBId;
    }

    public int getScoreTeamA() {
        return scoreTeamA;
    }

    public void setScoreTeamA(int scoreTeamA) {
        this.scoreTeamA = scoreTeamA;
    }

    public int getScoreTeamB() {
        return scoreTeamB;
    }

    public void setScoreTeamB(int scoreTeamB) {
        this.scoreTeamB = scoreTeamB;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(LocalDateTime matchTime) {
        this.matchTime = matchTime;
    }

    public String getLocationMatch() {
        return locationMatch;
    }

    public void setLocationMatch(String locationMatch) {
        this.locationMatch = locationMatch;
    }

    public int getIdTournoi() {
        return idTournoi;
    }

    public void setIdTournoi(int idTournoi) {
        this.idTournoi = idTournoi;
    }

    public String getTeamAName() {
        return teamAName;
    }

    public void setTeamAName(String teamAName) {
        this.teamAName = teamAName;
    }

    public String getTeamBName() {
        return teamBName;
    }

    public void setTeamBName(String teamBName) {
        this.teamBName = teamBName;
    }

    public Matches(int id, String teamAName, String teamBName, int scoreTeamA, int scoreTeamB, String status, LocalDateTime matchTime, String locationMatch, int idTournoi) {
        this.id = id;
        this.teamAName = teamAName;
        this.teamBName = teamBName;
        this.scoreTeamA = scoreTeamA;
        this.scoreTeamB = scoreTeamB;
        this.status = status;
        this.matchTime = matchTime;
        this.locationMatch = locationMatch;
        this.idTournoi = idTournoi;
    }
    public Matches(int id, String teamAName, String teamBName, int scoreTeamA, int scoreTeamB, String status, LocalDateTime matchTime, String locationMatch, int idTournoi,int idTeamA,int idTeamB) {
        this.id = id;
        this.teamAName = teamAName;
        this.teamBName = teamBName;
        this.scoreTeamA = scoreTeamA;
        this.scoreTeamB = scoreTeamB;
        this.status = status;
        this.matchTime = matchTime;
        this.locationMatch = locationMatch;
        this.idTournoi = idTournoi;
        this.teamAId = idTeamA;
        this.teamBId=idTeamB;
    }

}
