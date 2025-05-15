package io.github.palexdev.materialfx.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class Reclamation {
    public static final String STATUS_PENDING = "En attente";
    public static final String STATUS_RESOLVED = "Resolu";

    private int id;
    private User user;
    private String message;
    private LocalDateTime createdAt;
    private String status;
    private List<Answer> answers;

    // Default constructor
    public Reclamation() {
        this.status = STATUS_PENDING;
        this.answers = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    // Full constructor
    public Reclamation(int id, User user, String message, LocalDateTime createdAt, String status) {
        this.id = id;
        this.user = user;
        this.message = message;
        this.createdAt = createdAt;
        this.status = status;
        this.answers = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public void addAnswer(Answer answer) {
        if (!this.answers.contains(answer)) {
            this.answers.add(answer);
            this.status = STATUS_RESOLVED;
        }
    }

    public void removeAnswer(Answer answer) {
        if (this.answers.remove(answer)) {
            if (this.answers.isEmpty()) {
                this.status = STATUS_PENDING;
            }
        }
    }

    public boolean hasFilteredContent() {
        if (this.message == null) {
            return false;
        }
        return this.message.contains("***");
    }

    public int getFilteredWordsCount() {
        if (this.message == null) {
            return 0;
        }
        String[] words = this.message.split("\\s+");
        return (int) Arrays.stream(words)
                .filter(word -> word.matches("\\*{3,}"))
                .count();
    }

    public String getStatusColor() {
        return switch (this.status) {
            case STATUS_RESOLVED -> "success";
            case STATUS_PENDING -> "warning";
            default -> "info";
        };
    }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id +
                ", user=" + user +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                ", answers=" + answers +
                '}';
    }
} 