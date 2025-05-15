package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.Reclamation;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.utils.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService implements IService<Reclamation> {
    private Connection conn;
    private PreparedStatement pst;
    private Statement st;
    private ResultSet rs;

    public ReclamationService() {
        conn = DbConnection.getInstance().getCnx();
    }

    @Override
    public void create(Reclamation reclamation) throws SQLException {
        String req = "INSERT INTO reclamation (id_player, message, created_at, status) VALUES (?, ?, ?, ?)";
        pst = conn.prepareStatement(req);
        pst.setInt(1, reclamation.getUser().getId());
        pst.setString(2, reclamation.getMessage());
        pst.setTimestamp(3, Timestamp.valueOf(reclamation.getCreatedAt()));
        pst.setString(4, reclamation.getStatus());
        pst.executeUpdate();
    }

    @Override
    public void update(Reclamation reclamation) throws SQLException {
        String req = "UPDATE reclamation SET message = ?, status = ? WHERE id = ?";
        pst = conn.prepareStatement(req);
        pst.setString(1, reclamation.getMessage());
        pst.setString(2, reclamation.getStatus());
        pst.setInt(3, reclamation.getId());
        pst.executeUpdate();
    }

    @Override
    public void delete(int id) throws SQLException {
        String req = "DELETE FROM reclamation WHERE id = ?";
        pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    @Override
    public List<Reclamation> getAll() throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String req = "SELECT r.*, u.* FROM reclamation r JOIN user u ON r.id_player = u.id";
        st = conn.createStatement();
        rs = st.executeQuery(req);
        
        while (rs.next()) {
            Reclamation reclamation = new Reclamation();
            reclamation.setId(rs.getInt("r.id"));
            reclamation.setMessage(rs.getString("r.message"));
            reclamation.setCreatedAt(rs.getTimestamp("r.created_at").toLocalDateTime());
            reclamation.setStatus(rs.getString("r.status"));
            
            // Create user object
            User user = new User();
            user.setId(rs.getInt("u.id"));
            user.setFirstname(rs.getString("u.firstname"));
            user.setLastName(rs.getString("u.lastName"));
            user.setEmail(rs.getString("u.email"));
            reclamation.setUser(user);
            
            // Load answers for this reclamation
            loadAnswersForReclamation(reclamation);
            
            reclamations.add(reclamation);
        }
        return reclamations;
    }

    @Override
    public Reclamation getOneById() throws SQLException {
        throw new UnsupportedOperationException("Use getOneById(int id) instead");
    }

    public Reclamation getOneById(int id) throws SQLException {
        String req = "SELECT r.*, u.* FROM reclamation r JOIN user u ON r.id_player = u.id WHERE r.id = ?";
        pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        rs = pst.executeQuery();
        
        if (rs.next()) {
            Reclamation reclamation = new Reclamation();
            reclamation.setId(rs.getInt("r.id"));
            reclamation.setMessage(rs.getString("r.message"));
            reclamation.setCreatedAt(rs.getTimestamp("r.created_at").toLocalDateTime());
            reclamation.setStatus(rs.getString("r.status"));
            
            // Create user object
            User user = new User();
            user.setId(rs.getInt("u.id"));
            user.setFirstname(rs.getString("u.firstname"));
            user.setLastName(rs.getString("u.lastName"));
            user.setEmail(rs.getString("u.email"));
            reclamation.setUser(user);
            
            // Load answers for this reclamation
            loadAnswersForReclamation(reclamation);
            
            return reclamation;
        }
        return null;
    }

    private void loadAnswersForReclamation(Reclamation reclamation) throws SQLException {
        AnswerService answerService = new AnswerService();
        reclamation.setAnswers(answerService.getAnswersByReclamation(reclamation.getId()));
    }

    public List<Reclamation> getReclamationsByUser(int userId) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String req = "SELECT r.*, u.* FROM reclamation r JOIN user u ON r.id_player = u.id WHERE r.id_player = ?";
        pst = conn.prepareStatement(req);
        pst.setInt(1, userId);
        rs = pst.executeQuery();
        
        while (rs.next()) {
            Reclamation reclamation = new Reclamation();
            reclamation.setId(rs.getInt("r.id"));
            reclamation.setMessage(rs.getString("r.message"));
            reclamation.setCreatedAt(rs.getTimestamp("r.created_at").toLocalDateTime());
            reclamation.setStatus(rs.getString("r.status"));
            
            // Create user object
            User user = new User();
            user.setId(rs.getInt("u.id"));
            user.setFirstname(rs.getString("u.firstname"));
            user.setLastName(rs.getString("u.lastName"));
            user.setEmail(rs.getString("u.email"));
            reclamation.setUser(user);
            
            // Load answers for this reclamation
            loadAnswersForReclamation(reclamation);
            
            reclamations.add(reclamation);
        }
        return reclamations;
    }
} 