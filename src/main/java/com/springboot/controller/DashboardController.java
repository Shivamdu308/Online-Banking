package com.springboot.controller;

import com.springboot.entity.Account;
import com.springboot.entity.Loan;
import com.springboot.entity.Transaction;
import com.springboot.entity.User;
import com.springboot.service.AccountService;
import com.springboot.service.TransactionService;
import com.springboot.service.LoanService;
import com.springboot.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private UserService userService;

    // ================= SESSION USER =================
    private Long getUser(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Long userId = getUser(session);

        if (userId == null) {
            return "redirect:/login";
        }

        try {

            // ✅ SAFE USER
            User user = userService.findById(userId);
            if (user == null) {
                return "redirect:/login";
            }

            // ✅ SAFE ACCOUNT
            Account acc = accountService.getAccountByUserId(userId);

            if (acc == null) {
                System.out.println("❌ Account NULL for userId: " + userId);
                return "redirect:/login"; // safer
            }

            // ✅ BASIC INFO
            model.addAttribute("userName", user.getName());
            model.addAttribute("accountNumber", acc.getAccountNumber());
            model.addAttribute("savingsBalance", acc.getBalance());
            model.addAttribute("currentBalance", acc.getBalance() * 0.45);

            // ✅ SAFE TRANSACTIONS
            List<?> txns = transactionService.getRecentTransactions(acc.getAccountNumber());
            model.addAttribute("recentTransactions",
                    txns != null ? txns : Collections.emptyList());

            // ✅ SAFE LOANS
            List<Loan> loans = loanService.getUserLoans(user);
            model.addAttribute("loan",
                    (loans != null && !loans.isEmpty()) ? loans.get(0) : null);

            return "dashboard";

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 check console

            model.addAttribute("error", "Something went wrong");
            return "dashboard";
        }
    }

    // ================= PASSBOOK =================
 // ================= PASSBOOK =================
    @GetMapping("/passbook")
    public String passbook(HttpSession session, Model model) {

        Long userId = getUser(session);

        if (userId == null) {
            return "redirect:/login";
        }

        try {

            Account acc = accountService.getAccountByUserId(userId);

            if (acc == null) {
                model.addAttribute("transactions", Collections.emptyList());
                model.addAttribute("totalCredit", 0);
                model.addAttribute("totalDebit", 0);
                return "passbook";
            }

            String accNo = acc.getAccountNumber();

            // 🔥 TYPE FIX (IMPORTANT)
            List<Transaction> txList = transactionService.getTransactionsByAccount(accNo);

            if (txList == null) {
                txList = Collections.emptyList();
            }

            model.addAttribute("account", acc);
            model.addAttribute("transactions", txList);

            // 🔥 SAFE CALCULATION
            double totalCredit = 0;
            double totalDebit = 0;

            for (Transaction t : txList) {
                if (t.getType() == Transaction.TransactionType.CREDIT) {
                    totalCredit += t.getAmount();
                } else {
                    totalDebit += t.getAmount();
                }
            }

            model.addAttribute("totalCredit", totalCredit);
            model.addAttribute("totalDebit", totalDebit);

            return "passbook";

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 MUST

            model.addAttribute("transactions", Collections.emptyList());
            model.addAttribute("totalCredit", 0);
            model.addAttribute("totalDebit", 0);

            return "passbook";
        }
    }    // ================= CARD =================
    @GetMapping("/card")
    public String card(HttpSession session, Model model) {

        Long userId = getUser(session);
        if (userId == null) return "redirect:/login";

        try {
            model.addAttribute("account",
                    accountService.getAccountByUserId(userId));

            return "card";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/dashboard";
        }
    }

    // ================= TRANSACTIONS =================
    @GetMapping("/transactions")
    public String transactions(HttpSession session, Model model) {

        Long userId = getUser(session);
        if (userId == null) return "redirect:/login";

        try {

            Account acc = accountService.getAccountByUserId(userId);
            if (acc == null) return "redirect:/login";

            model.addAttribute("transactions",
                    transactionService.getTransactionsByAccount(acc.getAccountNumber()));

            return "transactions";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/dashboard";
        }
    }

    // ================= DOWNLOAD PDF =================
    @GetMapping("/download-pdf")
    public ResponseEntity<ByteArrayResource> downloadPdf(HttpSession session) {

        Long userId = getUser(session);
        if (userId == null) return ResponseEntity.badRequest().build();

        try {

            Account acc = accountService.getAccountByUserId(userId);
            if (acc == null) return ResponseEntity.badRequest().build();

            String data = "Account Statement\nAccount No: " + acc.getAccountNumber()
                    + "\nBalance: ₹" + acc.getBalance();

            byte[] pdfBytes = data.getBytes();

            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statement.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}