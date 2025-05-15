package io.github.palexdev.materialfx.demo.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Player extends User {
    private int rating; // Changed to camelCase
    private String position;
    private final BooleanProperty selected = new SimpleBooleanProperty();

    private boolean Favourite;
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
        this.selected.set(false);
        this.Favourite=false;
    }
    public Player(int id, String firstname, String lastName, String email, String password,
                  String role, String phoneNumber, LocalDate dateOfBirth, String profilePicture,
                  LocalDateTime createdAt, LocalDateTime updatedAt, int idTeam, int rating, String position,boolean Favourite) {
        super(id, firstname, lastName, email, password, role, phoneNumber, dateOfBirth, profilePicture, createdAt, updatedAt, idTeam);
        this.rating = rating;
        this.position = position;
        setActive(true); // Override the default active status - players start active
        this.selected.set(false);
        this.Favourite = Favourite;
    }
    public boolean isFavourite() {
        return Favourite;
    }

    public void setFavourite(boolean favourite) {
        Favourite = favourite;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
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