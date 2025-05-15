package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.demo.Demo;
import io.github.palexdev.materialfx.demo.services.UserService;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class ResetPasswordCodeController {

    @FXML
    private MFXTextField codeField1, codeField2, codeField3, codeField4, codeField5, codeField6;

    @FXML
    private Label errorLabel;

    @FXML
    private Pane pane;

    private final UserService userService = new UserService();
    private String userEmail;

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    @FXML
    public void initialize() {
        // Add paste event handlers to all fields
        setupPasteHandler(codeField1);
        setupPasteHandler(codeField2);
        setupPasteHandler(codeField3);
        setupPasteHandler(codeField4);
        setupPasteHandler(codeField5);
        setupPasteHandler(codeField6);
    }

    private void setupPasteHandler(MFXTextField field) {
        field.setOnKeyReleased(event -> {
            if (event.isShortcutDown() && event.getCode().toString().equals("V")) {
                handlePaste(field);
            }
        });
    }

    private void handlePaste(MFXTextField field) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            String pastedText = clipboard.getString().trim();
            distributePastedText(pastedText);
        }
    }

    private void distributePastedText(String pastedText) {
        // Clear all fields before pasting
        codeField1.clear();
        codeField2.clear();
        codeField3.clear();
        codeField4.clear();
        codeField5.clear();
        codeField6.clear();

        // Distribute the pasted text across the fields
        char[] chars = pastedText.toCharArray();
        if (chars.length > 0) codeField1.setText(String.valueOf(chars[0]));
        if (chars.length > 1) codeField2.setText(String.valueOf(chars[1]));
        if (chars.length > 2) codeField3.setText(String.valueOf(chars[2]));
        if (chars.length > 3) codeField4.setText(String.valueOf(chars[3]));
        if (chars.length > 4) codeField5.setText(String.valueOf(chars[4]));
        if (chars.length > 5) codeField6.setText(String.valueOf(chars[5]));

        // Move focus to the last field with content
        if (chars.length > 5) {
            codeField6.requestFocus();
        } else if (chars.length > 4) {
            codeField5.requestFocus();
        } else if (chars.length > 3) {
            codeField4.requestFocus();
        } else if (chars.length > 2) {
            codeField3.requestFocus();
        } else if (chars.length > 1) {
            codeField2.requestFocus();
        } else if (chars.length > 0) {
            codeField1.requestFocus();
        }
    }

    @FXML
    private void handleCodeInput(KeyEvent event) {
        // Auto-focus logic
        if (codeField1.getText().length() == 1) codeField2.requestFocus();
        if (codeField2.getText().length() == 1) codeField3.requestFocus();
        if (codeField3.getText().length() == 1) codeField4.requestFocus();
        if (codeField4.getText().length() == 1) codeField5.requestFocus();
        if (codeField5.getText().length() == 1) codeField6.requestFocus();
    }

    @FXML
    private void onVerifyCode() throws IOException {
        // Combine the code from all fields
        String code = codeField1.getText() + codeField2.getText() + codeField3.getText() +
                codeField4.getText() + codeField5.getText() + codeField6.getText();

        // Validate the code
        if (code.length() != 6) {
            errorLabel.setText("Invalid code. Please enter a 6-character code.");
            errorLabel.setVisible(true);
            return;
        }

        // Verify the code with the service
        if (userService.verifyResetCode(code)) {
            showAlert("Success", "Code verified successfully. You can now reset your password.");

            // Pass the resetCode to the next page's controller
            FXMLLoader loader = new FXMLLoader(Demo.class.getResource("fxml/reset_password_new.fxml"));
            Parent resetPasswordView = loader.load();

            ResetPasswordNewController controller = loader.getController();
            controller.setResetCode(code);  // Pass the resetCode to the next page's controller

            Stage stage = (Stage) pane.getScene().getWindow();
            Scene scene = new Scene(resetPasswordView);
            stage.setScene(scene);
            stage.show();
        } else {
            errorLabel.setText("Invalid or expired verification code.");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void onResendCodeClicked() {
        if (userEmail == null || userEmail.isEmpty()) {
            showAlert("Error", "Email information missing. Please go back to the previous screen.");
            return;
        }

        if (userService.requestPasswordReset(userEmail)) {
            showAlert("Success", "A new verification code has been sent to your email.");
        } else {
            showAlert("Error", "Failed to resend verification code. Please try again.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}