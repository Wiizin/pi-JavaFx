package io.github.palexdev.materialfx.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Organizer extends User {
    private String coachingLicense; // Coaching license number

    // Default Constructor
    public Organizer() {
        super(); // Call the User default constructor
        setActive(false); // Override the default active status - organizers start inactive
    }

    // Full Constructor
    public Organizer(int id, String firstname, String lastName, String email, String password,
                     String role, String phoneNumber, LocalDate dateOfBirth, byte[] profilePicture,
                     LocalDateTime createdAt, LocalDateTime updatedAt,
                     String coachingLicense) {
        super(id, firstname, lastName, email, password, role, phoneNumber, dateOfBirth, profilePicture, createdAt, updatedAt);
        setActive(false); // Override the default active status - organizers start inactive
        this.coachingLicense = coachingLicense;
    }

    public String getCoachingLicense() {
        return coachingLicense;
    }

    public void setCoachingLicense(String coachingLicense) {
        this.coachingLicense = coachingLicense;
    }

    @Override
    public String toString() {
        return super.toString() + // Include fields from the User class
                ", Organizer{" +
                "coachingLicense='" + coachingLicense + '\'' +
                '}';
    }
}