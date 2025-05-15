package io.github.palexdev.materialfx.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private Integer id;
    private String nom;
    private String description;
    private String image;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDateTime startTime;
    private LocalDateTime breakTime;
    private LocalDateTime endTime;
    private User organizer;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<User> participants;
    private List<Reservation> reservations;
    private Integer currentParticipants;
    private Integer maxParticipants;

    public Event() {
        this.participants = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.currentParticipants = 0;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getBreakTime() {
        return breakTime;
    }

    public void setBreakTime(LocalDateTime breakTime) {
        this.breakTime = breakTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    // Additional utility methods
    public void addParticipant(User user) {
        if (!this.participants.contains(user)) {
            this.participants.add(user);
            incrementCurrentParticipants();
        }
    }

    public void removeParticipant(User user) {
        if (this.participants.remove(user)) {
            decrementCurrentParticipants();
        }
    }

    public void addReservation(Reservation reservation) {
        if (!this.reservations.contains(reservation)) {
            this.reservations.add(reservation);
            reservation.setEvent(this);
        }
    }

    public void removeReservation(Reservation reservation) {
        if (this.reservations.remove(reservation)) {
            reservation.setEvent(null);
        }
    }

    public void incrementCurrentParticipants() {
        this.currentParticipants++;
    }

    public void decrementCurrentParticipants() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }

    public boolean isEventFull() {
        return this.maxParticipants != null && this.currentParticipants >= this.maxParticipants;
    }

    public Integer getRemainingSpots() {
        if (this.maxParticipants == null) {
            return null; // Unlimited spots
        }
        return this.maxParticipants - this.currentParticipants;
    }

    @Override
    public String toString() {
        return nom != null ? nom : "Event #" + id;
    }
} 