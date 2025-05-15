package io.github.palexdev.materialfx.demo.model;

import java.time.LocalDateTime;

public class Reservation {
    private Integer id;
    private Event event;
    private User user;
    private LocalDateTime date;
    private String status = "pending";  // Default value
    private String comment;

    public Reservation() {
        this.date = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getStatus() {
        return status != null ? status : "pending";
    }

    public void setStatus(String status) {
        this.status = status != null ? status : "pending";
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", event=" + event +
                ", user=" + user +
                ", date=" + date +
                ", status='" + status + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
} 