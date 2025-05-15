package io.github.palexdev.materialfx.demo.model;

public class Product {
    private int id;
    private String nameproduct;
    private double priceproduct;
    private String stock;
    private String category;
    private String image;
    private String productdescription;
    private boolean deleted;
    private String deleted_at;
    
    // For shopping cart functionality
    private int quantity;
    private double totalPrice;

    public Product() {
        // Default constructor
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNameproduct() { return nameproduct; }
    public void setNameproduct(String nameproduct) { this.nameproduct = nameproduct; }

    public double getPriceproduct() { return priceproduct; }
    public void setPriceproduct(double priceproduct) { this.priceproduct = priceproduct; }

    public String getStock() { return stock; }
    public void setStock(String stock) { this.stock = stock; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public String getProductdescription() { return productdescription; }
    public void setProductdescription(String productdescription) { this.productdescription = productdescription; }
    
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    
    public String getDeleted_at() { return deleted_at; }
    public void setDeleted_at(String deleted_at) { this.deleted_at = deleted_at; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", nameproduct='" + nameproduct + '\'' +
                ", priceproduct=" + priceproduct +
                ", stock='" + stock + '\'' +
                ", category='" + category + '\'' +
                ", image='" + image + '\'' +
                ", productdescription='" + productdescription + '\'' +
                "}";
    }
}
