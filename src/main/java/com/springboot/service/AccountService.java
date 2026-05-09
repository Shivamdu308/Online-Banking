package com.springboot.service;

import com.springboot.entity.Account;
import com.springboot.entity.User;
import com.springboot.repository.AccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    // ================= GET ACCOUNT BY USER =================
    public Account getAccountByUserId(Long userId) {

        if (userId == null) return null;

        return accountRepository.findByUser_Id(userId);
    }

    // ================= GET ACCOUNT BY NUMBER (SAFE) =================
    public Account getAccountByNumber(String accountNumber) {

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new RuntimeException("Invalid Account Number");
        }

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found"));
    }

    // ================= CREATE ACCOUNT =================
    public Account createAccount(User user, double initialDeposit) {

        if (user == null) {
            throw new RuntimeException("User cannot be null");
        }

        if (initialDeposit < 0) {
            throw new RuntimeException("Invalid initial deposit");
        }

        String accountNumber = generateAccountNumber();

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(accountNumber);
        account.setHolderName(user.getName());
        account.setBalance(initialDeposit);
        account.setInitialDeposit(initialDeposit);

        return accountRepository.save(account);
    }

    // ================= GENERATE ACCOUNT NUMBER =================
    private String generateAccountNumber() {
        return "MYB" + System.currentTimeMillis();
    }

    // ================= FIND ACCOUNT (OPTIONAL SAFE) =================
    public Account findByAccountNumber(String accountNumber) {

        if (accountNumber == null || accountNumber.isBlank()) {
            return null;
        }

        return accountRepository.findByAccountNumber(accountNumber).orElse(null);
    }

    // ================= UPDATE BALANCE =================
    public Account updateBalance(String accountNumber, double amount, boolean isCredit) {

        Account account = getAccountByNumber(accountNumber);

        if (amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        double balance = account.getBalance();

        if (isCredit) {
            balance += amount;
        } else {
            if (balance < amount) {
                throw new RuntimeException("Insufficient balance!");
            }
            balance -= amount;
        }

        account.setBalance(balance);

        return accountRepository.save(account);
    }

    // ================= SAVE =================
    public Account save(Account account) {

        if (account == null) {
            throw new RuntimeException("Account cannot be null");
        }

        return accountRepository.save(account);
    }

    // ================= ADMIN DASHBOARD =================
    public List<Account> getAllAccounts() {

        List<Account> accounts = accountRepository.findAll();

        if (accounts == null || accounts.isEmpty()) {
            return Collections.emptyList();
        }

        // 🔥 SAFE FILTER
        accounts.removeIf(acc -> acc == null || acc.getUser() == null);

        return accounts;
    }
}