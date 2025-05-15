package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.demo.model.Product;
import io.github.palexdev.materialfx.demo.services.ProductService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProductController implements Initializable {
    @FXML
    private MFXTextField nameField;
    
    @FXML
    private MFXTextField priceField;
    
    @FXML
    private MFXComboBox<String> stockCombo;
    
    @FXML
    private MFXComboBox<String> categoryCombo;
    
    @FXML
    private MFXButton saveButton;
    
    @FXML
    private MFXButton cancelButton;
    
    private ProductService productService;
    private Product currentProduct;
    private boolean isEditMode;
    private Runnable onSaveCallback;
    
    public ProductController() {
        productService = new ProductService();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize stock options
        stockCombo.getItems().addAll("In Stock", "Limited Stock", "Out of Stock");
        
        // Initialize category options
        categoryCombo.getItems().addAll("Equipment", "Clothing", "Footwear", "Accessories");
        
        // Add numeric validation to price field
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
            }
        });
        
        // Configure save button action
        saveButton.setOnAction(event -> handleSave());
        
        // Configure cancel button action
        cancelButton.setOnAction(event -> {
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
        });
    }
    
    public void setProduct(Product product) {
        this.currentProduct = product;
        this.isEditMode = (product != null);
        
        if (isEditMode) {
            // Fill form with product data
            nameField.setText(product.getNameproduct());
            priceField.setText(String.valueOf(product.getPriceproduct()));
            stockCombo.setValue(product.getStock());
            categoryCombo.setValue(product.getCategory());
        } else {
            // Clear form for new product
            nameField.clear();
            priceField.clear();
            stockCombo.setValue(null);
            categoryCombo.setValue(null);
        }
    }
    
    private void handleSave() {
        try {
            // Validate form
            if (nameField.getText().isEmpty() || 
                priceField.getText().isEmpty() || 
                stockCombo.getValue() == null || 
                categoryCombo.getValue() == null) {
                showAlert("Validation Error", "Please fill in all fields", AlertType.ERROR);
                return;
            }
            
            // Create or update product
            Product product = isEditMode ? currentProduct : new Product();
            product.setNameproduct(nameField.getText());
            product.setPriceproduct(Double.parseDouble(priceField.getText()));
            product.setStock(stockCombo.getValue());
            product.setCategory(categoryCombo.getValue());
            
            boolean success;
            if (isEditMode) {
                success = productService.update(product);
            } else {
                success = productService.insert(product);
            }
            
            if (success) {
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
            } else {
                showAlert("Error", "Failed to save product", AlertType.ERROR);
            }
            
        } catch (SQLException e) {
            showAlert("Database Error", "Error saving product: " + e.getMessage(), AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Invalid price format", AlertType.ERROR);
        }
    }
    
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }
    
    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
