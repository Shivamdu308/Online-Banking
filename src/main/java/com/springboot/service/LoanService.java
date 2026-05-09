package com.springboot.service;

import com.springboot.entity.*;
import com.springboot.repository.LoanRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class LoanService {

    @Autowired
    private LoanRepository repo;

    @Autowired
    private AccountService accountService;

    // ================= APPLY LOAN =================
    public void applyLoan(User user,
                          double amount,
                          String type,
                          double income,
                          String employment,
                          int tenure) {

        if (user == null) throw new RuntimeException("User not found");
        if (amount <= 0) throw new RuntimeException("Invalid loan amount");
        if (income <= 0) throw new RuntimeException("Invalid income");
        if (tenure <= 0) throw new RuntimeException("Invalid tenure");

        Loan loan = new Loan();

        loan.setUser(user);
        loan.setAmount(amount);
        loan.setType(type);
        loan.setIncome(income);
        loan.setEmployment(employment);
        loan.setTenure(tenure);

        double rate = getInterestRate(type);
        loan.setInterestRate(rate);

        // 🔥 AUTO EMI CALCULATE (entity method)
        double emi = loan.calculateEMI();
        loan.setEmiAmount(emi);

        // 🔥 SET FULL PAYMENT (principal + interest)
        loan.setRemainingAmount(loan.getTotalPayment());

        loan.setNextDueDate(LocalDate.now().plusMonths(1));
        loan.setStatus(LoanStatus.PENDING);

        repo.save(loan);
    }

    // ================= INTEREST =================
    private double getInterestRate(String type) {

        if (type == null) return 10;

        switch (type.toLowerCase()) {
            case "home":
            case "home loan": return 8.0;
            case "personal":
            case "personal loan": return 12.0;
            case "education":
            case "education loan": return 7.0;
            default: return 10.0;
        }
    }

    // ================= GET ALL =================
    public List<Loan> getAllLoans() {
        List<Loan> loans = repo.findAll();
        return loans != null ? loans : Collections.emptyList();
    }

    // ================= GET BY ID =================
    public Loan getLoan(Long id) {
        return repo.findById(id).orElse(null);
    }

    // ================= USER LOANS =================
    public List<Loan> getUserLoans(User user) {
        if (user == null) return Collections.emptyList();
        return repo.findByUserOrderByAppliedAtDesc(user);
    }

    // ================= LATEST LOAN =================
    public Loan getLoanByUser(User user) {
        return repo.findTopByUserOrderByAppliedAtDesc(user).orElse(null);
    }

    // ================= APPROVED LOAN =================
    public Loan getApprovedLoan(User user) {
        return repo.findTopByUserAndStatusOrderByAppliedAtDesc(user, LoanStatus.APPROVED)
                   .orElse(null);
    }

    // ================= APPROVE =================
    @Transactional
    public void approveLoan(Long id) {

        Loan loan = getLoan(id);

        if (loan == null) throw new RuntimeException("Loan not found");

        if (loan.getStatus() == LoanStatus.APPROVED) {
            throw new RuntimeException("Already approved!");
        }

        Account acc = accountService.getAccountByUserId(loan.getUser().getId());

        if (acc == null) throw new RuntimeException("Account not found");

        // 💰 CREDIT LOAN AMOUNT
        acc.setBalance(acc.getBalance() + loan.getAmount());
        accountService.save(acc);

        // 🔥 AUTO EMI SETUP (handled in entity)
        loan.setStatus(LoanStatus.APPROVED);

        repo.save(loan);
    }

    // ================= PAY EMI =================
    @Transactional
    public void payEmi(User user) {

        Loan loan = getApprovedLoan(user);

        if (loan == null) throw new RuntimeException("No active loan");

        Account acc = accountService.getAccountByUserId(user.getId());

        if (acc == null) throw new RuntimeException("Account not found");

        double emi = loan.getEmiAmount() != null ? loan.getEmiAmount() : 0;

        if (acc.getBalance() < emi)
            throw new RuntimeException("Insufficient balance");

        // 💳 DEDUCT
        acc.setBalance(acc.getBalance() - emi);
        accountService.save(acc);

        // 🔥 USE ENTITY LOGIC (clean)
        loan.payEmi();

        repo.save(loan);
    }

    // ================= REJECT =================
    public void rejectLoan(Long id) {

        Loan loan = getLoan(id);

        if (loan == null) throw new RuntimeException("Loan not found");

        loan.setStatus(LoanStatus.REJECTED);

        repo.save(loan);
    }

    // ================= SAVE =================
    public void save(Loan loan) {
        repo.save(loan);
    }
}