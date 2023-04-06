package com.stav.completenotes.db;

public class User {
    private String username;
    private String email;
    private String dob;
    private String name;
    private String phoneNumber;
    private String gender;
    private String userId;

    public User(String username, String email, String dob, String name, String phoneNumber, String gender, String userId) {
        this.username = username;
        this.email = email;
        this.dob = dob;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.userId = userId;
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

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserId() {
        return userId;
    }
}
