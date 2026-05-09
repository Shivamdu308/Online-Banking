package com.springboot.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.entity.Account;
import com.springboot.entity.Transaction;
import com.springboot.entity.Transaction.TransactionType;
import com.springboot.repository.TransactionRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    // ================= SAVE =================
    public Transaction saveTransaction(Account fromAccount, String toAccountNumber,
                                       TransactionType type, String description,
                                       double amount, String mode) {

        if (fromAccount == null) {
            throw new RuntimeException("Account not found");
        }

        Transaction txn = new Transaction(
                fromAccount,
                toAccountNumber,
                type,
                description,
                amount,
                mode
        );

        return transactionRepository.save(txn);
    }

    // ================= RECENT =================
    public List<Transaction> getRecentTransactions(String accountNumber) {

        if (accountNumber == null || accountNumber.isBlank()) {
            return Collections.emptyList();
        }

        List<Transaction> list = transactionRepository
                .findTop5ByFromAccount_AccountNumberOrToAccountNumberOrderByTimestampDesc(
                        accountNumber, accountNumber);

        return list != null ? list : Collections.emptyList();
    }

    // ================= ALL =================
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = transactionRepository.findAll();
        return list != null ? list : Collections.emptyList();
    }

    // ================= HISTORY =================
    public List<Transaction> getTransactionsByAccount(String accountNumber) {

        if (accountNumber == null || accountNumber.isBlank()) {
            return Collections.emptyList();
        }

        List<Transaction> list = transactionRepository
                .findByFromAccount_AccountNumberOrToAccountNumberOrderByTimestampDesc(
                        accountNumber, accountNumber);

        return list != null ? list : Collections.emptyList();
    }

    // ================= TOTAL CREDIT =================
    public double getTotalCredit(String accountNumber) {

        List<Transaction> list = getTransactionsByAccount(accountNumber);

        return list.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.CREDIT)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // ================= TOTAL DEBIT =================
    public double getTotalDebit(String accountNumber) {

        List<Transaction> list = getTransactionsByAccount(accountNumber);

        return list.stream()
                .filter(t -> t.getType() != Transaction.TransactionType.CREDIT)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // ================= DEPOSIT =================
    @Transactional
    public String deposit(String accountNumber, double amount) {

        Account account = accountService.findByAccountNumber(accountNumber);

        if (account == null) {
            throw new RuntimeException("Account not found");
        }

        accountService.updateBalance(accountNumber, amount, true);

        saveTransaction(account, accountNumber,
                TransactionType.CREDIT,
                "Cash Deposit",
                amount,
                "CASH");

        return "Deposit successful!";
    }

    // ================= WITHDRAW =================
    @Transactional
    public String withdraw(String accountNumber, double amount) {

        Account account = accountService.findByAccountNumber(accountNumber);

        if (account == null) {
            throw new RuntimeException("Account not found");
        }

        if (account.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance!");
        }

        accountService.updateBalance(accountNumber, amount, false);

        saveTransaction(account, accountNumber,
                TransactionType.DEBIT,
                "ATM Withdrawal",
                amount,
                "ATM");

        return "Withdrawal successful!";
    }

    // ================= NORMAL TRANSFER =================
    @Transactional
    public String fundTransfer(String fromAccountNumber, String toAccountNumber, double amount) {

        Account sender = accountService.findByAccountNumber(fromAccountNumber);
        Account receiver = accountService.findByAccountNumber(toAccountNumber);

        if (sender == null || receiver == null) {
            throw new RuntimeException("Account not found");
        }

        if (sender.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance!");
        }

        accountService.updateBalance(fromAccountNumber, amount, false);
        accountService.updateBalance(toAccountNumber, amount, true);

        saveTransaction(sender, toAccountNumber,
                TransactionType.TRANSFER,
                "Transfer to " + toAccountNumber,
                amount,
                "NETBANKING");

        saveTransaction(receiver, fromAccountNumber,
                TransactionType.CREDIT,
                "Received from " + fromAccountNumber,
                amount,
                "NETBANKING");

        return "Transfer successful!";
    }

    // ================= UPI TRANSFER =================
    @Transactional
    public String upiTransfer(String fromAccountNumber, String upiId, double amount) {

        if (upiId == null || !upiId.contains("@")) {
            throw new RuntimeException("Invalid UPI ID!");
        }

        Account sender = accountService.findByAccountNumber(fromAccountNumber);

        String username = upiId.split("@")[0];
        String receiverAccountNumber = username;

        Account receiver = accountService.findByAccountNumber(receiverAccountNumber);

        if (receiver == null) {
            throw new RuntimeException("UPI not linked to any account!");
        }

        if (sender.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance!");
        }

        accountService.updateBalance(fromAccountNumber, amount, false);
        accountService.updateBalance(receiverAccountNumber, amount, true);

        saveTransaction(sender, receiverAccountNumber,
                TransactionType.TRANSFER,
                "UPI Transfer to " + upiId,
                amount,
                "UPI");

        saveTransaction(receiver, fromAccountNumber,
                TransactionType.CREDIT,
                "Received via UPI from " + upiId,
                amount,
                "UPI");

        return "UPI Transfer successful!";
    }
}