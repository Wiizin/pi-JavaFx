package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.utils.DbConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User>{
    private Connection connection;


    public UserService(){
        this.connection= DbConnection.getInstance().getCnx();
    }

    public  void create(User user) {
        Connection conn = DbConnection.getInstance().getCnx();
        String sql = "INSERT INTO user (firstname, lastname, email, password, role, phonenumber, dateofbirth, profilepicture, createdat, updatedat) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getFirstname());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword()); // Use password hashing
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getPhoneNumber());
            pstmt.setDate(7, Date.valueOf(user.getDateOfBirth()));
            pstmt.setString(8, user.getProfilePicture());

            LocalDateTime now = LocalDateTime.now();
            pstmt.setTimestamp(9, Timestamp.valueOf(now));
            pstmt.setTimestamp(10, Timestamp.valueOf(now));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            // Log error
            e.printStackTrace();
        }
    }



    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastName(rs.getString("lastname"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password")); // You may not need to display this
                user.setRole(rs.getString("role"));
                user.setPhoneNumber(rs.getString("phonenumber"));
                user.setDateOfBirth(rs.getDate("dateofbirth").toLocalDate());
                user.setProfilePicture(rs.getString("profilepicture"));
                user.setCreatedAt(rs.getTimestamp("createdat").toLocalDateTime());
                user.setUpdatedAt(rs.getTimestamp("updatedat").toLocalDateTime());

                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public User getOneById() throws SQLException {
        return null;
    }


    public void update(User user) {
        String sql = "UPDATE user SET firstname=?, lastname=?, email=?, password=?, role=?, phonenumber=?, dateofbirth=?, profilepicture=?, updatedat=? WHERE id=?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getFirstname());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword()); // Hash password before saving
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getPhoneNumber());
            pstmt.setDate(7, Date.valueOf(user.getDateOfBirth()));
            pstmt.setString(8, user.getProfilePicture());
            pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(10, user.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id); // Set the user ID for deletion
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Deleting user failed, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFirstname(rs.getString("firstname"));
                    user.setLastName(rs.getString("lastname"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password")); // Avoid returning the password in production
                    user.setRole(rs.getString("role"));
                    user.setPhoneNumber(rs.getString("phonenumber"));
                    user.setDateOfBirth(rs.getDate("dateofbirth").toLocalDate());
                    user.setProfilePicture(rs.getString("profilepicture"));
                    user.setCreatedAt(rs.getTimestamp("createdat").toLocalDateTime());
                    user.setUpdatedAt(rs.getTimestamp("updatedat").toLocalDateTime());

                    return user;
                }
            }
        }
        return null; // Return null if no user is found
    }


    public boolean isEmailUnique(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count == 0; // Returns true if email is unique (count = 0)
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // In case of database error, you might want to handle this differently
            // For now, we'll return false to prevent the creation of potentially duplicate emails
            return false;
        }

        return false; // Default to false for safety
    }





}
