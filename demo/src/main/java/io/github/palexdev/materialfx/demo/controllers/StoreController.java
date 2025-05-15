package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.demo.utils.BasketHelper;
import io.github.palexdev.materialfx.demo.utils.OrderHelper;
import io.github.palexdev.materialfx.demo.utils.ImageUtils;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.demo.model.Order;
import io.github.palexdev.materialfx.demo.model.Product;
import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.model.UserSession;
import io.github.palexdev.materialfx.demo.services.StoreService;
import io.github.palexdev.materialfx.demo.services.EmailService;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.scene.layout.Region;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StoreController implements Initializable {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private MFXComboBox<String> categoryFilter;
    
    @FXML
    private VBox sidebarContainer; // Add reference to the sidebar container

    private int userId;
    private boolean loadedInHomeView = false; // Flag to check if loaded from home page
    private Label totalAmountLabel;
    private javafx.scene.control.ScrollPane ordersScrollPane;
    private final StoreService storeService = new StoreService();
    private final EmailService emailService = new EmailService();
    private boolean isAdmin = false;

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("User ID set in StoreController: " + userId);
        
        // Check if the user is an admin to customize the store view
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole())) {
            isAdmin = true;
        }
    }
    
    /**
     * Set whether this view is being loaded inside a home page
     * When true, the sidebar will be hidden
     */
    public void setLoadedInHomeView(boolean loaded) {
        this.loadedInHomeView = loaded;
        // If the FXML has already been initialized, update the UI immediately
        if (sidebarContainer != null) {
            sidebarContainer.setVisible(!loaded);
            sidebarContainer.setManaged(!loaded);
            
            // Adjust the main content to take full width
            if (cardsContainer != null && cardsContainer.getParent() instanceof VBox) {
                VBox mainContent = (VBox) cardsContainer.getParent();
                // Reset the left anchor to 0 to take full width
                if (mainContent.getParent() instanceof javafx.scene.layout.AnchorPane) {
                    javafx.scene.layout.AnchorPane anchorPane = (javafx.scene.layout.AnchorPane) mainContent.getParent();
                    javafx.scene.layout.AnchorPane.setLeftAnchor(mainContent, 0.0);
                }
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ensure that the default image exists in all image directories
        ImageUtils.ensureDefaultImageExists();
        loadProductCardsFromDB();
        setupCategoryFilter();
        
        // If loaded from home view, hide the sidebar
        if (loadedInHomeView && sidebarContainer != null) {
            sidebarContainer.setVisible(false);
            sidebarContainer.setManaged(false);
            
            // Adjust the main content to take full width
            if (cardsContainer != null && cardsContainer.getParent() instanceof VBox) {
                VBox mainContent = (VBox) cardsContainer.getParent();
                // Reset the left anchor to 0 to take full width
                if (mainContent.getParent() instanceof javafx.scene.layout.AnchorPane) {
                    javafx.scene.layout.AnchorPane anchorPane = (javafx.scene.layout.AnchorPane) mainContent.getParent();
                    javafx.scene.layout.AnchorPane.setLeftAnchor(mainContent, 0.0);
                }
            }
        } else {
            // Apply different views/functionality based on role as before
            if (UserSession.getInstance().isLoggedIn()) {
                User currentUser = UserSession.getInstance().getCurrentUser();
                if (currentUser != null) {
                    userId = currentUser.getId();
                    
                    if ("Admin".equalsIgnoreCase(currentUser.getRole())) {
                        isAdmin = true;
                        // Show admin controls
                        createAdminControls();
                    } else {
                        // Show regular user/organizer controls
                        createSidebar();
                    }
                }
            }
        }
    }

    private void createAdminControls() {
        // TODO: Add admin-specific controls (add product button, etc.)
        MFXButton addProductButton = new MFXButton("Add New Product");
        addProductButton.getStyleClass().add("add-product-button");
        addProductButton.setOnAction(event -> showAddProductDialog());
        
        HBox adminControls = new HBox(10, addProductButton);
        adminControls.setAlignment(Pos.CENTER_RIGHT);
        
        // Add to the scene
        if (cardsContainer.getParent() instanceof VBox) {
            VBox parent = (VBox) cardsContainer.getParent();
            parent.getChildren().add(0, adminControls);
        }
    }

    private void showAddProductDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/palexdev/materialfx/demo/fxml/AddProduct.fxml"));
            Parent root = loader.load();
            
            // Get controller to pass data if needed
            //AddProductController controller = loader.getController();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Product");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh products after adding
            loadProductCardsFromDB();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Add Product dialog: " + e.getMessage());
        }
    }

    private void loadProductCardsFromDB() {
        try {
            List<Product> products = storeService.getAllProducts();
            displayProducts(products);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayProducts(List<Product> products) {
        cardsContainer.getChildren().clear();
        for (Product product : products) {
            VBox card = createProductCard(product);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createProductCard(Product product) {
        // Create main card container with styling
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("product-card");
        card.setPadding(new javafx.geometry.Insets(15));
        card.setMinWidth(200);
        card.setMaxWidth(250);
        
        // Create image container to ensure consistent sizing
        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setMinHeight(150);
        imageContainer.setMaxHeight(150);
        
        // Add product image with improved styling
        ImageView productImage = new ImageView();
        productImage.setFitHeight(130);
        productImage.setFitWidth(130);
        productImage.setPreserveRatio(true);
        productImage.setSmooth(true);
        
        // Load the product image using our utility class
        String imagePath = product.getImage();
        ImageUtils.setImageOnView(productImage, imagePath);
        
        imageContainer.getChildren().add(productImage);
        
        // Product name with improved styling
        Label nameLabel = new Label(product.getNameproduct());
        nameLabel.getStyleClass().add("header-label");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(200);

        // Price with improved styling
        Label priceLabel = new Label(String.format("$%.2f", product.getPriceproduct()));
        priceLabel.getStyleClass().add("price-label");
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        // Availability indicator (small colored circle)
        String stockStatus = product.getStock();
        boolean isInStock = stockStatus != null && !stockStatus.equalsIgnoreCase("out of stock");
        
        // Create a simple availability indicator
        Region availabilityIndicator = new Region();
        availabilityIndicator.setPrefSize(10, 10);
        availabilityIndicator.setStyle("-fx-background-radius: 5; -fx-background-color: " + 
                                      (isInStock ? "#4caf50" : "#f44336") + ";");
        
        // Create a horizontal box for price and availability
        HBox priceBox = new HBox(10);
        priceBox.setAlignment(Pos.CENTER);
        priceBox.getChildren().addAll(priceLabel, availabilityIndicator);
        
        // Quantity field - just the number input without label
        TextField quantityField = new TextField("1");
        quantityField.setPrefWidth(60);
        quantityField.setMaxWidth(60);
        quantityField.setAlignment(Pos.CENTER);
        quantityField.setDisable(!isInStock);
        quantityField.setPromptText("Qty");
        
        // Action buttons container
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        
        // Add to basket button
        MFXButton addToBasketButton = new MFXButton();
        addToBasketButton.setText("Add");
        addToBasketButton.setPrefWidth(80);
        addToBasketButton.getStyleClass().add("add-to-basket-button");
        addToBasketButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        addToBasketButton.setDisable(!isInStock);
        addToBasketButton.setOnAction(event -> {
            try {
                int quantity = Integer.parseInt(quantityField.getText());
                boolean success = BasketHelper.addToBasket(userId, product.getId(), quantity);
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product added to basket!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product to basket.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid quantity.");
            }
        });
        
        // Details button
        MFXButton detailsButton = new MFXButton();
        detailsButton.setText("Details");
        detailsButton.setPrefWidth(80);
        detailsButton.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white;");
        detailsButton.setOnAction(event -> showProductDetails(product));
        
        actionButtons.getChildren().addAll(addToBasketButton, detailsButton);
        
        // Admin controls
        HBox adminControls = new HBox(10);
        adminControls.setAlignment(Pos.CENTER);
        
        if (isAdmin) {
            MFXButton editButton = new MFXButton("Edit");
            editButton.getStyleClass().add("edit-button");
            editButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
            editButton.setOnAction(event -> {
                showAlert(Alert.AlertType.INFORMATION, "Edit Product", 
                        "Edit functionality will be implemented here.");
            });
            
            MFXButton deleteButton = new MFXButton("Delete");
            deleteButton.getStyleClass().add("delete-button");
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            deleteButton.setOnAction(event -> {
                if (showConfirmationDialog("Delete Product", 
                        "Are you sure you want to delete " + product.getNameproduct() + "?")) {
                    try {
                        if (storeService.deleteProduct(product)) {
                            loadProductCardsFromDB();
                            showAlert(Alert.AlertType.INFORMATION, "Success", 
                                    "Product deleted successfully!");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", 
                                    "Failed to delete product.");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", 
                                "Database error: " + e.getMessage());
                    }
                }
            });
            
            adminControls.getChildren().addAll(editButton, deleteButton);
        }
        
        // Add all components to the card in a cleaner layout
        card.getChildren().addAll(
            imageContainer,
            nameLabel,
            priceBox,
            quantityField,
            actionButtons
        );
        
        // Add admin controls if the user is an admin
        if (isAdmin) {
            card.getChildren().add(adminControls);
        }

        return card;
    }
    
    private void setupCategoryFilter() {
        try {
            List<String> categories = storeService.getAllCategories();
            categoryFilter.getItems().clear();
            categoryFilter.getItems().addAll(categories);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void filterProducts() {
        try {
            String searchText = searchField.getText() != null ? searchField.getText() : "";
            String category = categoryFilter.getValue();
            
            List<Product> filteredProducts = storeService.filterProducts(searchText, category, null);
            displayProducts(filteredProducts);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not filter products: " + e.getMessage());
        }
    }
    
    @FXML
    private void resetFilters() {
        searchField.clear();
        categoryFilter.clearSelection();
        loadProductCardsFromDB();
    }
    
    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("order-card");
        card.setPrefWidth(200);
        
        Label orderIdLabel = new Label("Order #" + order.getOrderId());
        orderIdLabel.getStyleClass().add("header-label");
        
        Label productLabel = new Label("Product: " + order.getProductName());
        Label quantityLabel = new Label("Quantity: " + order.getQuantity());
        Label totalLabel = new Label(String.format("Total: $%.2f", order.getTotalPrice()));
        Label statusLabel = new Label("Status: " + order.getStatus());
        
        card.getChildren().addAll(
            orderIdLabel,
            productLabel,
            quantityLabel,
            totalLabel,
            statusLabel
        );
        
        return card;
    }
    
    @FXML
    private void viewOrders() {
        try {
            Stage ordersStage = new Stage();
            ordersStage.initModality(Modality.APPLICATION_MODAL);
            ordersStage.setTitle("My Orders");
            
            VBox root = new VBox(20);
            root.setAlignment(Pos.CENTER);
            root.setStyle("-fx-padding: 20; -fx-background-color: #f0f0f0;");
            
            Label headerLabel = new Label("My Orders");
            headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            
            // Get orders from DB
            List<Order> orders = OrderHelper.getOrdersForUser(userId);
            System.out.println("Retrieved " + orders.size() + " orders for user ID: " + userId);
            
            if (orders.isEmpty()) {
                Label emptyLabel = new Label("You have no orders.");
                emptyLabel.setStyle("-fx-font-size: 16px;");
                root.getChildren().addAll(headerLabel, emptyLabel);
            } else {
                FlowPane cardsContainer = new FlowPane();
                cardsContainer.setHgap(20);
                cardsContainer.setVgap(20);
                cardsContainer.setAlignment(Pos.CENTER);
                
                ordersScrollPane = new javafx.scene.control.ScrollPane(cardsContainer);
                ordersScrollPane.setFitToWidth(true);
                ordersScrollPane.setPrefHeight(400);
                ordersScrollPane.setStyle("-fx-background-color: transparent;");
                
                VBox.setVgrow(ordersScrollPane, Priority.ALWAYS);
                
                for (Order order : orders) {
                    cardsContainer.getChildren().add(createOrderCard(order));
                }
                
                // Calculate total
                totalAmountLabel = new Label();
                updateTotalAmount(orders);
                
                // Add payment button
                MFXButton payButton = new MFXButton("Proceed to Payment");
                payButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-size: 16px;");
                payButton.setOnAction(event -> handlePayment(orders));
                
                root.getChildren().addAll(headerLabel, ordersScrollPane, totalAmountLabel, payButton);
            }
            
            Scene scene = new Scene(root, 600, 500);
            ordersStage.setScene(scene);
            ordersStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load orders: " + e.getMessage());
        }
    }
    
    private MFXButton createSidebarButton(String text, String icon) {
        MFXButton button = new MFXButton(text);
        button.setPrefWidth(150);
        button.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
        button.setAlignment(Pos.CENTER_LEFT);
        
        return button;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10;");
        
        MFXButton dashboardButton = createSidebarButton("Dashboard", "dashboard");
        dashboardButton.setOnAction(event -> handleDashboard());
        
        MFXButton productsButton = createSidebarButton("Products", "shopping-bag");
        productsButton.setOnAction(event -> handleProducts());
        
        MFXButton ordersButton = createSidebarButton("My Orders", "shopping-cart");
        ordersButton.setOnAction(event -> viewOrders());
        
        MFXButton settingsButton = createSidebarButton("Settings", "cog");
        settingsButton.setOnAction(event -> handleSettings());
        
        MFXButton logoutButton = createSidebarButton("Logout", "sign-out-alt");
        logoutButton.setOnAction(event -> handleLogout());
        
        sidebar.getChildren().addAll(
            dashboardButton,
            productsButton,
            ordersButton,
            settingsButton,
            logoutButton
        );
        
        return sidebar;
    }
    
    private void handleDashboard() {
        // Navigate to dashboard
        try {
            // Implementation depends on your application structure
            showAlert(Alert.AlertType.INFORMATION, "Navigation", "Navigate to Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleProducts() {
        // Navigate to products (current view)
        loadProductCardsFromDB();
    }
    
    private void handleSettings() {
        // Navigate to settings
        try {
            // Implementation depends on your application structure
            showAlert(Alert.AlertType.INFORMATION, "Navigation", "Navigate to Settings");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleLogout() {
        boolean confirmed = showConfirmationDialog("Confirm Logout", "Are you sure you want to logout?");
        
        if (confirmed) {
            try {
                UserSession.getInstance().logout();
                
                // Navigate back to login page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/palexdev/materialfx/demo/fxml/Login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/io/github/palexdev/materialfx/demo/css/Stylesheet.css").toExternalForm());
                
                // Get current stage and set new scene
                Stage stage = (Stage) cardsContainer.getScene().getWindow();
                stage.setScene(scene);
                
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
            }
        }
    }
    
    private boolean showConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().get().getButtonData().isDefaultButton();
    }
    
    private void updateTotalAmount(List<Order> orders) {
        double total = 0;
        for (Order order : orders) {
            if (!"Paid".equalsIgnoreCase(order.getStatus())) {
                total += order.getTotalPrice();
            }
        }
        
        totalAmountLabel.setText(String.format("Total Amount: $%.2f", total));
        totalAmountLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    }
    
    private void handlePayment(List<Order> orders) {
        // Create a simple payment form
        Stage paymentStage = new Stage();
        paymentStage.initModality(Modality.APPLICATION_MODAL);
        paymentStage.setTitle("Payment");
        
        VBox paymentLayout = new VBox(20);
        paymentLayout.setStyle("-fx-padding: 20; -fx-background-color: white;");
        paymentLayout.setAlignment(Pos.CENTER);
        
        Label headerLabel = new Label("Complete Your Payment");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Display order summary
        VBox summaryBox = new VBox(10);
        summaryBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");
        
        Label summaryLabel = new Label("Order Summary");
        summaryLabel.setStyle("-fx-font-weight: bold;");
        summaryBox.getChildren().add(summaryLabel);
        
        double totalAmount = 0;
        for (Order order : orders) {
            if (!"Paid".equalsIgnoreCase(order.getStatus())) {
                HBox orderLine = new HBox(10);
                orderLine.setAlignment(Pos.CENTER_LEFT);
                
                Label productLabel = new Label(order.getProductName() + " (x" + order.getQuantity() + ")");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label priceLabel = new Label(String.format("$%.2f", order.getTotalPrice()));
                
                orderLine.getChildren().addAll(productLabel, spacer, priceLabel);
                summaryBox.getChildren().add(orderLine);
                
                totalAmount += order.getTotalPrice();
            }
        }
        
        // Add total line
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ddd;");
        HBox totalLine = new HBox(10);
        totalLine.setAlignment(Pos.CENTER_LEFT);
        
        Label totalLabel = new Label("Total");
        totalLabel.setStyle("-fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label totalValueLabel = new Label(String.format("$%.2f", totalAmount));
        totalValueLabel.setStyle("-fx-font-weight: bold;");
        
        totalLine.getChildren().addAll(totalLabel, spacer, totalValueLabel);
        summaryBox.getChildren().addAll(separator, totalLine);
        
        // Create payment form
        VBox paymentForm = new VBox(15);
        paymentForm.setAlignment(Pos.CENTER);
        
        // Email field
        Label emailLabel = new Label("Email Address");
        TextField emailField = new TextField();
        emailField.setPromptText("your.email@example.com");
        emailField.setPrefWidth(300);
        
        // Phone number field
        Label phoneLabel = new Label("Phone Number");
        TextField phoneField = new TextField();
        phoneField.setPromptText("+123 456 7890");
        phoneField.setPrefWidth(300);
        
        // Home address field
        Label addressLabel = new Label("Delivery Address");
        TextField addressField = new TextField();
        addressField.setPromptText("123 Main St, City, Country");
        addressField.setPrefWidth(300);
        
        // Credit card field
        Label cardLabel = new Label("Credit Card Number");
        TextField cardField = new TextField();
        cardField.setPromptText("XXXX XXXX XXXX XXXX");
        cardField.setPrefWidth(300);
        
        // Expiry date and CVV
        HBox cardDetailsBox = new HBox(10);
        cardDetailsBox.setAlignment(Pos.CENTER);
        
        VBox expiryBox = new VBox(5);
        Label expiryLabel = new Label("Expiry Date");
        TextField expiryField = new TextField();
        expiryField.setPromptText("MM/YY");
        expiryField.setPrefWidth(140);
        expiryBox.getChildren().addAll(expiryLabel, expiryField);
        
        VBox cvvBox = new VBox(5);
        Label cvvLabel = new Label("CVV");
        TextField cvvField = new TextField();
        cvvField.setPromptText("XXX");
        cvvField.setPrefWidth(140);
        cvvBox.getChildren().addAll(cvvLabel, cvvField);
        
        cardDetailsBox.getChildren().addAll(expiryBox, cvvBox);
        
        // Name on card
        Label nameLabel = new Label("Name on Card");
        TextField nameField = new TextField();
        nameField.setPromptText("JOHN DOE");
        nameField.setPrefWidth(300);
        
        // Show required fields note
        Label requiredNote = new Label("* All fields are required");
        requiredNote.setStyle("-fx-font-style: italic; -fx-text-fill: #777;");
        
        // Pay button
        MFXButton payButton = new MFXButton("Complete Payment");
        payButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; " +
                            "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; " +
                            "-fx-pref-width: 300;");
        
        payButton.setOnAction(e -> {
            // Basic validation
            String email = emailField.getText();
            if (email == null || email.isEmpty() || !email.contains("@")) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid email address.");
                emailField.requestFocus();
                return;
            }
            
            // Validate other fields
            if (cardField.getText().isEmpty() || expiryField.getText().isEmpty() || 
                cvvField.getText().isEmpty() || nameField.getText().isEmpty() || 
                phoneField.getText().isEmpty() || addressField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all payment details including phone number and delivery address.");
                return;
            }
            
            try {
                // Get phone and address information
                String phoneNumber = phoneField.getText();
                String homeAddress = addressField.getText();
                
                // Update order status to Paid and add phone/address information
                for (Order order : orders) {
                    storeService.updateOrderStatusAndDeliveryInfo(order.getOrderId(), "Paid", phoneNumber, homeAddress);
                    order.setPhoneNumber(phoneNumber);
                    order.setHomeAddress(homeAddress);
                    System.out.println("Updated status for order: " + order.getOrderId() + " to Paid with delivery info");
                }
                
                // Send detailed confirmation email with order information
                emailService.sendOrderConfirmationEmail(emailField.getText(), orders, phoneNumber, homeAddress);
                
                // Clear the basket after successful payment
                storeService.clearBasket(userId);
                System.out.println("Basket cleared for user ID: " + userId);
                
                // Close payment window
                paymentStage.close();
                
                // Refresh the orders view to show updated status
                List<Order> updatedOrders = OrderHelper.getOrdersForUser(userId);
                System.out.println("Retrieved " + updatedOrders.size() + " orders for user ID: " + userId);
                
                // If there are no unpaid orders, simply close the orders view
                if (updatedOrders.isEmpty()) {
                    System.out.println("No unpaid orders remaining, closing orders view");
                    // Find the Stage of the orders view and close it
                    if (ordersScrollPane != null && ordersScrollPane.getScene() != null && 
                        ordersScrollPane.getScene().getWindow() instanceof Stage) {
                        Stage ordersStage = (Stage) ordersScrollPane.getScene().getWindow();
                        ordersStage.close();
                    }
                } else {
                    // Otherwise update the orders display
                    FlowPane cardsContainer = (FlowPane) ordersScrollPane.getContent();
                    cardsContainer.getChildren().clear();
                    for (Order order : updatedOrders) {
                        cardsContainer.getChildren().add(createOrderCard(order));
                    }
                    // Update the total amount
                    updateTotalAmount(updatedOrders);
                }
                
                // Show success message with email confirmation
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        String.format("Payment processed successfully!\n\nA confirmation email with your order details has been sent to %s\n\nPlease check your inbox.", 
                        emailField.getText()));
                
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to process payment: " + ex.getMessage());
            }
        });
        
        // Cancel Button
        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                            "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; " +
                            "-fx-pref-width: 300;");
        cancelButton.setOnAction(e -> paymentStage.close());
        
        // Update form layout with new order
        paymentForm.getChildren().addAll(
            emailLabel, emailField,
            phoneLabel, phoneField,
            addressLabel, addressField,
            cardLabel, cardField,
            cardDetailsBox,
            nameLabel, nameField,
            requiredNote,
            new Separator(),
            payButton,
            cancelButton
        );
        
        paymentLayout.getChildren().addAll(
            headerLabel,
            summaryBox,
            paymentForm
        );
        
        Scene scene = new Scene(paymentLayout, 400, 650);
        paymentStage.setScene(scene);
        paymentStage.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Helper method to load a default product image when the actual image can't be loaded
     * @param imageView The ImageView to set the default image to
     */
    private void loadDefaultImage(ImageView imageView) {
        // Use our utility class to load the default image
        Image defaultImage = ImageUtils.loadDefaultImage();
        imageView.setImage(defaultImage);
    }
    
    /**
     * Shows a dialog with detailed product information
     * @param product The product to show details for
     */
    private void showProductDetails(Product product) {
        // Create a new stage for the product details
        Stage detailsStage = new Stage();
        detailsStage.setTitle(product.getNameproduct() + " - Details");
        detailsStage.initModality(Modality.APPLICATION_MODAL);
        
        // Create the main layout
        VBox detailsLayout = new VBox(20);
        detailsLayout.setPadding(new javafx.geometry.Insets(20));
        detailsLayout.setAlignment(Pos.TOP_CENTER);
        detailsLayout.setStyle("-fx-background-color: white;");
        
        // Product image
        ImageView productImage = new ImageView();
        productImage.setFitHeight(200);
        productImage.setFitWidth(200);
        productImage.setPreserveRatio(true);
        
        // Load the product image using the same logic as in createProductCard
        try {
            String imagePath = product.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                String resourcePath = "/io/github/palexdev/materialfx/demo/images/" + imagePath;
                Image image = new Image(getClass().getResourceAsStream(resourcePath));
                
                if (image == null || image.isError() || image.getWidth() == 0) {
                    // Try file path
                    String filePath = "file:src/main/resources/io/github/palexdev/materialfx/demo/images/" + imagePath;
                    image = new Image(filePath);
                }
                
                if (image != null && !image.isError() && image.getWidth() > 0) {
                    productImage.setImage(image);
                } else {
                    loadDefaultImage(productImage);
                }
            } else {
                loadDefaultImage(productImage);
            }
        } catch (Exception e) {
            loadDefaultImage(productImage);
        }
        
        // Product name with styling
        Label nameLabel = new Label(product.getNameproduct());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Price with styling
        Label priceLabel = new Label(String.format("Price: $%.2f", product.getPriceproduct()));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        // Category
        Label categoryLabel = new Label("Category: " + product.getCategory());
        categoryLabel.setStyle("-fx-font-size: 16px;");
        
        // Stock status
        String stockStatus = product.getStock();
        Label stockLabel = new Label("Availability: " + stockStatus);
        stockLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + 
                           (stockStatus != null && !stockStatus.equalsIgnoreCase("out of stock") ? 
                           "#4caf50" : "#f44336") + ";");
        
        // Description (if available)
        Label descriptionTitle = new Label("Description:");
        descriptionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label descriptionLabel = new Label(product.getProductdescription() != null && !product.getProductdescription().isEmpty() ? 
                                          product.getProductdescription() : "No description available");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPrefWidth(400);
        
        // Add to basket section
        HBox addToBasketBox = new HBox(10);
        addToBasketBox.setAlignment(Pos.CENTER);
        
        Label quantityLabel = new Label("Quantity:");
        TextField quantityField = new TextField("1");
        quantityField.setPrefWidth(60);
        
        MFXButton addToBasketButton = new MFXButton("Add to Basket");
        addToBasketButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-padding: 10 20;");
        addToBasketButton.setDisable(stockStatus == null || stockStatus.equalsIgnoreCase("out of stock"));
        
        addToBasketButton.setOnAction(event -> {
            try {
                int quantity = Integer.parseInt(quantityField.getText());
                boolean success = BasketHelper.addToBasket(userId, product.getId(), quantity);
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product added to basket!");
                    detailsStage.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product to basket.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid quantity.");
            }
        });
        
        addToBasketBox.getChildren().addAll(quantityLabel, quantityField, addToBasketButton);
        
        // Close button
        MFXButton closeButton = new MFXButton("Close");
        closeButton.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white; -fx-padding: 10 20;");
        closeButton.setOnAction(event -> detailsStage.close());
        
        // Add all components to the layout
        detailsLayout.getChildren().addAll(
            productImage,
            nameLabel,
            priceLabel,
            categoryLabel,
            stockLabel,
            descriptionTitle,
            descriptionLabel,
            new Separator(),
            addToBasketBox,
            closeButton
        );
        
        // Set the scene and show the stage
        Scene scene = new Scene(detailsLayout, 450, 650);
        detailsStage.setScene(scene);
        detailsStage.showAndWait();
    }
}
