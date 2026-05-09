package com.springboot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
public class User {

    // ================= ID =================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= NAME =================
    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    // ================= MOBILE =================
    @NotBlank(message = "Mobile is required")
    @Column(unique = true, nullable = false)
    private String mobile;

    // ================= EMAIL =================
    @Email(message = "Invalid Email")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;

    // ================= USER PIN =================
    private String pin;

    // ================= ROLE =================
    @Column(nullable = false)
    private String role = "USER";

    // ================= ADMIN ID =================
    @Column(unique = true)
    private String adminId;

    // ================= ADMIN PASSWORD =================
    private String adminPassword;

    // ================= ACCOUNT =================
    @OneToOne(mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)

    private Account account;

    // ================= CONSTRUCTOR =================
    public User() {
    }

    // ================= GETTERS =================

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public String getPin() {
        return pin;
    }

    public String getRole() {
        return role;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public Account getAccount() {
        return account;
    }

    // ================= SETTERS =================

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    // ================= CLEAN DATA =================
    @PrePersist
    @PreUpdate
    public void cleanData() {

        if (mobile != null) {
            mobile = mobile.trim();
        }

        if (email != null) {
            email = email.trim().toLowerCase();
        }

        if (adminId != null) {
            adminId = adminId.trim();
        }
    }
}