package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import  io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.services.UserService;
import  io.github.palexdev.materialfx.demo.utils.DbConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.IOException;

public class LoginController {
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private MFXButton signupButton;
    @FXML
    private MFXButton loginButton;
    @FXML
    private MFXPasswordField PasswordTextField;
    @FXML
    private MFXTextField UserNameTextField;


    //@FXML
//    public void initialize() {
//        // Set the options for the ComboBox
//        roleComboBox.getItems().addAll("Player", "Organizer");
//
//        // Optional: Set a default selection
//        roleComboBox.getSelectionModel().selectFirst();  // Selects "Player" by default
//
//
//
//    }
    @FXML
    public void OnLoginClicked(ActionEvent actionEvent) {
        String email = UserNameTextField.getText();
        String password = PasswordTextField.getText();

        try {
            UserService userService = new UserService();
            User user = userService.login(email, password);

            if (user != null) {
                System.out.println("Login successful! Welcome, " + user.getFirstname());

                // Redirect based on role
                String role = user.getRole();
                if ("Admin".equalsIgnoreCase(role)) {
                    navigateToAdminDashboard();
                } else if ("client".equalsIgnoreCase(role)) {
                    //navigateToClientDashboard();
                } else {
                    System.out.println("Unknown role: " + role);
                }
            } else {
                System.out.println("Invalid email or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred during login.");
        }

    }



    private void navigateToAdminDashboard() {
        try {

            Demo.loadMainScene((Stage) rootPane.getScene().getWindow());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void navigateToClientDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/client_dashboard.fxml"));
            Parent dashboardView = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(dashboardView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load FXML: " + e.getMessage());
        }
    }


    @FXML
    public void OnSignUpClicked(ActionEvent actionEvent) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(Demo.class.getResource("fxml/signup.fxml"));
            Parent signUpView = loader.load();

            Stage stage = (Stage)rootPane .getScene().getWindow();
            Scene scene = new Scene(signUpView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {

        }
    }

}