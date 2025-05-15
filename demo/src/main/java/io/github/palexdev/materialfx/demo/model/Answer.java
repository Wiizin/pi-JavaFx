package io.github.palexdev.materialfx.demo.model;

public class Answer {
    private int id;
    private Reclamation reclamation;
    private User admin;
    private String message;

    // Default constructor
    public Answer() {
    }

    // Full constructor
    public Answer(int id, Reclamation reclamation, User admin, String message) {
        this.id = id;
        this.reclamation = reclamation;
        this.admin = admin;
        this.message = message;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Reclamation getReclamation() {
        return reclamation;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", reclamation=" + reclamation.getId() + // Only include the ID to avoid circular reference
                ", admin=" + admin +
                ", message='" + message + '\'' +
                '}';
    }
} 