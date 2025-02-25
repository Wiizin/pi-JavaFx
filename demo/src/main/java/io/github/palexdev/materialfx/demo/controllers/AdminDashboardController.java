package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.Organizer;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AdminDashboardController implements Initializable {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label organizersLabel;
    @FXML private Label totalUsersChangeLabel;
    @FXML private Label activeUsersChangeLabel;
    @FXML private Label organizersChangeLabel;
    @FXML private MFXProgressBar totalUsersProgress;
    @FXML private MFXProgressBar activeUsersProgress;
    @FXML private MFXProgressBar organizersProgress;
    @FXML private MFXProgressBar playersBar;
    @FXML private MFXProgressBar organizersBar;
    @FXML private MFXProgressBar activeBar;
    @FXML private MFXProgressBar inactiveBar;
    @FXML private Label playersCountLabel;
    @FXML private Label organizersCountLabel;
    @FXML private Label activeCountLabel;
    @FXML private Label inactiveCountLabel;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDashboardStatistics();
    }

    private void loadDashboardStatistics() {
        List<User> users = userService.getAll();
        int totalUsers = users.size();
        
        // Count active users and organizers
        long activeUsers = users.stream()
                .filter(User::isActive)
                .count();
        
        long totalOrganizers = users.stream()
                .filter(user -> user instanceof Organizer)
                .count();

        long totalPlayers = totalUsers - totalOrganizers;
        long inactiveUsers = totalUsers - activeUsers;

        // Calculate month-over-month changes (example calculation)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minus(1, ChronoUnit.MONTHS);
        
        double monthlyGrowth = calculateMonthlyGrowth(users, now, oneMonthAgo);
        double activeGrowth = calculateActiveGrowth(users, now, oneMonthAgo);
        double organizerGrowth = calculateOrganizerGrowth(users, now, oneMonthAgo);

        // Update statistics labels with growth indicators
        updateStatisticsLabels(totalUsers, activeUsers, totalOrganizers, 
                             monthlyGrowth, activeGrowth, organizerGrowth);

        // Update progress bars for cards
        updateProgressBars(totalUsers, activeUsers, totalOrganizers, 
                         totalPlayers, inactiveUsers);

        // Update count labels
        updateCountLabels(totalPlayers, totalOrganizers, activeUsers, inactiveUsers);
    }

    private double calculateMonthlyGrowth(List<User> users, LocalDateTime now, LocalDateTime oneMonthAgo) {
        long currentUsers = users.size();
        long previousUsers = users.stream()
                .filter(user -> user.getCreatedAt().isBefore(oneMonthAgo))
                .count();
        return previousUsers > 0 ? ((double)(currentUsers - previousUsers) / previousUsers) * 100 : 0;
    }

    private double calculateActiveGrowth(List<User> users, LocalDateTime now, LocalDateTime oneMonthAgo) {
        long currentActive = users.stream()
                .filter(User::isActive)
                .count();
        long previousActive = users.stream()
                .filter(user -> user.isActive() && user.getCreatedAt().isBefore(oneMonthAgo))
                .count();
        return previousActive > 0 ? ((double)(currentActive - previousActive) / previousActive) * 100 : 0;
    }

    private double calculateOrganizerGrowth(List<User> users, LocalDateTime now, LocalDateTime oneMonthAgo) {
        long currentOrganizers = users.stream()
                .filter(user -> user instanceof Organizer)
                .count();
        long previousOrganizers = users.stream()
                .filter(user -> user instanceof Organizer && user.getCreatedAt().isBefore(oneMonthAgo))
                .count();
        return previousOrganizers > 0 ? ((double)(currentOrganizers - previousOrganizers) / previousOrganizers) * 100 : 0;
    }

    private void updateStatisticsLabels(int totalUsers, long activeUsers, long totalOrganizers,
                                      double monthlyGrowth, double activeGrowth, double organizerGrowth) {
        // Update main statistics
        totalUsersLabel.setText(String.valueOf(totalUsers));
        activeUsersLabel.setText(String.valueOf(activeUsers));
        organizersLabel.setText(String.valueOf(totalOrganizers));

        // Update growth labels with appropriate colors
        String growthFormat = "%+.1f%% this month";
        totalUsersChangeLabel.setText(String.format(growthFormat, monthlyGrowth));
        totalUsersChangeLabel.setStyle("-fx-text-fill: " + (monthlyGrowth >= 0 ? "#4CAF50" : "#F44336"));

        activeUsersChangeLabel.setText(String.format(growthFormat, activeGrowth));
        activeUsersChangeLabel.setStyle("-fx-text-fill: " + (activeGrowth >= 0 ? "#4CAF50" : "#F44336"));

        organizersChangeLabel.setText(String.format(growthFormat, organizerGrowth));
        organizersChangeLabel.setStyle("-fx-text-fill: " + (organizerGrowth >= 0 ? "#4CAF50" : "#F44336"));
    }

    private void updateProgressBars(int totalUsers, long activeUsers, long totalOrganizers,
                                  long totalPlayers, long inactiveUsers) {
        // Card progress bars
        totalUsersProgress.setProgress(1.0); // Always full as it represents total
        activeUsersProgress.setProgress((double) activeUsers / totalUsers);
        organizersProgress.setProgress((double) totalOrganizers / totalUsers);

        // Distribution progress bars
        playersBar.setProgress((double) totalPlayers / totalUsers);
        organizersBar.setProgress((double) totalOrganizers / totalUsers);

        // Activity status bars
        activeBar.setProgress((double) activeUsers / totalUsers);
        inactiveBar.setProgress((double) inactiveUsers / totalUsers);
    }

    private void updateCountLabels(long totalPlayers, long totalOrganizers,
                                 long activeUsers, long inactiveUsers) {
        playersCountLabel.setText(totalPlayers + " Players");
        organizersCountLabel.setText(totalOrganizers + " Organizers");
        activeCountLabel.setText(activeUsers + " Active");
        inactiveCountLabel.setText(inactiveUsers + " Inactive");
    }

    @FXML
    private void handleRefresh() {
        loadDashboardStatistics();
    }

    @FXML
    private void handleBack() {
        // Add logic to navigate back to the previous page
    }
}