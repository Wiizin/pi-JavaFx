package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.Event;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.utils.DbConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventService {
    private final Connection connection;

    public EventService() {
        connection = DbConnection.getInstance().getConnection();
    }

    public void create(Event event) throws SQLException {
        String query = "INSERT INTO events (nom, description, image, address, latitude, longitude, " +
                "start_time, break_time, end_time, id_organizer, status, created_at, updated_at, " +
                "current_participants, max_participants, event_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, event.getNom());
            pst.setString(2, event.getDescription());
            pst.setString(3, event.getImage());
            pst.setString(4, event.getAddress());
            pst.setDouble(5, event.getLatitude() != null ? event.getLatitude() : 0.0);
            pst.setDouble(6, event.getLongitude() != null ? event.getLongitude() : 0.0);
            
            // Debug time values
            System.out.println("Saving event: " + event.getNom());
            System.out.println("Start time: " + event.getStartTime());
            System.out.println("End time: " + event.getEndTime());
            
            // Ensure we're using Timestamp.valueOf to preserve time component
            pst.setTimestamp(7, event.getStartTime() != null ? Timestamp.valueOf(event.getStartTime()) : null);
            pst.setTimestamp(8, event.getBreakTime() != null ? Timestamp.valueOf(event.getBreakTime()) : null);
            pst.setTimestamp(9, event.getEndTime() != null ? Timestamp.valueOf(event.getEndTime()) : null);
            pst.setInt(10, event.getOrganizer().getId());
            pst.setString(11, event.getStatus());
            pst.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
            pst.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(14, event.getCurrentParticipants());
            pst.setInt(15, event.getMaxParticipants());
            // Use the full date-time for event_date as well
            pst.setTimestamp(16, event.getStartTime() != null ? Timestamp.valueOf(event.getStartTime()) : null);

            int result = pst.executeUpdate();
            System.out.println("Insert result: " + result + " rows affected");

            // Get the generated ID
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                event.setId(rs.getInt(1));
            }
        }
    }

    public void update(Event event) throws SQLException {
        String query = "UPDATE events SET nom = ?, description = ?, image = ?, address = ?, " +
                "latitude = ?, longitude = ?, start_time = ?, break_time = ?, end_time = ?, " +
                "status = ?, updated_at = ?, current_participants = ?, max_participants = ?, " +
                "event_date = ? " +  // Added event_date update
                "WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, event.getNom());
            pst.setString(2, event.getDescription());
            pst.setString(3, event.getImage());
            pst.setString(4, event.getAddress());
            pst.setDouble(5, event.getLatitude() != null ? event.getLatitude() : 0.0);
            pst.setDouble(6, event.getLongitude() != null ? event.getLongitude() : 0.0);
            
            // Debug time values
            System.out.println("Updating event: " + event.getNom());
            System.out.println("Start time: " + event.getStartTime());
            System.out.println("End time: " + event.getEndTime());
            
            // Ensure we're using Timestamp.valueOf to preserve time component
            pst.setTimestamp(7, event.getStartTime() != null ? Timestamp.valueOf(event.getStartTime()) : null);
            pst.setTimestamp(8, event.getBreakTime() != null ? Timestamp.valueOf(event.getBreakTime()) : null);
            pst.setTimestamp(9, event.getEndTime() != null ? Timestamp.valueOf(event.getEndTime()) : null);
            pst.setString(10, event.getStatus());
            pst.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(12, event.getCurrentParticipants());
            pst.setInt(13, event.getMaxParticipants());
            // Use the full date-time for event_date as well
            pst.setTimestamp(14, event.getStartTime() != null ? Timestamp.valueOf(event.getStartTime()) : null);
            pst.setInt(15, event.getId());

            int result = pst.executeUpdate();
            System.out.println("Update result: " + result + " rows affected");
        }
    }

    public void delete(int id) throws SQLException {
        System.out.println("EventService: Starting delete operation for event ID: " + id);
        
        // First delete all reservations for this event
        String deleteReservations = "DELETE FROM reservation WHERE id_event = ?";
        try (PreparedStatement pst = connection.prepareStatement(deleteReservations)) {
            pst.setInt(1, id);
            int reservationsDeleted = pst.executeUpdate();
            System.out.println("EventService: Deleted " + reservationsDeleted + " reservations");
        }

        // Then delete the event
        String deleteEvent = "DELETE FROM events WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(deleteEvent)) {
            pst.setInt(1, id);
            int eventsDeleted = pst.executeUpdate();
            System.out.println("EventService: Deleted " + eventsDeleted + " events");
            
            if (eventsDeleted == 0) {
                System.out.println("EventService: Warning - No event found with ID: " + id);
            }
        }
        
        System.out.println("EventService: Delete operation completed");
    }

    public Event getById(int id) throws SQLException {
        String query = "SELECT e.*, u.id as organizer_id, u.firstname, u.lastname, u.email " +
                "FROM events e " +
                "LEFT JOIN user u ON e.id_organizer = u.id " +
                "WHERE e.id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSetToEvent(rs);
            }
        }
        return null;
    }

    public List<Event> getAll() throws SQLException {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.id as organizer_id, u.firstname, u.lastname, u.email " +
                "FROM events e " +
                "LEFT JOIN user u ON e.id_organizer = u.id";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
        }
        return events;
    }

    public List<Event> getByOrganizer(int organizerId) throws SQLException {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.id as organizer_id, u.firstname, u.lastname, u.email " +
                "FROM events e " +
                "LEFT JOIN users u ON e.id_organizer = u.id " +
                "WHERE e.id_organizer = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, organizerId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
        }
        return events;
    }

    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setId(rs.getInt("id"));
        event.setNom(rs.getString("nom"));
        event.setDescription(rs.getString("description"));
        event.setImage(rs.getString("image"));
        event.setAddress(rs.getString("address"));
        event.setLatitude(rs.getDouble("latitude"));
        event.setLongitude(rs.getDouble("longitude"));
        
        // Debug time values
        System.out.println("Loading event: " + rs.getString("nom"));
        
        Timestamp startTime = rs.getTimestamp("start_time");
        if (startTime != null) {
            LocalDateTime startDateTime = startTime.toLocalDateTime();
            System.out.println("Start time from DB: " + startDateTime);
            event.setStartTime(startDateTime);
        }
        
        Timestamp breakTime = rs.getTimestamp("break_time");
        if (breakTime != null) {
            LocalDateTime breakDateTime = breakTime.toLocalDateTime();
            System.out.println("Break time from DB: " + breakDateTime);
            event.setBreakTime(breakDateTime);
        }
        
        Timestamp endTime = rs.getTimestamp("end_time");
        if (endTime != null) {
            LocalDateTime endDateTime = endTime.toLocalDateTime();
            System.out.println("End time from DB: " + endDateTime);
            event.setEndTime(endDateTime);
        }

        event.setStatus(rs.getString("status"));
        event.setCurrentParticipants(rs.getInt("current_participants"));
        event.setMaxParticipants(rs.getInt("max_participants"));

        // Map organizer
        User organizer = new User();
        organizer.setId(rs.getInt("organizer_id"));
        organizer.setFirstname(rs.getString("firstname"));
        organizer.setLastName(rs.getString("lastname"));
        organizer.setEmail(rs.getString("email"));
        event.setOrganizer(organizer);

        return event;
    }
} 