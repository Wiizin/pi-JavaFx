package io.github.palexdev.materialfx.demo.model;

public class TeamRanking {
        private int id;
        private int idTeam;
        private int points;
        private int position;
        private int idTournoi;
        private int wins;
        private int draws;
        private int losses;
        private int goalsScored;
        private int goalsConceded;
        private int goalDifference;
        private Team team;

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    
        public TeamRanking() {}
        public TeamRanking(int id, int idTeam, int idTournoi){
            this.id = id;
            this.idTeam = idTeam;
            this.idTournoi = idTournoi;
        }
        public TeamRanking(int id, int idTeam, int points, int position,  int idTournoi,int wins, int draws, int losses, int goalsScored, int goalsConceded, int goalDifference, Team team) {
            this.id = id;
            this.idTeam = idTeam;
            this.points = points;
            this.position = position;
            this.wins = wins;
            this.draws = draws;
            this.losses = losses;
            this.goalsScored = goalsScored;
            this.goalsConceded = goalsConceded;
            this.goalDifference = goalDifference;
            this.idTournoi = idTournoi;
            this.team = team;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getIdTeam() {
            return idTeam;
        }

        public void setIdTeam(int idTeam) {
            this.idTeam = idTeam;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public int getIdTournoi() {
            return idTournoi;
        }

        public void setIdTournoi(int idTournoi) {
            this.idTournoi = idTournoi;
        }

        public int getWins() {
            return wins;
        }

        public void setWins(int wins) {
            this.wins = wins;
        }

        public int getDraws() {
            return draws;
        }

        public void setDraws(int draws) {
            this.draws = draws;
        }

        public int getLosses() {
            return losses;
        }

        public void setLosses(int losses) {
            this.losses = losses;
        }

        public int getGoalsScored() {
            return goalsScored;
        }

        public void setGoalsScored(int goalsScored) {
            this.goalsScored = goalsScored;
        }

        public int getGoalsConceded() {
            return goalsConceded;
        }

        public void setGoalsConceded(int goalsConceded) {
            this.goalsConceded = goalsConceded;
        }

        public int getGoalDifference() {
            return goalDifference;
        }

        public void setGoalDifference(int goalDifference) {
            this.goalDifference = goalDifference;
        }

    }

