package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.demo.model.OrderAdmin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.ResourceBundle;
import io.github.palexdev.materialfx.demo.utils.DbConnection;

public class OrderAdminController implements Initializable {
    @FXML private MFXTableView<OrderAdmin> ordersTable;
    @FXML private MFXTextField searchField;
    @FXML private MFXComboBox<String> statusFilter;
    @FXML private MFXButton refreshButton;

    private ObservableList<OrderAdmin> orders;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize collections
        orders = FXCollections.observableArrayList();

        // Setup the table
        setupTable();
        setupFilters();

        // Set table items
        ordersTable.setItems(orders);

        // Load initial data
        loadOrders();

        // Setup refresh button
        refreshButton.setOnAction(e -> loadOrders());

        // Set table size constraints
        ordersTable.setPrefSize(1100, 600);
        ordersTable.setMinSize(800, 400);
        ordersTable.setMaxSize(1200, 800);
    }

    private void setupTable() {
        // Clear existing columns
        ordersTable.getTableColumns().clear();

        // Setup columns with fixed widths
        MFXTableColumn<OrderAdmin> idColumn = new MFXTableColumn<>("ID", false, Comparator.comparing(OrderAdmin::getId));
        idColumn.setPrefWidth(60);

        MFXTableColumn<OrderAdmin> userColumn = new MFXTableColumn<>("User ID", false, Comparator.comparing(OrderAdmin::getUserId));
        userColumn.setPrefWidth(80);

        MFXTableColumn<OrderAdmin> dateColumn = new MFXTableColumn<>("Date", false, Comparator.comparing(OrderAdmin::getDate));
        dateColumn.setPrefWidth(150);

        MFXTableColumn<OrderAdmin> productColumn = new MFXTableColumn<>("Product", false, Comparator.comparing(OrderAdmin::getProductName));
        productColumn.setPrefWidth(150);

        MFXTableColumn<OrderAdmin> quantityColumn = new MFXTableColumn<>("Quantity", false, Comparator.comparing(OrderAdmin::getQuantityOrder));
        quantityColumn.setPrefWidth(80);

        MFXTableColumn<OrderAdmin> totalColumn = new MFXTableColumn<>("Total", false, Comparator.comparing(OrderAdmin::getTotalAmount));
        totalColumn.setPrefWidth(100);

        MFXTableColumn<OrderAdmin> statusColumn = new MFXTableColumn<>("Status", false, Comparator.comparing(OrderAdmin::getStatus));
        statusColumn.setPrefWidth(100);

        MFXTableColumn<OrderAdmin> phoneColumn = new MFXTableColumn<>("Phone", false, Comparator.comparing(OrderAdmin::getPhoneNumber));
        phoneColumn.setPrefWidth(120);

        MFXTableColumn<OrderAdmin> addressColumn = new MFXTableColumn<>("Address", false, Comparator.comparing(OrderAdmin::getHomeAddress));
        addressColumn.setPrefWidth(200);

        MFXTableColumn<OrderAdmin> actionsColumn = new MFXTableColumn<>("Actions", false);
        actionsColumn.setPrefWidth(120);

        // Setup cell factories
        idColumn.setRowCellFactory(order -> new MFXTableRowCell<>(o -> String.valueOf(o.getId())));
        userColumn.setRowCellFactory(order -> new MFXTableRowCell<>(o -> String.valueOf(o.getUserId())));
        dateColumn.setRowCellFactory(order -> new MFXTableRowCell<>(o -> o.getDate().toString()));
        productColumn.setRowCellFactory(order -> new MFXTableRowCell<>(OrderAdmin::getProductName));
        quantityColumn.setRowCellFactory(order -> new MFXTableRowCell<>(o -> String.valueOf(o.getQuantityOrder())));
        totalColumn.setRowCellFactory(order -> new MFXTableRowCell<>(o -> String.format("%.2f", o.getTotalAmount())));
        statusColumn.setRowCellFactory(order -> new MFXTableRowCell<>(OrderAdmin::getStatus));
        phoneColumn.setRowCellFactory(order -> new MFXTableRowCell<>(o -> String.valueOf(o.getPhoneNumber())));
        addressColumn.setRowCellFactory(order -> new MFXTableRowCell<>(OrderAdmin::getHomeAddress));

        // Setup actions column with edit and delete buttons
        actionsColumn.setRowCellFactory(orderItem -> {
            MFXTableRowCell<OrderAdmin, String> cell = new MFXTableRowCell<>(o -> "");
            
            // Create edit button
            MFXButton editButton = new MFXButton("Edit");
            editButton.getStyleClass().addAll("table-action-button", "edit-button");
            editButton.setOnAction(event -> showEditDialog(orderItem));
            
            // Create delete button
            MFXButton deleteButton = new MFXButton("Delete");
            deleteButton.getStyleClass().addAll("table-action-button", "delete-button");
            deleteButton.setOnAction(event -> showDeleteConfirmation(orderItem));
            
            // Create container for buttons
            HBox actions = new HBox(5, editButton, deleteButton);
            actions.setAlignment(Pos.CENTER);
            
            cell.setGraphic(actions);
            return cell;
        });

        // Add columns to table
        ordersTable.getTableColumns().addAll(
            idColumn, userColumn, dateColumn, productColumn, quantityColumn,
            totalColumn, statusColumn, phoneColumn, addressColumn, actionsColumn
        );

        // Set selection mode
        ordersTable.setFooterVisible(true);
        ordersTable.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    private void setupFilters() {
        // Setup status filter
        statusFilter.getItems().addAll("All", "In Basket", "Paid", "Pending", "Cancelled");
        statusFilter.selectFirst();
        statusFilter.setOnAction(e -> filterOrders());

        // Setup search field
        searchField.setOnKeyReleased(e -> filterOrders());
    }

    private void loadOrders() {
        orders.clear();
        try (Connection conn = DbConnection.getInstance().getCnx()) {
            String query = "SELECT o.*, p.nameproduct FROM order_ o " +
                          "LEFT JOIN product p ON o.id_product = p.id " +
                          "ORDER BY o.date DESC";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    orders.add(new OrderAdmin(
                        rs.getInt("id"),
                        rs.getInt("id_user"),
                        rs.getTimestamp("date").toLocalDateTime(),
                        rs.getInt("quantity_order"),
                        rs.getInt("id_product"),
                        rs.getInt("id_panier"),
                        rs.getString("status"),
                        rs.getDouble("total_amount"),
                        rs.getInt("phonenum"),
                        rs.getString("homeaddress"),
                        rs.getString("nameproduct")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading orders: " + e.getMessage());
        }
    }

    private void filterOrders() {
        String searchText = searchField.getText().toLowerCase();
        String statusText = statusFilter.getValue();

        ordersTable.setItems(orders.filtered(order -> {
            boolean matchesSearch = searchText.isEmpty() ||
                String.valueOf(order.getId()).contains(searchText) ||
                String.valueOf(order.getUserId()).contains(searchText) ||
                order.getProductName().toLowerCase().contains(searchText) ||
                order.getHomeAddress().toLowerCase().contains(searchText);

            boolean matchesStatus = statusText.equals("All") || order.getStatus().equals(statusText);

            return matchesSearch && matchesStatus;
        }));
    }

    private void showEditDialog(OrderAdmin order) {
        // Create a dialog to edit order status
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Edit Order Status");
        alert.setHeaderText("Order #" + order.getId());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Status combo box
        Label statusLabel = new Label("Status:");
        MFXComboBox<String> statusCombo = new MFXComboBox<>();
        statusCombo.getItems().addAll("In Basket", "Paid", "Pending", "Cancelled");
        statusCombo.setValue(order.getStatus());
        
        content.getChildren().addAll(statusLabel, statusCombo);
        alert.getDialogPane().setContent(content);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                updateOrderStatus(order.getId(), statusCombo.getValue());
                loadOrders();
            }
        });
    }

    private void showDeleteConfirmation(OrderAdmin order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Order");
        alert.setHeaderText("Delete Order #" + order.getId());
        alert.setContentText("Are you sure you want to delete this order? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteOrder(order.getId());
                loadOrders();
            }
        });
    }

    private void deleteOrder(int orderId) {
        try (Connection conn = DbConnection.getInstance().getCnx()) {
            String query = "DELETE FROM order_ WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, orderId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error deleting order: " + e.getMessage());
        }
    }

    private void updateOrderStatus(int orderId, String newStatus) {
        try (Connection conn = DbConnection.getInstance().getCnx()) {
            String query = "UPDATE order_ SET status = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, orderId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error updating order status: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
