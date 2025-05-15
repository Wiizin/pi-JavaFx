package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.Product;
import io.github.palexdev.materialfx.demo.model.Order;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private Connection connection;
    private boolean useMockData = false;
    
    /**
     * Check if the service is using mock data
     * @return true if using mock data, false otherwise
     */
    public boolean useMockData() {
        return useMockData;
    }

    public ProductService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sportifydb3", "root", "");
            System.out.println("Database connection successful");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage() + ". Using mock data instead.");
            useMockData = true;
        }
    }

    public List<Product> filterProducts(String searchText, String category, String stock) throws SQLException {
        if (useMockData || connection == null) {
            // Filter the mock products based on the provided criteria
            List<Product> mockProducts = getMockProducts();
            List<Product> filteredProducts = new ArrayList<>();
            
            for (Product product : mockProducts) {
                boolean matchesSearch = product.getNameproduct().toLowerCase().contains(searchText.toLowerCase());
                boolean matchesCategory = category == null || category.isEmpty() || product.getCategory().equals(category);
                boolean matchesStock = stock == null || stock.isEmpty() || product.getStock().equals(stock);
                
                if (matchesSearch && matchesCategory && matchesStock) {
                    filteredProducts.add(product);
                }
            }
            
            return filteredProducts;
        }
        
        // Use database if connection is available
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM product WHERE nameproduct LIKE ? AND (category = ? OR ? IS NULL) AND (stock = ? OR ? IS NULL) AND deleted = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + searchText + "%");
            pstmt.setString(2, category);
            pstmt.setString(3, category);
            pstmt.setString(4, stock);
            pstmt.setString(5, stock);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setNameproduct(rs.getString("nameproduct"));
                product.setPriceproduct(rs.getDouble("priceproduct"));
                product.setStock(rs.getString("stock"));
                product.setCategory(rs.getString("category"));
                product.setImage(rs.getString("image"));
                product.setProductdescription(rs.getString("productdescription"));
                product.setDeleted(rs.getBoolean("deleted"));
                product.setDeleted_at(rs.getString("deleted_at"));
                products.add(product);
            }
        }
        return products;
    }


    // Fetch all products from the database or return mock data if database connection failed
    public List<Product> showAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        
        try {
            // Check if we should use mock data
            if (useMockData || connection == null) {
                System.out.println("Using mock product data");
                List<Product> mockProducts = getMockProducts();
                System.out.println("Generated " + mockProducts.size() + " mock products");
                return mockProducts;
            }
            
            // Check if connection is valid
            if (connection.isClosed()) {
                System.out.println("Database connection is closed. Reconnecting...");
                try {
                    connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sportifydb4", "root", "");
                } catch (SQLException e) {
                    System.out.println("Failed to reconnect to database: " + e.getMessage());
                    return getMockProducts();
                }
            }
            
            // Query the database
            String query = "SELECT * FROM product WHERE deleted = 0";
            System.out.println("Executing query: " + query);
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                int count = 0;
                while (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setNameproduct(rs.getString("nameproduct"));
                    product.setPriceproduct(rs.getDouble("priceproduct"));
                    product.setStock(rs.getString("stock"));
                    product.setCategory(rs.getString("category"));
                    product.setImage(rs.getString("image"));
                    product.setProductdescription(rs.getString("productdescription"));
                    product.setDeleted(rs.getBoolean("deleted"));
                    product.setDeleted_at(rs.getString("deleted_at"));
                    products.add(product);
                    count++;
                }
                
                System.out.println("Retrieved " + count + " products from database");
                
                // If no products found in database, return mock data
                if (products.isEmpty()) {
                    System.out.println("No products found in database, using mock data");
                    return getMockProducts();
                }
            }
            return products;
            
        } catch (SQLException e) {
            System.err.println("Error in showAll method: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Returning mock data due to database error");
            return getMockProducts();
        }
    }
    
    // Create mock product data when database is not available
    public List<Product> getMockProducts() {
        List<Product> mockProducts = new ArrayList<>();
        
        // Sports Equipment
        Product football = new Product();
        football.setId(1);
        football.setNameproduct("Football");
        football.setPriceproduct(29.99);
        football.setStock("Yes");
        football.setCategory("sport tools");
        football.setImage("football.jpg");
        football.setProductdescription("Professional football for matches and training");
        football.setDeleted(false);
        mockProducts.add(football);
        
        Product basketball = new Product();
        basketball.setId(2);
        basketball.setNameproduct("Basketball");
        basketball.setPriceproduct(34.99);
        basketball.setStock("Yes");
        basketball.setCategory("sport tools");
        basketball.setImage("basketball.jpg");
        basketball.setProductdescription("High-quality basketball with excellent grip");
        basketball.setDeleted(false);
        mockProducts.add(basketball);
        
        // Sports Clothing
        Product jersey = new Product();
        jersey.setId(3);
        jersey.setNameproduct("Soccer Jersey");
        jersey.setPriceproduct(59.99);
        jersey.setStock("Coming");
        jersey.setCategory("clothes");
        jersey.setImage("jersey.jpg");
        jersey.setProductdescription("Official team jersey with breathable fabric");
        jersey.setDeleted(false);
        mockProducts.add(jersey);
        
        Product shorts = new Product();
        shorts.setId(4);
        shorts.setNameproduct("Training Shorts");
        shorts.setPriceproduct(24.99);
        shorts.setStock("Yes");
        shorts.setCategory("clothes");
        shorts.setImage("shorts.jpg");
        shorts.setProductdescription("Comfortable training shorts for all sports");
        shorts.setDeleted(false);
        mockProducts.add(shorts);
        
        // Sports Trophies
        Product trophy = new Product();
        trophy.setId(5);
        trophy.setNameproduct("Championship Trophy");
        trophy.setPriceproduct(99.99);
        trophy.setStock("No");
        trophy.setCategory("trophies");
        trophy.setImage("trophy.jpg");
        trophy.setProductdescription("Elegant trophy for tournament champions");
        trophy.setDeleted(false);
        mockProducts.add(trophy);
        
        return mockProducts;
    }

    // Insert a new product into the database
    public boolean insert(Product product) throws SQLException {
        if (useMockData || connection == null) {
            System.out.println("Mock insert product: " + product.getNameproduct());
            // Generate a mock ID for the product
            product.setId(getMockProducts().size() + 1);
            return true;
        }
        
        try {
            // Check if connection is valid
            if (connection.isClosed()) {
                System.out.println("Database connection is closed. Reconnecting...");
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sportifydb4", "root", "");
            }
            
            // Debug information
            System.out.println("Inserting product into database: " + product.getNameproduct());
            
            String query = "INSERT INTO product (nameproduct, priceproduct, stock, category, image, productdescription, deleted) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, product.getNameproduct());
                pstmt.setDouble(2, product.getPriceproduct());
                pstmt.setString(3, product.getStock());
                pstmt.setString(4, product.getCategory());
                pstmt.setString(5, product.getImage() != null ? product.getImage() : "default_product.png");
                pstmt.setString(6, product.getProductdescription());
                pstmt.setBoolean(7, false); // New products are not deleted by default
                
                System.out.println("Executing SQL: " + query);
                System.out.println("Parameters: " + product.getNameproduct() + ", " + 
                                 product.getPriceproduct() + ", " + 
                                 product.getStock() + ", " + 
                                 product.getCategory() + ", " + 
                                 product.getImage() + ", " + 
                                 product.getProductdescription());
                
                int affectedRows = pstmt.executeUpdate();
                System.out.println("Affected rows: " + affectedRows);
                
                if (affectedRows > 0) {
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        product.setId(newId);
                        System.out.println("Generated ID: " + newId);
                    }
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("SQL Error in insert method: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Update an existing product in the database
    public boolean update(Product product) throws SQLException {
        if (useMockData || connection == null) {
            System.out.println("Mock update product: " + product.getNameproduct());
            return true;
        }
        
        String query = "UPDATE product SET nameproduct = ?, priceproduct = ?, stock = ?, category = ?, image = ?, productdescription = ?, deleted = ?, deleted_at = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, product.getNameproduct());
            pstmt.setDouble(2, product.getPriceproduct());
            pstmt.setString(3, product.getStock());
            pstmt.setString(4, product.getCategory());
            pstmt.setString(5, product.getImage());
            pstmt.setString(6, product.getProductdescription());
            pstmt.setBoolean(7, product.isDeleted());
            pstmt.setString(8, product.getDeleted_at());
            pstmt.setInt(9, product.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    // Soft delete a product (set deleted flag to true)
    public boolean delete(Product product) throws SQLException {
        if (useMockData || connection == null) {
            System.out.println("Mock delete product: " + product.getNameproduct());
            return true;
        }
        
        // Use soft delete instead of hard delete
        String query = "UPDATE product SET deleted = 1, deleted_at = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setInt(2, product.getId());
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // Get product by ID
    public Product getById(int id) throws SQLException {
        if (useMockData || connection == null) {
            // Try to find the product in mock data
            for (Product product : getMockProducts()) {
                if (product.getId() == id) {
                    return product;
                }
            }
            return null;
        }
        
        String query = "SELECT * FROM product WHERE id = ? AND deleted = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setNameproduct(rs.getString("nameproduct"));
                product.setPriceproduct(rs.getDouble("priceproduct"));
                product.setStock(rs.getString("stock"));
                product.setCategory(rs.getString("category"));
                product.setImage(rs.getString("image"));
                product.setProductdescription(rs.getString("productdescription"));
                product.setDeleted(rs.getBoolean("deleted"));
                product.setDeleted_at(rs.getString("deleted_at"));
                return product;
            }
        }
        return null;
    }

    public boolean addToBasket(int userId, int productId, int quantity) throws SQLException {
        if (useMockData || connection == null) {
            System.out.println("Mock add to basket - User: " + userId + ", Product: " + productId + ", Quantity: " + quantity);
            // In mock mode, simulate success and create a mock order
            createOrder(userId, productId, quantity, 9999); // Dummy panier ID
            return true;
        }

        // Generate a new ID manually to work around potential auto-increment issues
        int newId = 1;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM panier")) {
            if (rs.next() && rs.getObject(1) != null) {
                newId = rs.getInt(1) + 1;
            }
        }
        
        System.out.println("Will use ID: " + newId + " for new basket item");
        
        // Include the ID field explicitly in the INSERT query
        String query = "INSERT INTO panier (id, client_id, product_id, quantity, total, status) VALUES (?, ?, ?, ?, (SELECT priceproduct FROM product WHERE id = ?) * ?, 'Pending')";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, newId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, productId);
            pstmt.setInt(4, quantity);
            pstmt.setInt(5, productId);
            pstmt.setInt(6, quantity);
            pstmt.executeUpdate();

            // Get the generated panier ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int panierId = rs.getInt(1);
                createOrder(userId, productId, quantity, panierId);
                return true;
            }
        }
        return false;
    }
    
    // Create an order
    private void createOrder(int userId, int productId, int quantity, int panierId) throws SQLException {
        if (useMockData || connection == null) {
            System.out.println("Mock create order - User: " + userId + ", Product: " + productId + ", Quantity: " + quantity + ", Panier: " + panierId);
            return;
        }
        
        String query = "INSERT INTO order_ (id_user, id_product, quantity_order, id_panier) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, panierId);
            pstmt.executeUpdate();
        }
    }
    
    // Fetch all orders for a user
    public List<Order> getOrdersByUserId(int userId) throws SQLException {
        if (useMockData || connection == null) {
            // Return mock orders in demo mode
            List<Order> mockOrders = new ArrayList<>();
            
            // Add a sample order
            Order order = new Order();
            order.setOrderId(12345);
            order.setUserId(userId);
            order.setProductId(1); // First mock product
            order.setQuantity(2);
            order.setPanierId(9999);
            mockOrders.add(order);
            
            // Add another sample order
            Order order2 = new Order();
            order2.setOrderId(12346);
            order2.setUserId(userId);
            order2.setProductId(3); // Third mock product
            order2.setQuantity(1);
            order2.setPanierId(9998);
            mockOrders.add(order2);
            
            System.out.println("Returning " + mockOrders.size() + " mock orders for user " + userId);
            return mockOrders;
        }
        
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM order_ WHERE id_user = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("id"));
                order.setUserId(rs.getInt("id_user"));
                order.setProductId(rs.getInt("id_product"));
                order.setQuantity(rs.getInt("quantity_order"));
                order.setPanierId(rs.getInt("id_panier"));
                orders.add(order);
            }
        }
        return orders;
    }
}
