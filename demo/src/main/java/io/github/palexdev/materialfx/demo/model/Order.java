package io.github.palexdev.materialfx.demo.model;

import java.time.LocalDateTime;

public class Order {
    private int orderId;
    private int userId;
    private int productId;
    private int quantity;
    private LocalDateTime date;
    private int panierId;
    private String productName;
    private double totalPrice;
    private String status;
    private String phoneNumber;
    private String homeAddress;

    // Default constructor
    public Order() {
    }

    // Constructor with all fields
    public Order(int orderId, int userId, int productId, int quantity, LocalDateTime date, 
                int panierId, String productName, double totalPrice, String status, 
                String phoneNumber, String homeAddress) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.date = date;
        this.panierId = panierId;
        this.productName = productName;
        this.totalPrice = totalPrice;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.homeAddress = homeAddress;
    }
    
    // Constructor with all fields except phone and address (for backward compatibility)
    public Order(int orderId, int userId, int productId, int quantity, LocalDateTime date, 
                int panierId, String productName, double totalPrice, String status) {
        this(orderId, userId, productId, quantity, date, panierId, productName, totalPrice, status, "", "");
    }

    // Constructor for table view (used in viewOrders)
    public Order(int orderId, int productId, int quantity, double totalPrice, String status) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // Getters
    public int getOrderId() { return orderId; }
    public int getUserId() { return userId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getDate() { return date; }
    public int getPanierId() { return panierId; }
    public String getProductName() { return productName; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getHomeAddress() { return homeAddress; }

    // Setters
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public void setPanierId(int panierId) { this.panierId = panierId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setStatus(String status) { this.status = status; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }
}
