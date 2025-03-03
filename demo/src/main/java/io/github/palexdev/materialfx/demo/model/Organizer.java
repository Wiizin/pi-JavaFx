package io.github.palexdev.materialfx.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Organizer extends User {
    private String coachingLicense; // Coaching license number
    private Team team;
    // Default Constructor
    public Organizer() {
        super(); // Call the User default constructor
        setActive(false); // Override the default active status - organizers start inactive
    }

    // Full Constructor
    public Organizer(int id, String firstname, String lastName, String email, String password,
                     String role, String phoneNumber, LocalDate dateOfBirth, String profilePicture,int id_team,
                     LocalDateTime createdAt, LocalDateTime updatedAt,
                     String coachingLicense) {
        super(id, firstname, lastName, email, password, role, phoneNumber, dateOfBirth, profilePicture, createdAt, updatedAt,id_team);
        setActive(false); // Override the default active status - organizers start inactive
        this.coachingLicense = coachingLicense;
    }

    public String getCoachingLicense() {
        return coachingLicense;
    }

    public void setCoachingLicense(String coachingLicense) {
        this.coachingLicense = coachingLicense;
    }
    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
    @Override
    public String toString() {
        return super.toString() + // Include fields from the User class
                ", Organizer{" +
                "coachingLicense='" + coachingLicense + '\'' +
                '}';
    }
}