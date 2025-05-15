package io.github.palexdev.materialfx.demo.utils;

import java.sql.*;

/**
 * Helper class for basket operations, created to resolve the issues with adding products to basket
 */
public class BasketHelper {
    
    /**
     * Adds a product to the basket using a simplified approach
     * 
     * @param userId The user ID
     * @param productId The product ID
     * @param quantity The quantity to add
     * @return true if successful, false otherwise
     */
    public static boolean addToBasket(int userId, int productId, int quantity) {
        Connection connection = null;
        try {
            // Get a fresh connection
            connection = DbConnection.getInstance().getCnx();
            
            if (connection == null) {
                System.err.println("Failed to get database connection");
                return false;
            }
            
            // Calculate total (get product price first)
            double price = 0;
            String priceQuery = "SELECT priceproduct FROM product WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(priceQuery)) {
                pstmt.setInt(1, productId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    price = rs.getDouble("priceproduct");
                } else {
                    System.err.println("Product not found: " + productId);
                    return false;
                }
            }
            
            double total = price * quantity;
            
            // Insert into panier table using PreparedStatement
            String sql = "INSERT INTO panier (product_id, quantity, total, status, client_id) VALUES (?, ?, ?, 'In Basket', ?)";
            
            int panierId = -1;
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, productId);
                pstmt.setInt(2, quantity);
                pstmt.setDouble(3, total);
                pstmt.setInt(4, userId);
                
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("Panier insert executed, rows affected: " + rowsAffected);
                
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            panierId = generatedKeys.getInt(1);
                            System.out.println("Generated panier ID: " + panierId);
                            
                            // Create corresponding order
                            createOrder(connection, userId, productId, quantity, panierId);
                            System.out.println("Order created successfully");
                            return true;
                        } else {
                            System.err.println("Failed to get generated panier ID");
                            return false;
                        }
                    }
                }
            }
            
            System.out.println("No rows affected by SQL, returning false");
            return false;
        } catch (SQLException e) {
            System.err.println("Error in BasketHelper.addToBasket: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static int getLastInsertId(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }
    
    private static void createOrder(Connection connection, int userId, int productId, int quantity, int panierId) throws SQLException {
        // Get the product price for total calculation
        double price = 0;
        double totalAmount = 0;
        String priceQuery = "SELECT priceproduct FROM product WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(priceQuery)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                price = rs.getDouble("priceproduct");
                totalAmount = price * quantity;
            }
        }
        
        // Current date for the order
        Timestamp currentDate = new Timestamp(System.currentTimeMillis());
        
        // More comprehensive insert that includes all needed fields
        String query = "INSERT INTO order_ (id_user, date, quantity_order, id_product, id_panier, status, total_amount, phonenum, homeaddress) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setTimestamp(2, currentDate);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, productId);
            pstmt.setInt(5, panierId);
            pstmt.setString(6, "In Basket"); // Default status for new orders
            pstmt.setDouble(7, totalAmount);
            pstmt.setInt(8, 85859855); // Default phone number
            pstmt.setString(9, "Default Address"); // Default address
            pstmt.executeUpdate();
            
            System.out.println("Created order with total amount: " + totalAmount);
        }
    }
}
