package io.github.palexdev.materialfx.demo.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MatchPageController {
    @FXML
    private Label titleLabel;

    public void initialize() {
        // For testing purposes, update the label text and print a message.
        titleLabel.setText("Match Preview - Initialized");
        System.out.println("MatchPreviewController initialized.");
    }
}
