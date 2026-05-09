package com.springboot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionId;

    // ================= FROM ACCOUNT =================
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_account", nullable = false)
    private Account fromAccount;

    // ================= TO ACCOUNT =================
    @Column(name = "to_account", nullable = false)
    private String toAccountNumber;

    // 🔥 NEW (UPI SUPPORT)
    @Column(name = "upi_id")
    private String upiId;

    // ================= ENUMS =================
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    // ================= EXTRA INFO =================
    private String description;
    private String referenceId;
    private String mode;   // CASH / ATM / NETBANKING / UPI

    private double amount;

    private LocalDateTime timestamp;

    // ================= ENUMS =================
    public enum TransactionType {
        CREDIT, DEBIT, TRANSFER
    }

    public enum TransactionStatus {
        SUCCESS, FAILED, PENDING
    }

    // ================= CONSTRUCTOR =================
    public Transaction() {}

    public Transaction(Account fromAccount, String toAccountNumber,
                       TransactionType type, String description,
                       double amount, String mode) {

        this.fromAccount = fromAccount;
        this.toAccountNumber = toAccountNumber;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.mode = mode;

        this.transactionId = generateTxnId();
        this.referenceId = UUID.randomUUID().toString();
        this.status = TransactionStatus.SUCCESS;
        this.timestamp = LocalDateTime.now();
    }

    // 🔥 NEW CONSTRUCTOR (UPI SUPPORT)
    public Transaction(Account fromAccount, String toAccountNumber,
                       String upiId,
                       TransactionType type, String description,
                       double amount, String mode) {

        this.fromAccount = fromAccount;
        this.toAccountNumber = toAccountNumber;
        this.upiId = upiId;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.mode = mode;

        this.transactionId = generateTxnId();
        this.referenceId = UUID.randomUUID().toString();
        this.status = TransactionStatus.SUCCESS;
        this.timestamp = LocalDateTime.now();
    }

    // ================= AUTO SET =================
    @PrePersist
    public void prePersist() {
        if (timestamp == null) timestamp = LocalDateTime.now();
        if (transactionId == null) transactionId = generateTxnId();
        if (referenceId == null) referenceId = UUID.randomUUID().toString();
        if (status == null) status = TransactionStatus.SUCCESS;
    }

    // ================= HELPER =================
    private String generateTxnId() {
        return "TXN" + System.currentTimeMillis();
    }

    // ================= GETTERS =================
    public Long getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public Account getFromAccount() { return fromAccount; }
    public String getToAccountNumber() { return toAccountNumber; }
    public String getUpiId() { return upiId; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public String getReferenceId() { return referenceId; }
    public String getMode() { return mode; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}