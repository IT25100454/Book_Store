package com.pageturner.model;

import java.time.LocalDateTime;

public class PendingRegistration {
    
    private String username;
    private String name;
    private String email;
    private String encodedPassword;
    private String address;
    private String otp;
    private LocalDateTime expiryTime;

    public PendingRegistration() {
    }

    public PendingRegistration(String username, String name, String email, String encodedPassword, String address, String otp, LocalDateTime expiryTime) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.address = address;
        this.otp = otp;
        this.expiryTime = expiryTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
}
