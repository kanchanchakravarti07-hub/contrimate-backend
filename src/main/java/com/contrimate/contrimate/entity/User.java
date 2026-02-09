package com.contrimate.contrimate.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String password; 
    private String upiId;

    // 🔥 Ye field database mein save nahi hoga, sirf verification ke liye hai
    @Transient 
    private String otp;

    // --- MANUAL GETTERS & SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    // 👇 OTP ke Getters & Setters zaroori hain
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}