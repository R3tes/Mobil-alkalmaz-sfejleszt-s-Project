package com.example.gadgetzone.Models;

public class User {
    private String username;
    private String email;
    private String phone;
    private String address;

    public User() {
    }

    public User(String username, String email, String phone, String address) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }
}
