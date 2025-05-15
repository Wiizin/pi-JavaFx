package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.Order;
import io.github.palexdev.materialfx.demo.model.Product;
import io.github.palexdev.materialfx.demo.utils.OrderHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreService {
    private Connection connection;
    private ProductService productService;
    
    public StoreService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sportifydb4", "root", "");
            System.out.println("StoreService: Database connection successful");
        } catch (SQLException e) {
            System.out.println("StoreService: Database connection failed: " + e.getMessage());
        }
        productService = new ProductService();
    }
    
    public List<Product> getAllProducts() throws SQLException {
        return productService.showAll();
    }
    
    public List<Product> filterProducts(String searchText, String category, String stock) throws SQLException {
        return productService.filterProducts(searchText, category, stock);
    }
    
    public List<String> getAllCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        
        if (connection == null) {
            // Return mock categories
            categories.add("Equipment");
            categories.add("Clothing");
            categories.add("Footwear");
            return categories;
        }
        
        String query = "SELECT DISTINCT category FROM product";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        }
        return categories;
    }
    
    public List<Order> getOrdersForUser(int userId) {
        return OrderHelper.getOrdersForUser(userId);
    }
    
    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        if (connection == null) {
            System.out.println("Mock update order status - Order: " + orderId + ", Status: " + status);
            return true;
        }
        
        String query = "UPDATE order_ SET status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Update order status and add delivery information
     * 
     * @param orderId The order ID to update
     * @param status The new status for the order
     * @param phoneNumber Customer's phone number
     * @param homeAddress Customer's delivery address
     * @return true if the update was successful
     * @throws SQLException if there's a database error
     */
    public boolean updateOrderStatusAndDeliveryInfo(int orderId, String status, String phoneNumber, String homeAddress) throws SQLException {
        if (connection == null) {
            System.out.println("Mock update order with delivery info - Order: " + orderId + 
                              ", Status: " + status + ", Phone: " + phoneNumber + ", Address: " + homeAddress);
            return true;
        }
        
        // First, check if phonenum and homeaddress columns exist in order_ table
        boolean columnsExist = checkIfColumnsExist("order_", "phonenum", "homeaddress");
        
        if (!columnsExist) {
            // Add the columns if they don't exist
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("ALTER TABLE order_ ADD COLUMN phonenum VARCHAR(20)");
                stmt.executeUpdate("ALTER TABLE order_ ADD COLUMN homeaddress VARCHAR(255)");
                System.out.println("Added phonenum and homeaddress columns to order_ table");
            } catch (SQLException e) {
                System.out.println("Error adding columns: " + e.getMessage());
                // Continue anyway - the update might still work if the error was just that the columns already exist
            }
        }
        
        String query = "UPDATE order_ SET status = ?, phonenum = ?, homeaddress = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setString(2, phoneNumber);
            pstmt.setString(3, homeAddress);
            pstmt.setInt(4, orderId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Check if specified columns exist in a table
     * 
     * @param tableName Name of the table to check
     * @param columnNames Names of columns to check for
     * @return true if all columns exist
     */
    private boolean checkIfColumnsExist(String tableName, String... columnNames) {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet columns = meta.getColumns(null, null, tableName, null);
            
            List<String> existingColumns = new ArrayList<>();
            while (columns.next()) {
                existingColumns.add(columns.getString("COLUMN_NAME").toLowerCase());
            }
            
            for (String columnName : columnNames) {
                if (!existingColumns.contains(columnName.toLowerCase())) {
                    return false;
                }
            }
            
            return true;
        } catch (SQLException e) {
            System.out.println("Error checking columns: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Clear the basket for a user after payment is complete
     * 
     * @param userId The user ID whose basket should be cleared
     * @return true if the operation was successful
     * @throws SQLException if there's a database error
     */
    public boolean clearBasket(int userId) throws SQLException {
        if (connection == null) {
            System.out.println("Mock clear basket for user: " + userId);
            return true;
        }
        
        System.out.println("Attempting to clear basket for user ID: " + userId);
        
        try {
            // First, update the status of all orders that are 'In Basket' to 'Paid'
            String updateOrdersQuery = "UPDATE order_ SET status = 'Paid' WHERE id_user = ? AND status = 'In Basket'";
            
            try (PreparedStatement updateStmt = connection.prepareStatement(updateOrdersQuery)) {
                updateStmt.setInt(1, userId);
                int ordersUpdated = updateStmt.executeUpdate();
                System.out.println("Updated " + ordersUpdated + " orders from 'In Basket' to 'Paid' for user " + userId);
            }
            
            // Then update the corresponding basket items to 'Paid'
            String updateBasketQuery = "UPDATE panier p " +
                                      "INNER JOIN order_ o ON p.id = o.id_panier " +
                                      "SET p.status = 'Paid' " +
                                      "WHERE o.id_user = ? AND o.status = 'Paid'";
            
            try (PreparedStatement basketStmt = connection.prepareStatement(updateBasketQuery)) {
                basketStmt.setInt(1, userId);
                int basketItemsUpdated = basketStmt.executeUpdate();
                System.out.println("Updated " + basketItemsUpdated + " basket items to 'Paid' for user " + userId);
            }
            
            // Print the current basket state for debugging
            try (Statement checkStmt = connection.createStatement();
                 ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM panier WHERE client_id = " + userId)) {
                if (rs.next()) {
                    System.out.println("Remaining items in basket after clearing: " + rs.getInt(1));
                }
            }
            
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error clearing basket: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean addProduct(Product product) throws SQLException {
        return productService.insert(product);
    }
    
    public boolean updateProduct(Product product) throws SQLException {
        return productService.update(product);
    }
    
    public boolean deleteProduct(Product product) throws SQLException {
        return productService.delete(product);
    }
    
    public String getProductNameById(int productId) throws SQLException { 
        if (connection == null) {
            // Return mock product names
            switch (productId) {
                case 1: return "Football";
                case 2: return "Basketball";
                case 3: return "Soccer Jersey";
                case 4: return "Training Shorts";
                case 5: return "Running Shoes";
                default: return "Unknown Product";
            }
        }
        
        String query = "SELECT nameproduct FROM product WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nameproduct");
            }
        }
        return "Unknown Product";
    }
    
    public double getProductPriceById(int productId) throws SQLException {
        if (connection == null) {
            // Return mock product price
            switch (productId) {
                case 1: return 29.99;
                case 2: return 34.99;
                case 3: return 59.99;
                case 4: return 24.99;
                case 5: return 99.99;
                default: return 0.0;
            }
        }
        
        String query = "SELECT priceproduct FROM product WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("priceproduct");
            }
        }
        return 0.0;
    }
}
