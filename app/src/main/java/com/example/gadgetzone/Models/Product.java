package com.example.gadgetzone.Models;

public class Product {

    private String _id;
    private String name;

    private int quantity;

    private String information;
    private double price;

    private int imageResource;
    private int cartedCount;


    public Product(String name, double price, String information, int quantity, int imageResource, int cartedCount) {
        this.name = name;
        this.price = price;
        this.information = information;
        this.quantity = quantity;
        this.imageResource = imageResource;
        this.cartedCount = cartedCount;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getInformation() {
        return information;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getCartedCount() {
        return cartedCount;
    }


    public String get_id() { // Changed from '_getId' to 'get_id'
        return _id;
    }

    public void set_id(String _id) { // Added setter for '_id'
        this._id = _id;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

//    public String _getId() {
//        return id;
//    }
//    public void setId(String id) {
//        this.id = id;
//    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void incrementQuantity() {
        quantity++;
    }
}
