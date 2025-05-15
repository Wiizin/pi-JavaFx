package io.github.palexdev.materialfx.demo.model;

import java.time.LocalDateTime;

public class OrderAdmin {
    private int id;
    private int userId;
    private LocalDateTime date;
    private int quantityOrder;
    private int productId;
    private int panierId;
    private String status;
    private double totalAmount;
    private int phoneNumber;
    private String homeAddress;
    private String productName; // Added to show product name in the table

    public OrderAdmin(int id, int userId, LocalDateTime date, int quantityOrder, int productId, int panierId, 
                     String status, double totalAmount, int phoneNumber, String homeAddress, String productName) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.quantityOrder = quantityOrder;
        this.productId = productId;
        this.panierId = panierId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.phoneNumber = phoneNumber;
        this.homeAddress = homeAddress;
        this.productName = productName;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public int getQuantityOrder() { return quantityOrder; }
    public void setQuantityOrder(int quantityOrder) { this.quantityOrder = quantityOrder; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getPanierId() { return panierId; }
    public void setPanierId(int panierId) { this.panierId = panierId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public int getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(int phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}
