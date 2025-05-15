package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.*;
import io.github.palexdev.materialfx.demo.utils.DbConnection;

import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {
    private Connection connection;
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    public UserService() {
        this.connection = DbConnection.getInstance().getCnx();
        if (connection == null) {
            System.err.println("Failed to connect to the database.");
        } else {
            System.out.println("Database connection established.");
        }
    }

    public void create(User user) {
        Connection conn = DbConnection.getInstance().getCnx();
        String sql = "INSERT INTO user (firstname, lastname, email, password, role, phonenumber, dateofbirth, profilepicture, createdat, updatedat, is_active, coachinglicense) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getFirstname());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getPhoneNumber());
            pstmt.setDate(7, Date.valueOf(user.getDateOfBirth()));
            pstmt.setString(8, user.getProfilePicture());

            LocalDateTime now = LocalDateTime.now();
            pstmt.setTimestamp(9, Timestamp.valueOf(now));
            pstmt.setTimestamp(10, Timestamp.valueOf(now));

            // Set active status based on role
            if ("organizer".equalsIgnoreCase(user.getRole())) {
                pstmt.setBoolean(11, false); // Organizers start as inactive, needing admin approval
                Organizer organizer = (Organizer) user;
                pstmt.setString(12, organizer.getCoachingLicense());
            } else {
                pstmt.setBoolean(11, true); // Admin and players start as active
                pstmt.setString(12, null); // No coaching license for non-organizers
            }

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
            e.printStackTrace();
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user;
                String role = rs.getString("role");
                boolean isActive = rs.getBoolean("isactive");

                // Create appropriate user type based on role
                if ("organizer".equalsIgnoreCase(role)) {
                    Organizer organizer = new Organizer();
                    organizer.setCoachingLicense(rs.getString("coachinglicense"));
                    organizer.setActive(isActive);
                    user = organizer;
                } else {
                    user = new User();
                    user.setActive(true);
                }

                // Set common fields
                user.setId(rs.getInt("id"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastName(rs.getString("lastname"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(role);
                user.setPhoneNumber(rs.getString("phonenumber"));
                user.setDateOfBirth(rs.getDate("dateofbirth").toLocalDate());
                user.setProfilePicture(rs.getString("profilepicture"));
                user.setCreatedAt(rs.getTimestamp("createdat").toLocalDateTime());
                user.setUpdatedAt(rs.getTimestamp("updatedat").toLocalDateTime());
                user.setIdteam(rs.getInt("id_team"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    public List<Player> getAvailablePlayers() {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * \n" +
                "FROM user \n" +
                "WHERE (id_team = 0 OR id_team IS NULL) \n" +
                "  AND role = 'player' \n" +
                "  AND is_active = 1;";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Player player = new Player(
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("phonenumber"),
                        rs.getDate("dateofbirth") != null ? rs.getDate("dateofbirth").toLocalDate() : null,
                        rs.getString("profilepicture"),
                        rs.getTimestamp("createdat") != null ? rs.getTimestamp("createdat").toLocalDateTime() : null,
                        rs.getTimestamp("updatedat") != null ? rs.getTimestamp("updatedat").toLocalDateTime() : null,
                        rs.getInt("id_team"),
                        rs.getInt("Rating"),
                        rs.getString("position")
                );
                players.add(player);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging the exception instead of printing the stack trace
        }
        return players;
    }

    @Override
    public User getOneById() throws SQLException {
        return null;
    }

    public void update(User user) {
        String sql = "UPDATE user SET firstname=?, lastname=?, email=?, password=?, role=?, phonenumber=?, dateofbirth=?, profilepicture=?, updatedat=?, is_active=?, coachinglicense=?, id_team=? WHERE id=?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Verify that the team exists
            int idTeam = user.getIdteam();
            if (idTeam > 0) {
                String teamCheckSql = "SELECT COUNT(*) FROM team WHERE id = ?";
                try (PreparedStatement teamCheckStmt = connection.prepareStatement(teamCheckSql)) {
                    teamCheckStmt.setInt(1, idTeam);
                    ResultSet rs = teamCheckStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new SQLException("Team with ID " + idTeam + " does not exist.");
                    }
                }
            }

            // Set user fields
            pstmt.setString(1, user.getFirstname());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getPhoneNumber());
            pstmt.setDate(7, Date.valueOf(user.getDateOfBirth()));
            pstmt.setString(8, user.getProfilePicture());
            pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));

            // Handle active status based on role
            if ("organizer".equalsIgnoreCase(user.getRole())) {
                boolean isActive = user.isActive();
                System.out.println("Updating organizer active status to: " + isActive);
                pstmt.setBoolean(10, isActive);
            } else {
                pstmt.setBoolean(10, true);
            }

            // Handle coaching license
            if (user instanceof Organizer) {
                String license = ((Organizer) user).getCoachingLicense();
                System.out.println("Setting coaching license: " + license);
                pstmt.setString(11, license);
            } else {
                pstmt.setString(11, ""); // Provide a default value (empty string)
            }

            pstmt.setInt(12, user.getIdteam());
            pstmt.setInt(13, user.getId());

            System.out.println("Executing update for user ID: " + user.getId());
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected by update: " + rowsAffected);

            if (rowsAffected == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update user: " + e.getMessage());
        }
    }

    public void delete(int id) throws SQLException {
        // First verify if the user exists
        String checkSql = "SELECT COUNT(*) FROM user WHERE id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                throw new SQLException("User with ID " + id + " does not exist");
            }
        }

        // If user exists, proceed with deletion
        String sql = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Deleting user failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new SQLException("Error deleting user: " + e.getMessage());
        }
    }

    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user;
                    String role = rs.getString("role");
                    int isActiveIn = rs.getInt("is_active");
                    boolean isActive;
                    if(isActiveIn==1) {
                         isActive = true;
                    }else{
                        isActive = false;
                    }
                    // Create appropriate user type based on role
                    if ("organizer".equalsIgnoreCase(role)) {
                        Organizer organizer = new Organizer();
                        organizer.setCoachingLicense(rs.getString("coachinglicense"));
                        if(organizer.getIdteam()!=0){
                            Team equipe = null;
                            String req2 = "SELECT * FROM Team WHERE id = ?";

                            try (PreparedStatement pst = connection.prepareStatement(req2)) {
                                pst.setInt(1, organizer.getIdteam());

                                try (ResultSet rs2 = pst.executeQuery()) {
                                    if (rs2.next()) {
                                        equipe = new Team(
                                                rs2.getInt("id"),
                                                rs2.getString("nom"),
                                                rs2.getString("categorie"),
                                                ModeJeu.valueOf(rs2.getString("modeJeu")),
                                                rs2.getInt("nombreJoueurs"),
                                                rs2.getString("logoPath")
                                        );
                                    }
                                }
                            }
                            List<User> members = new ArrayList<>();
                            String req3 = "SELECT * FROM user WHERE id_team = ? AND id != ?";

                            try (PreparedStatement pst = connection.prepareStatement(req3)) {
                                pst.setInt(1, organizer.getIdteam());
                                pst.setInt(2, organizer.getId());
                                try (ResultSet rs3 = pst.executeQuery()) {
                                    while (rs3.next()) {
                                        User member = new User(
                                            rs3.getInt("id"),
                                            rs3.getString("firstname"),
                                            rs3.getString("lastname"), 
                                            rs3.getString("email"),
                                            rs3.getString("password"),
                                            rs3.getString("role"),
                                            rs3.getString("phonenumber"),
                                            rs3.getDate("dateofbirth").toLocalDate(),
                                            rs3.getString("profilepicture"),
                                            rs3.getTimestamp("createdat").toLocalDateTime(),
                                            rs3.getTimestamp("updatedat").toLocalDateTime(),
                                            rs3.getInt("id_team")
                                        );
                                        member.setActive(rs3.getBoolean("isActive"));
                                        members.add(member);
                                    }
                                }
                            }
                            equipe.setMembres(members);
                            organizer.setTeam(equipe);
                        }
                        user = organizer;
                    } else {
                        user = new User();
                    }

                    // Set common fields
                    user.setId(rs.getInt("id"));
                    user.setFirstname(rs.getString("firstname"));
                    user.setLastName(rs.getString("lastname"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(role);
                    user.setPhoneNumber(rs.getString("phonenumber"));
                    user.setDateOfBirth(rs.getDate("dateofbirth").toLocalDate());
                    user.setProfilePicture(rs.getString("profilepicture"));
                    user.setCreatedAt(rs.getTimestamp("createdat").toLocalDateTime());
                    user.setUpdatedAt(rs.getTimestamp("updatedat").toLocalDateTime());
                    user.setIdteam(rs.getInt("id_team"));
                    user.setActive(isActive);

                    return user;
                }
            }
        }
        return null;
    }

    public boolean isEmailUnique(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    /////////////////reset password

    public String generateResetCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return code.toString();
    }

    // Store the reset code in the database along with expiration time
    public boolean storeResetCodeInDB(String email, String resetCode) {
        String sql = "UPDATE user SET reset_code = ?, reset_code_expiry = ? WHERE email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, resetCode);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().plusMinutes(1))); // Code expires in 15 minutes
            pstmt.setString(3, email);

            int updatedRows = pstmt.executeUpdate();
            return updatedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendResetEmail(String email, String resetCode) {
        String subject = "Password Reset Code";
        String body = "Hello,\n\nHere is your password reset code: " + resetCode +
                "\n\nThis code will expire in 15 minutes.\n\nBest Regards,\nYourApp Team";

        EmailService.sendEmail(email, subject, body);
    }

    public boolean updatePassword(String resetCode, String newPassword) {
        String sql = "UPDATE user SET password = ? WHERE reset_code = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);  // You should hash the password here before saving
            pstmt.setString(2, resetCode);

            int updatedRows = pstmt.executeUpdate();
            return updatedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean verifyResetCode(String resetCode) {
        String sql = "SELECT reset_code, reset_code_expiry FROM user WHERE reset_code = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, resetCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedCode = rs.getString("reset_code");
                Timestamp expiry = rs.getTimestamp("reset_code_expiry");

                System.out.println("Entered Code: " + resetCode);
                System.out.println("Stored Code: " + storedCode);
                System.out.println("Expiry: " + expiry);

                if (storedCode != null && storedCode.equals(resetCode) && expiry != null && expiry.after(Timestamp.valueOf(LocalDateTime.now()))) {
                    return true; // Code valide et pas expiré
                }
            } else {
                System.out.println("No matching reset code found in the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Code invalide ou expiré
    }

    private boolean isEmailRegistered(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean requestPasswordReset(String email) {
        if (isEmailRegistered(email)) { // Check if the email exists in the database
            String resetCode = generateResetCode();
            if (storeResetCodeInDB(email, resetCode)) {
                sendResetEmail(email, resetCode);
                return true;
            }
        }
        return false;
    }
}