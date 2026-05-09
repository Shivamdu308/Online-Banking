package com.springboot.controller;

import com.springboot.entity.Account;
import com.springboot.service.AccountService;
import com.springboot.service.TransactionService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class TransactionController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    private Account getAccount(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return null;
        return accountService.getAccountByUserId(userId);
    }

    // ================= DEPOSIT =================
    @GetMapping("/deposit")
    public String depositPage() {
        return "deposit";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam double amount,
                          HttpSession session) {

        Account acc = getAccount(session);
        if (acc == null) return "redirect:/login";

        try {
            transactionService.deposit(acc.getAccountNumber(), amount);
        } catch (Exception e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }

        return "redirect:/dashboard?success=deposit";
    }

    // ================= WITHDRAW =================
    @GetMapping("/withdraw")
    public String withdrawPage() {
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam double amount,
                           HttpSession session) {

        Account acc = getAccount(session);
        if (acc == null) return "redirect:/login";

        try {
            transactionService.withdraw(acc.getAccountNumber(), amount);
        } catch (Exception e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }

        return "redirect:/dashboard?success=withdraw";
    }

    // ================= TRANSFER =================
    @GetMapping("/transfer")
    public String transferPage() {
        return "transfer";
    }

    @PostMapping("/transfer")
    public String transfer(
            @RequestParam String paymentType,
            @RequestParam(required = false) String toAccount,
            @RequestParam(required = false) String upiId,
            @RequestParam(required = false) String upiNote,
            @RequestParam double amount,
            HttpSession session) {

        Account sender = getAccount(session);
        if (sender == null) return "redirect:/login";

        try {

            // ================= BANK =================
            if ("BANK".equals(paymentType)) {

                if (toAccount == null || toAccount.isEmpty()) {
                    return "redirect:/transfer?error=Enter receiver account";
                }

                transactionService.fundTransfer(
                        sender.getAccountNumber(),
                        toAccount,
                        amount
                );
            }

            // ================= UPI =================
            else if ("UPI".equals(paymentType)) {

                if (upiId == null || upiId.isEmpty()) {
                    return "redirect:/transfer?error=Enter UPI ID";
                }

                // 🔥 CORRECT CALL
                transactionService.upiTransfer(
                        sender.getAccountNumber(),
                        upiId,
                        amount
                );
            }

        } catch (Exception e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }

        return "redirect:/dashboard?success=transfer";
    }
}