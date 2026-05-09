package com.springboot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "feedback")
public class Feedback {

    // ================= PRIMARY KEY =================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= FIELDS =================
    private String name;
    private String email;
    private String rating;
    private String feature;

    @Column(length = 1000)
    private String message;

    // ================= CONSTRUCTORS =================
    public Feedback() {
    }

    public Feedback(String name, String email, String rating, String feature, String message) {
        this.name = name;
        this.email = email;
        this.rating = rating;
        this.feature = feature;
        this.message = message;
    }

    // ================= GETTERS =================
    public Long getId() {
        return id;
    }

    public String getName() {
        return name != null ? name.trim() : null;
    }

    public String getEmail() {
        return email != null ? email.trim().toLowerCase() : null;
    }

    public String getRating() {
        return rating;
    }

    public String getFeature() {
        return feature;
    }

    public String getMessage() {
        return message;
    }

    // ================= SETTERS =================
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // ================= CLEAN DATA =================
    @PrePersist
    @PreUpdate
    public void cleanData() {
        if (name != null) name = name.trim();
        if (email != null) email = email.trim().toLowerCase();
    }

    // ================= TO STRING =================
    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", rating='" + rating + '\'' +
                ", feature='" + feature + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}