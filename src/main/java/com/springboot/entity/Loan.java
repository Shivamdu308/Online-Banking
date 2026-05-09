package com.springboot.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USER =================
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ================= BASIC =================
    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String type;

    // ================= DETAILS =================
    private Double income;
    private String employment;
    private Integer tenure;
    private Double interestRate;

    // ================= EMI SYSTEM =================
    private Double emiAmount;
    private Double remainingAmount;
    private LocalDate nextDueDate;

    // 🔥 NEW
    private Integer totalPaidEmi = 0;
    private Integer totalEmiCount;

    // ================= STATUS =================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;

    // 🔥 NEW (ADMIN NOTES)
    private String remarks;

    // ================= CONSTRUCTOR =================
    public Loan() {
        this.appliedAt = LocalDateTime.now();
        this.status = LoanStatus.PENDING;
    }

    // ================= GETTERS =================
    public Long getId() { return id; }
    public User getUser() { return user; }
    public Double getAmount() { return amount; }
    public String getType() { return type; }
    public Double getIncome() { return income; }
    public String getEmployment() { return employment; }
    public Integer getTenure() { return tenure; }
    public Double getInterestRate() { return interestRate; }
    public LoanStatus getStatus() { return status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }

    public Double getEmiAmount() { return emiAmount; }
    public Double getRemainingAmount() { return remainingAmount; }
    public LocalDate getNextDueDate() { return nextDueDate; }

    public Integer getTotalPaidEmi() { return totalPaidEmi; }
    public Integer getTotalEmiCount() { return totalEmiCount; }

    public String getRemarks() { return remarks; }

    // ================= SETTERS =================
    public void setUser(User user) { this.user = user; }

    public void setAmount(Double amount) {
        if (amount == null || amount <= 0)
            throw new RuntimeException("Invalid loan amount");
        this.amount = amount;
    }

    public void setType(String type) { this.type = type; }
    public void setIncome(Double income) { this.income = income; }
    public void setEmployment(String employment) { this.employment = employment; }

    public void setTenure(Integer tenure) {
        if (tenure == null || tenure <= 0)
            throw new RuntimeException("Invalid tenure");
        this.tenure = tenure;
        this.totalEmiCount = tenure;
    }

    public void setInterestRate(Double interestRate) {
        if (interestRate == null || interestRate <= 0)
            throw new RuntimeException("Invalid interest rate");
        this.interestRate = interestRate;
    }

    public void setEmiAmount(Double emiAmount) { this.emiAmount = emiAmount; }
    public void setRemainingAmount(Double remainingAmount) { this.remainingAmount = remainingAmount; }
    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }

    public void setRemarks(String remarks) { this.remarks = remarks; }

    public void setStatus(LoanStatus status) {
        this.status = status;

        if (status == LoanStatus.APPROVED) {
            this.approvedAt = LocalDateTime.now();

            // 🔥 Auto EMI setup
            this.emiAmount = calculateEMI();
            this.remainingAmount = getTotalPayment();
            this.nextDueDate = LocalDate.now().plusMonths(1);
        }
    }

    // ================= EMI CALCULATION =================
    public double calculateEMI() {
        if (amount == null || interestRate == null || tenure == null) return 0;

        double P = amount;
        double R = interestRate / 12 / 100;
        int N = tenure;

        double emi = (P * R * Math.pow(1 + R, N)) /
                     (Math.pow(1 + R, N) - 1);

        return Math.round(emi);
    }

    public double getTotalPayment() {
        return calculateEMI() * tenure;
    }

    public double getTotalInterest() {
        return getTotalPayment() - amount;
    }

    // ================= 🔥 PAY EMI =================
    public void payEmi() {

        if (remainingAmount == null || remainingAmount <= 0) {
            throw new RuntimeException("Loan already paid!");
        }

        if (emiAmount == null) {
            throw new RuntimeException("EMI not initialized!");
        }

        remainingAmount -= emiAmount;
        totalPaidEmi++;

        // 🔥 Next due date update
        nextDueDate = nextDueDate.plusMonths(1);

        // 🔥 Loan completed
        if (remainingAmount <= 0) {
            remainingAmount = 0.0;
            status = LoanStatus.COMPLETED;
        }
    }
}