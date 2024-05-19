package com.example.gadgetzone.Models;

public class Product {
    private String id;
    private String name;
    private int quantity;
    private String information;
    private double price;
    private String productImage;

    // No-argument constructor
    public Product() {}

    public Product(String id, String name, int quantity, String information, double price, String productImage) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.information = information;
        this.price = price;
        this.productImage = productImage;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }
}