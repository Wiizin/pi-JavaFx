package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.Answer;
import io.github.palexdev.materialfx.demo.model.Reclamation;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.utils.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnswerService implements IService<Answer> {
    private Connection conn;
    private PreparedStatement pst;
    private Statement st;
    private ResultSet rs;

    public AnswerService() {
        conn = DbConnection.getInstance().getCnx();
    }

    @Override
    public void create(Answer answer) throws SQLException {
        String req = "INSERT INTO answer (id_reclamation, id_admin, message) VALUES (?, ?, ?)";
        pst = conn.prepareStatement(req);
        pst.setInt(1, answer.getReclamation().getId());
        pst.setInt(2, answer.getAdmin().getId());
        pst.setString(3, answer.getMessage());
        pst.executeUpdate();
        
        // Update reclamation status when answer is created
        updateReclamationStatus(answer.getReclamation().getId());
    }

    @Override
    public void update(Answer answer) throws SQLException {
        String req = "UPDATE answer SET message = ? WHERE id = ?";
        pst = conn.prepareStatement(req);
        pst.setString(1, answer.getMessage());
        pst.setInt(2, answer.getId());
        pst.executeUpdate();
    }

    @Override
    public void delete(int id) throws SQLException {
        // Get the reclamation ID before deleting the answer
        int reclamationId = getReclamationIdForAnswer(id);
        
        String req = "DELETE FROM answer WHERE id = ?";
        pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        
        // Update reclamation status after answer is deleted
        if (reclamationId > 0) {
            updateReclamationStatus(reclamationId);
        }
    }

    private int getReclamationIdForAnswer(int answerId) throws SQLException {
        String req = "SELECT id_reclamation FROM answer WHERE id = ?";
        pst = conn.prepareStatement(req);
        pst.setInt(1, answerId);
        rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("id_reclamation");
        }
        return -1;
    }

    private void updateReclamationStatus(int reclamationId) throws SQLException {
        // Check if reclamation has any answers
        String checkReq = "SELECT COUNT(*) as answer_count FROM answer WHERE id_reclamation = ?";
        pst = conn.prepareStatement(checkReq);
        pst.setInt(1, reclamationId);
        rs = pst.executeQuery();
        
        String newStatus = Reclamation.STATUS_PENDING;
        if (rs.next() && rs.getInt("answer_count") > 0) {
            newStatus = Reclamation.STATUS_RESOLVED;
        }
        
        // Update reclamation status
        String updateReq = "UPDATE reclamation SET status = ? WHERE id = ?";
        pst = conn.prepareStatement(updateReq);
        pst.setString(1, newStatus);
        pst.setInt(2, reclamationId);
        pst.executeUpdate();
    }

    @Override
    public List<Answer> getAll() throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String req = "SELECT a.*, r.*, u.* FROM answer a " +
                    "JOIN reclamation r ON a.id_reclamation = r.id " +
                    "JOIN user u ON a.id_admin = u.id";
        st = conn.createStatement();
        rs = st.executeQuery(req);
        
        while (rs.next()) {
            answers.add(createAnswerFromResultSet(rs));
        }
        return answers;
    }

    @Override
    public Answer getOneById() throws SQLException {
        throw new UnsupportedOperationException("Use getOneById(int id) instead");
    }

    public Answer getOneById(int id) throws SQLException {
        String req = "SELECT a.*, r.*, u.* FROM answer a " +
                    "JOIN reclamation r ON a.id_reclamation = r.id " +
                    "JOIN user u ON a.id_admin = u.id " +
                    "WHERE a.id = ?";
        pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        rs = pst.executeQuery();
        
        if (rs.next()) {
            return createAnswerFromResultSet(rs);
        }
        return null;
    }

    public List<Answer> getAnswersByReclamation(int reclamationId) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String req = "SELECT a.*, r.*, u.* FROM answer a " +
                    "JOIN reclamation r ON a.id_reclamation = r.id " +
                    "JOIN user u ON a.id_admin = u.id " +
                    "WHERE a.id_reclamation = ?";
        pst = conn.prepareStatement(req);
        pst.setInt(1, reclamationId);
        rs = pst.executeQuery();
        
        while (rs.next()) {
            answers.add(createAnswerFromResultSet(rs));
        }
        return answers;
    }

    private Answer createAnswerFromResultSet(ResultSet rs) throws SQLException {
        Answer answer = new Answer();
        answer.setId(rs.getInt("a.id"));
        answer.setMessage(rs.getString("a.message"));

        // Create reclamation object
        Reclamation reclamation = new Reclamation();
        reclamation.setId(rs.getInt("r.id"));
        reclamation.setMessage(rs.getString("r.message"));
        reclamation.setCreatedAt(rs.getTimestamp("r.created_at").toLocalDateTime());
        reclamation.setStatus(rs.getString("r.status"));
        answer.setReclamation(reclamation);

        // Create admin user object
        User admin = new User();
        admin.setId(rs.getInt("u.id"));
        admin.setFirstname(rs.getString("u.firstname"));
        admin.setLastName(rs.getString("u.lastName"));
        admin.setEmail(rs.getString("u.email"));
        answer.setAdmin(admin);

        return answer;
    }
} 