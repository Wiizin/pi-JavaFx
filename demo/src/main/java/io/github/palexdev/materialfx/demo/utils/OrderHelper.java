package io.github.palexdev.materialfx.demo.utils;

import io.github.palexdev.materialfx.demo.model.Order;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for order operations
 */
public class OrderHelper {
    
    /**
     * Get all orders for a user with better error handling
     * 
     * @param userId The user ID
     * @return List of orders
     */
    public static List<Order> getOrdersForUser(int userId) {
        System.out.println("========================================");
        System.out.println("OrderHelper.getOrdersForUser called with userId: " + userId);
        System.out.println("========================================");
        List<Order> orders = new ArrayList<>();
        Connection connection = null;
        
        try {
            // Get a fresh connection
            connection = DbConnection.getInstance().getCnx();
            
            if (connection == null) {
                System.err.println("Failed to get database connection");
                return orders;
            }
            
            // TEMPORARY: Simplify the query to test basic connectivity
            String query = "SELECT * FROM order_ WHERE id_user = ?";
            
            System.out.println("Database connection URL: " + connection.getMetaData().getURL());
            System.out.println("Database name: " + connection.getCatalog());
            
            // Also try a query to see all tables
            try (Statement stmt = connection.createStatement()) {
                ResultSet tables = stmt.executeQuery("SHOW TABLES");
                System.out.println("Available tables in this database:");
                while (tables.next()) {
                    System.out.println(" - " + tables.getString(1));
                }
            } catch (Exception e) {
                System.out.println("Could not list tables: " + e.getMessage());
            }
            
            // Also try a direct count query to see if any orders exist
            try (Statement stmt = connection.createStatement()) {
                ResultSet countRS = stmt.executeQuery("SELECT COUNT(*) FROM order_");
                if (countRS.next()) {
                    System.out.println("Total orders in database: " + countRS.getInt(1));
                }
            } catch (Exception e) {
                System.out.println("Could not count orders: " + e.getMessage());
            }
            
            // Now try the actual query with proper join for product name and only show orders in basket
            try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT o.*, p.nameproduct FROM order_ o JOIN product p ON o.id_product = p.id WHERE o.id_user = ? AND (o.status = 'In Basket' OR o.status IS NULL)")) {
                
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                
                boolean hasResults = false;
                int resultCount = 0;
                
                while (rs.next()) {
                    hasResults = true;
                    resultCount++;
                    
                    try {
                        // Get the column values directly to handle column name differences
                        int orderId = rs.getInt("id"); // Primary key column
                        int userIdFromDb = rs.getInt("id_user");
                        int productId = rs.getInt("id_product");
                        int quantity = rs.getInt("quantity_order");
                        
                        // Get date value
                        LocalDateTime orderDate = null;
                        try {
                            Timestamp ts = rs.getTimestamp("date");
                            if (ts != null) {
                                orderDate = ts.toLocalDateTime();
                            }
                        } catch (SQLException e) {
                            orderDate = LocalDateTime.now();
                        }
                        
                        // Get remaining values with defensive coding
                        int panierId = 0;
                        try { panierId = rs.getInt("id_panier"); } catch (SQLException e) {}
                        
                        String productName = "Unknown Product";
                        try { 
                            productName = rs.getString("nameproduct"); 
                        } catch (SQLException e) {
                            System.out.println("Could not get product name from join: " + e.getMessage());
                        }
                        
                        double total = 0.0;
                        try { total = rs.getDouble("total_amount"); } catch (SQLException e) {}
                        
                        String status = "Unknown";
                        try { status = rs.getString("status"); } catch (SQLException e) {}
                        
                        Order order = new Order(
                            orderId,
                            userIdFromDb,
                            productId,
                            quantity,
                            orderDate,
                            panierId,
                            productName,
                            total,
                            status
                        );
                        
                        orders.add(order);
                    } catch (SQLException e) {
                        System.out.println("Error processing order: " + e.getMessage());
                    }
                }
                
                if (!hasResults) {
                    System.out.println("No orders found for user ID: " + userId);
                } else {
                    System.out.println("Found " + resultCount + " orders for user ID: " + userId);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error in OrderHelper.getOrdersForUser: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Total orders retrieved: " + orders.size());
        return orders;
    }
}
