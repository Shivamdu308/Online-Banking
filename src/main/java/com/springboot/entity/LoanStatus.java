package com.springboot.entity;

public enum LoanStatus {

    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    COMPLETED("Completed"); // 🔥 NEW (EMI CLOSE)

    private final String displayName;

    // Constructor
    LoanStatus(String displayName) {
        this.displayName = displayName;
    }

    // ================= GETTER =================
    public String getDisplayName() {
        return displayName;
    }

    // ================= SAFE CONVERT =================
    public static LoanStatus fromString(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }

        for (LoanStatus status : LoanStatus.values()) {
            if (status.name().equalsIgnoreCase(value)
                    || status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid Loan Status: " + value);
    }
}