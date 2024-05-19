package com.example.gadgetzone.Models;

import androidx.annotation.NonNull;

public class User {
    private String username;
    private String email;
    private String phone;
    private String address;

    private String profilePicture;

    public User(String username, String email, String phone, String address) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.profilePicture = "default_user_icon";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                '}';
    }
}