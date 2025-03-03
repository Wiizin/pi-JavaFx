package io.github.palexdev.materialfx.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Player extends User {
    private int rating; // Changed to camelCase
    private String position;

    // Default Constructor
    public Player() {
        super(); // Call the User default constructor
        this.rating = 69; // Initialize with a default value
        this.position = ""; // Initialize with a default value
        setActive(true); // Override the default active status - players start active
    }

    // Full Constructor
    public Player(int id, String firstname, String lastName, String email, String password,
                  String role, String phoneNumber, LocalDate dateOfBirth, String profilePicture,
                  LocalDateTime createdAt, LocalDateTime updatedAt, int idTeam, int rating, String position) {
        super(id, firstname, lastName, email, password, role, phoneNumber, dateOfBirth, profilePicture, createdAt, updatedAt, idTeam);
        this.rating = rating;
        this.position = position;
        setActive(true); // Override the default active status - players start active
    }

    // Getters and Setters
    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    // toString Method
    @Override
    public String toString() {
        return super.toString() + // Include fields from the User class
                ", Player{" +
                "rating=" + rating +
                ", position='" + position + '\'' +
                '}';
    }
}