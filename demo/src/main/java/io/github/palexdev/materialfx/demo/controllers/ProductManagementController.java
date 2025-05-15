package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.demo.model.Product;
import io.github.palexdev.materialfx.demo.services.ProductService;
import io.github.palexdev.materialfx.demo.utils.ImageUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProductManagementController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private ComboBox<String> stockFilterComboBox;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> imageColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, String> stockColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, String> descriptionColumn;
    @FXML private TableColumn<Product, Void> actionsColumn;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> stockComboBox;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextArea descriptionField;
    @FXML private TextField imagePathField;
    @FXML private Button browseButton;

    private final ProductService productService = new ProductService();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private Product currentProduct;
    private boolean isEditMode = false;
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "uploads/products/";
    private static final String DEFAULT_IMAGE = "default_product.png";

    // Category and Stock options
    private final List<String> CATEGORIES = Arrays.asList("Clothing", "Shoes", "Equipment", "Accessories", "Nutrition");
    private final List<String> STOCK_OPTIONS = Arrays.asList("Yes", "Coming", "No");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ensure the default image exists in all image directories
        ImageUtils.ensureDefaultImageExists();

        // Initialize the table and form
        initializeTable();
        initializeForm();

        // Load products after initialization
        javafx.application.Platform.runLater(() -> {
            loadProducts();
            System.out.println("Products loaded after initialization");
        });
    }

    private void initializeTable() {
        // Set up table columns with explicit cell value factories
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        imageColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getImage()));
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNameproduct()));
        priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPriceproduct()).asObject());
        stockColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStock()));
        categoryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));
        descriptionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductdescription()));

        // Set up image column to display images
        imageColumn.setCellFactory(column -> new TableCell<Product, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);

                if (empty || imagePath == null) {
                    setGraphic(null);
                } else {
                    try {
                        // Try to load the image
                        String imageName = imagePath;
                        String resourcePath = "/io/github/palexdev/materialfx/demo/images/" + imageName;
                        Image image = new Image(getClass().getResourceAsStream(resourcePath));

                        if (image.isError()) {
                            // If image can't be loaded, use a default image
                            image = new Image(getClass().getResourceAsStream("/io/github/palexdev/materialfx/demo/images/default_product.png"));
                        }

                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        // If there's an error, don't show any image
                        setGraphic(null);
                        System.err.println("Error loading image: " + e.getMessage());
                    }
                }
            }
        });

        // Debug column setup
        System.out.println("Table columns initialized with explicit cell value factories");

        // Set default cell factories with explicit text color
        idColumn.setCellFactory(column -> new TableCell<Product, Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else {
                    setText(id.toString());
                    setStyle("-fx-text-fill: #212529;");
                }
            }
        });

        nameColumn.setCellFactory(column -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                } else {
                    setText(name);
                    setStyle("-fx-text-fill: #212529;");
                }
            }
        });

        categoryColumn.setCellFactory(column -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(category);
                    setStyle("-fx-text-fill: #212529;");
                }
            }
        });

        descriptionColumn.setCellFactory(column -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String description, boolean empty) {
                super.updateItem(description, empty);
                if (empty || description == null) {
                    setText(null);
                } else {
                    setText(description);
                    setStyle("-fx-text-fill: #212529;");
                }
            }
        });

        // Format the price column to show currency
        priceColumn.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    DecimalFormat df = new DecimalFormat("#,##0.00");
                    setText("$" + df.format(price));
                    setAlignment(Pos.CENTER_RIGHT);
                    setStyle("-fx-text-fill: #212529; -fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        // Format the stock column with colors
        stockColumn.setCellFactory(column -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(stock);
                    if ("Yes".equals(stock)) {
                        setStyle("-fx-text-fill: #28a745;"); // Green for in stock
                    } else if ("Coming".equals(stock)) {
                        setStyle("-fx-text-fill: #fd7e14;"); // Orange for coming soon
                    } else {
                        setStyle("-fx-text-fill: #dc3545;"); // Red for out of stock
                    }
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Actions column with edit and delete buttons
        actionsColumn.setCellFactory(column -> new TableCell<Product, Void>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox buttonsBox = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                buttonsBox.setAlignment(Pos.CENTER);

                // Use icons instead of text for buttons
                editButton.setStyle("-fx-background-color: #ffc107; -fx-min-width: 30px; -fx-min-height: 30px; -fx-background-radius: 15px;");
                deleteButton.setStyle("-fx-background-color: #dc3545; -fx-min-width: 30px; -fx-min-height: 30px; -fx-background-radius: 15px;");

                // Set text to 'E' and 'D' as simple icons
                editButton.setText("E");
                deleteButton.setText("D");
                editButton.setTextFill(javafx.scene.paint.Color.WHITE);
                deleteButton.setTextFill(javafx.scene.paint.Color.WHITE);

                editButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleEditProduct(product);
                });

                deleteButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDeleteProduct(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonsBox);
            }
        });
    }

    private void initializeForm() {
        // Initialize stock options
        stockComboBox.getItems().addAll(STOCK_OPTIONS);

        // Initialize category options
        categoryComboBox.getItems().addAll(CATEGORIES);

        // Initialize filter options
        categoryFilterComboBox.getItems().addAll(CATEGORIES);
        stockFilterComboBox.getItems().addAll(STOCK_OPTIONS);

        // Add numeric validation to price field
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
            }
        });
    }

    @FXML
    private void handleAddProduct() {
        isEditMode = false;
        currentProduct = new Product();
        clearForm();
    }

    private void handleEditProduct(Product product) {
        isEditMode = true;
        currentProduct = product;

        // Fill form with product data
        nameField.setText(product.getNameproduct());
        priceField.setText(String.valueOf(product.getPriceproduct()));
        stockComboBox.setValue(product.getStock());
        categoryComboBox.setValue(product.getCategory());
        descriptionField.setText(product.getProductdescription());
        imagePathField.setText(product.getImage());
        selectedImageFile = null;
    }

    @FXML
    private void handleDeleteProduct(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Product");
        alert.setContentText("Are you sure you want to delete the product: " + product.getNameproduct() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Soft delete - update the deleted flag and timestamp
                    product.setDeleted(true);
                    product.setDeleted_at(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                    boolean success = productService.update(product);
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully");
                        loadProducts();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product");
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Error deleting product: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleSaveProduct() {
        // Validate form
        if (!validateForm()) {
            return;
        }

        try {
            // Make sure we have a product object to work with
            if (currentProduct == null) {
                currentProduct = new Product();
                System.out.println("Created new Product object");
            }

            // Set product properties from form
            currentProduct.setNameproduct(nameField.getText().trim());
            currentProduct.setPriceproduct(Double.parseDouble(priceField.getText().trim()));
            currentProduct.setStock(stockComboBox.getValue());
            currentProduct.setCategory(categoryComboBox.getValue());
            currentProduct.setProductdescription(descriptionField.getText().trim());

            // Set default image if none is provided
            if (selectedImageFile != null) {
                String fileName = processImageUpload(selectedImageFile);
                currentProduct.setImage(fileName);
                System.out.println("Set image to: " + fileName);
            } else if (currentProduct.getImage() == null || currentProduct.getImage().isEmpty()) {
                currentProduct.setImage(DEFAULT_IMAGE);
                System.out.println("Set default image: " + DEFAULT_IMAGE);
            }

            // Set deleted flag to false for new products
            if (!isEditMode) {
                currentProduct.setDeleted(false);
                System.out.println("Set deleted flag to false for new product");
            }

            // Debug information
            System.out.println("\n==== SAVING PRODUCT ====");
            System.out.println("Mode: " + (isEditMode ? "UPDATE" : "INSERT"));
            System.out.println("ID: " + (currentProduct.getId() > 0 ? currentProduct.getId() : "New product"));
            System.out.println("Name: " + currentProduct.getNameproduct());
            System.out.println("Price: " + currentProduct.getPriceproduct());
            System.out.println("Category: " + currentProduct.getCategory());
            System.out.println("Stock: " + currentProduct.getStock());
            System.out.println("Image: " + currentProduct.getImage());
            System.out.println("Description: " + currentProduct.getProductdescription());
            System.out.println("========================\n");

            boolean success;
            if (isEditMode) {
                success = productService.update(currentProduct);
                System.out.println("Update result: " + success);
            } else {
                success = productService.insert(currentProduct);
                System.out.println("Insert result: " + success);
            }

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        isEditMode ? "Product updated successfully" : "Product added successfully");
                clearForm();
                loadProducts(); // Refresh the table

                // Reset current product and edit mode
                currentProduct = null;
                isEditMode = false;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        isEditMode ? "Failed to update product" : "Failed to add product");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error saving product: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Number Format Error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid price format");
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "File Error",
                    "Error processing image: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
    }

    private void clearForm() {
        // Clear form fields
        nameField.clear();
        priceField.clear();
        stockComboBox.getSelectionModel().clearSelection();
        categoryComboBox.getSelectionModel().clearSelection();
        descriptionField.clear();
        imagePathField.clear();
        selectedImageFile = null;
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png",
                        "*.jpg",
                        "*.jpeg"
                )
        );

        Stage stage = (Stage) browseButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImageFile = file;
            imagePathField.setText(file.getName());
        }
    }

    @FXML
    private void handleFilter() {
        String nameFilter = searchField.getText();
        String categoryFilterValue = categoryFilterComboBox.getValue();
        String stockFilterValue = stockFilterComboBox.getValue();

        loadProducts(nameFilter, categoryFilterValue, stockFilterValue);
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        categoryFilterComboBox.getSelectionModel().clearSelection();
        stockFilterComboBox.getSelectionModel().clearSelection();
        loadProducts();
    }

    @FXML
    private void handleRefresh() {
        loadProducts();
    }

    @FXML
    private void loadProducts() {
        loadProducts("", null, null);
    }

    private void loadProducts(String nameFilter, String categoryFilter, String stockFilter) {
        try {
            // Get products from the database
            List<Product> products = productService.showAll();
            System.out.println("Loaded " + products.size() + " products from database/mock data");

            // Debug: print out the first product if available
            if (!products.isEmpty()) {
                Product firstProduct = products.get(0);
                System.out.println("First product: ID=" + firstProduct.getId() +
                        ", Name=" + firstProduct.getNameproduct() +
                        ", Price=" + firstProduct.getPriceproduct() +
                        ", Category=" + firstProduct.getCategory());
            }

            // Filter the products manually if filters are provided
            if (nameFilter != null && !nameFilter.isEmpty() ||
                    categoryFilter != null ||
                    stockFilter != null) {

                products = products.stream().filter(product -> {
                    boolean matchesName = nameFilter == null || nameFilter.isEmpty() ||
                            product.getNameproduct().toLowerCase().contains(nameFilter.toLowerCase());
                    boolean matchesCategory = categoryFilter == null ||
                            product.getCategory().equals(categoryFilter);
                    boolean matchesStock = stockFilter == null ||
                            product.getStock().equals(stockFilter);
                    return matchesName && matchesCategory && matchesStock;
                }).collect(Collectors.toList());

                System.out.println("After filtering: " + products.size() + " products remain");
            }

            // Clear and add items to the table
            productTable.getItems().clear();
            productTable.getItems().addAll(products);
            System.out.println("Added " + productTable.getItems().size() + " items to the table");

            // Force the table to refresh
            productTable.refresh();

        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();

        // Validate name
        if (nameField.getText().isEmpty()) {
            errorMessage.append("Product name is required\n");
        }

        // Validate price
        if (priceField.getText().isEmpty()) {
            errorMessage.append("Price is required\n");
        } else {
            try {
                double price = Double.parseDouble(priceField.getText());
                if (price <= 0) {
                    errorMessage.append("Price must be greater than zero\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Price must be a valid number\n");
            }
        }

        // Validate stock status
        if (stockComboBox.getValue() == null) {
            errorMessage.append("Stock status is required\n");
        }

        // Validate category
        if (categoryComboBox.getValue() == null) {
            errorMessage.append("Category is required\n");
        }

        // Show error message if validation fails
        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errorMessage.toString());
            return false;
        }

        return true;
    }

    private String processImageUpload(File imageFile) throws IOException {
        if (imageFile == null) {
            return DEFAULT_IMAGE;
        }

        System.out.println("Processing image upload using ImageUtils: " + imageFile.getAbsolutePath());
        // Use our ImageUtils class to save the image in all available directories
        String filename = ImageUtils.saveUploadedImage(imageFile);

        // Log success
        System.out.println("Image saved successfully as: " + filename);
        return filename;
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        showAlert(alertType, title, null, content);
    }
}