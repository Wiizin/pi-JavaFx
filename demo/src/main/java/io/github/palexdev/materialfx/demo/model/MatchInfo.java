package io.github.palexdev.materialfx.demo.model;

public class MatchInfo {
    private String homeTeam;
    private String awayTeam;
    private String time;
    private String league;
    private String venue;
    private boolean isLive;
    private String homeTeamCode;
    private String awayTeamCode;
    private int homeTeamId;
    private int awayTeamId;
    private String fixtureId;  // The real fixture id from the API
    private int homeScore;     // New field for the home team's score
    private int awayScore;     // New field for the away team's score
    private String status;     // New field for match status (e.g., "Live", "Finished", "Upcoming")

    public MatchInfo(String homeTeam, String awayTeam, String time, String league, String venue, boolean isLive,
                     String homeTeamCode, String awayTeamCode, int homeTeamId, int awayTeamId,
                     String fixtureId, int homeScore, int awayScore, String status) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.time = time;
        this.league = league;
        this.venue = venue;
        this.isLive = isLive;
        this.homeTeamCode = homeTeamCode;
        this.awayTeamCode = awayTeamCode;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.fixtureId = fixtureId;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.status = status;
    }

    // Getters and Setters
    public String getHomeTeam() {
        return homeTeam;
    }
    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }
    public String getAwayTeam() {
        return awayTeam;
    }
    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getLeague() {
        return league;
    }
    public void setLeague(String league) {
        this.league = league;
    }
    public String getVenue() {
        return venue;
    }
    public void setVenue(String venue) {
        this.venue = venue;
    }
    public boolean isLive() {
        return isLive;
    }
    public void setLive(boolean live) {
        isLive = live;
    }
    public String getHomeTeamCode() {
        return homeTeamCode;
    }
    public void setHomeTeamCode(String homeTeamCode) {
        this.homeTeamCode = homeTeamCode;
    }
    public String getAwayTeamCode() {
        return awayTeamCode;
    }
    public void setAwayTeamCode(String awayTeamCode) {
        this.awayTeamCode = awayTeamCode;
    }
    public int getHomeTeamId() {
        return homeTeamId;
    }
    public void setHomeTeamId(int homeTeamId) {
        this.homeTeamId = homeTeamId;
    }
    public int getAwayTeamId() {
        return awayTeamId;
    }
    public void setAwayTeamId(int awayTeamId) {
        this.awayTeamId = awayTeamId;
    }
    public String getFixtureId() {
        return fixtureId;
    }
    public void setFixtureId(String fixtureId) {
        this.fixtureId = fixtureId;
    }
    public int getHomeScore() {
        return homeScore;
    }
    public void setHomeScore(int homeScore) {
        this.homeScore = homeScore;
    }
    public int getAwayScore() {
        return awayScore;
    }
    public void setAwayScore(int awayScore) {
        this.awayScore = awayScore;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
