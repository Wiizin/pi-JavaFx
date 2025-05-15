package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.Event;
import io.github.palexdev.materialfx.demo.model.Reservation;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.utils.DbConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {
    private final Connection connection;
    private final EventService eventService;

    public ReservationService() {
        connection = DbConnection.getInstance().getConnection();
        eventService = new EventService();
    }

    public void create(Reservation reservation) throws SQLException {
        String query = "INSERT INTO reservation (id_event, id_player, date, status, comment) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, reservation.getEvent().getId());
            pst.setInt(2, reservation.getUser().getId());
            pst.setTimestamp(3, Timestamp.valueOf(reservation.getDate()));
            pst.setString(4, reservation.getStatus());
            pst.setString(5, reservation.getComment());

            pst.executeUpdate();

            // Get the generated ID
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                reservation.setId(rs.getInt(1));
            }

            // Update event participants count
            Event event = reservation.getEvent();
            event.incrementCurrentParticipants();
            eventService.update(event);
        }
    }

    public void update(Reservation reservation) throws SQLException {
        String query = "UPDATE reservation SET status = ?, comment = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, reservation.getStatus());
            pst.setString(2, reservation.getComment());
            pst.setInt(3, reservation.getId());

            pst.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        // First get the reservation to update event count
        Reservation reservation = getById(id);
        if (reservation != null) {
            Event event = reservation.getEvent();
            event.decrementCurrentParticipants();
            eventService.update(event);
        }

        // Then delete the reservation
        String query = "DELETE FROM reservation WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    public Reservation getById(int id) throws SQLException {
        String query = "SELECT r.*, " +
                "e.id as event_id, e.nom as event_nom, " +
                "u.id as user_id, u.firstname, u.lastname, u.email " +
                "FROM reservation r " +
                "LEFT JOIN events e ON r.id_event = e.id " +
                "LEFT JOIN user u ON r.id_player = u.id " +
                "WHERE r.id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSetToReservation(rs);
            }
        }
        return null;
    }

    public List<Reservation> getAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT r.*, " +
                "e.id as event_id, e.nom as event_nom, " +
                "u.id as user_id, u.firstname, u.lastname, u.email " +
                "FROM reservation r " +
                "LEFT JOIN events e ON r.id_event = e.id " +
                "LEFT JOIN user u ON r.id_player = u.id";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        }
        return reservations;
    }

    public List<Reservation> getByUser(int userId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT r.*, " +
                "e.id as event_id, e.nom as event_nom, " +
                "u.id as user_id, u.firstname, u.lastname, u.email " +
                "FROM reservation r " +
                "LEFT JOIN events e ON r.id_event = e.id " +
                "LEFT JOIN user u ON r.id_player = u.id " +
                "WHERE r.id_player = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        }
        return reservations;
    }

    public List<Reservation> getByEvent(int eventId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT r.*, " +
                "e.id as event_id, e.nom as event_nom, " +
                "u.id as user_id, u.firstname, u.lastname, u.email " +
                "FROM reservation r " +
                "LEFT JOIN events e ON r.id_event = e.id " +
                "LEFT JOIN user u ON r.id_player = u.id " +
                "WHERE r.id_event = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, eventId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        }
        return reservations;
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getInt("id"));
        
        // Map event (basic info only)
        Event event = new Event();
        event.setId(rs.getInt("event_id"));
        event.setNom(rs.getString("event_nom"));
        reservation.setEvent(event);

        // Map user
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setFirstname(rs.getString("firstname"));
        user.setLastName(rs.getString("lastname"));
        user.setEmail(rs.getString("email"));
        reservation.setUser(user);

        // Safely handle date
        try {
            Timestamp date = rs.getTimestamp("date");
            if (date != null) {
                reservation.setDate(date.toLocalDateTime());
            } else {
                reservation.setDate(LocalDateTime.now()); // Default to current time if null
            }
        } catch (SQLException e) {
            // If there's an error reading the date (zero date), set to current time
            reservation.setDate(LocalDateTime.now());
        }

        reservation.setStatus(rs.getString("status"));
        reservation.setComment(rs.getString("comment"));

        return reservation;
    }
} 