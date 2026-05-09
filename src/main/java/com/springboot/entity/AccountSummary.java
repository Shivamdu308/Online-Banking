package com.springboot.entity;

public class AccountSummary {
    private String accountType;
    private double balance;
    private double monthlyChange;
    private String changeType;  // "up" or "down"

    public AccountSummary(String accountType, double balance, double monthlyChange) {
        this.accountType = accountType;
        this.balance = balance;
        this.monthlyChange = monthlyChange;
        this.changeType = monthlyChange > 0 ? "up" : "down";
    }

    // Getters & Setters
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public double getMonthlyChange() { return monthlyChange; }
    public void setMonthlyChange(double monthlyChange) { this.monthlyChange = monthlyChange; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
}
