package io.github.palexdev.materialfx.demo.model;

import java.time.LocalDateTime;


public class Matches {
    private int id;
    private String teamAName;
    private int idTeamA;
    private String teamBName;
    private int idTeamB;
    private int scoreTeamA;
    private int scoreTeamB;
    private String status;
    private LocalDateTime matchTime;

    private String locationMatch;
    private int idTournoi;
    public Matches() {
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getIdTeamA() {
        return idTeamA;
    }
    public void setIdTeamA(int idTeamA) {
        this.idTeamA = idTeamA;
    }
    public int getIdTeamB() {
        return idTeamB;
    }
    public void setIdTeamB(int idTeamB) {
        this.idTeamB = idTeamB;
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
        this.idTeamA = idTeamA;
        this.idTeamB=idTeamB;
    }

}