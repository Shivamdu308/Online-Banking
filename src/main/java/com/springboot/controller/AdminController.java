package com.springboot.controller;

import com.springboot.entity.*;
import com.springboot.service.*;
import com.springboot.repository.FeedbackRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private FeedbackRepository feedbackRepo;

    // ================= ADMIN CHECK =================
    private boolean isAdmin(HttpSession session) {

        Object role =
                session.getAttribute("userRole");

        return role != null &&
                role.toString()
                        .equalsIgnoreCase("ADMIN");
    }

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session,
                            Model model) {

        try {

            if (!isAdmin(session)) {

                return "redirect:/login";
            }

            // ================= ACCOUNTS =================

            List<Account> accounts =
                    Optional.ofNullable(
                            accountService.getAllAccounts())

                    .orElse(Collections.emptyList())

                    .stream()

                    .filter(acc ->
                            acc != null &&
                            acc.getUser() != null)

                    .collect(Collectors.toList());

            double totalBalance =
                    accounts.stream()

                    .mapToDouble(Account::getBalance)

                    .sum();

            // ================= LOANS =================

            List<Loan> loans =
                    Optional.ofNullable(
                            loanService.getAllLoans())

                    .orElse(Collections.emptyList());

            long totalLoans = loans.size();

            long pendingLoans =
                    loans.stream()

                    .filter(l ->
                            l.getStatus() != null &&
                            l.getStatus()
                                    .name()
                                    .equals("PENDING"))

                    .count();

            long approvedLoans =
                    loans.stream()

                    .filter(l ->
                            l.getStatus() != null &&
                            l.getStatus()
                                    .name()
                                    .equals("APPROVED"))

                    .count();

            long rejectedLoans =
                    loans.stream()

                    .filter(l ->
                            l.getStatus() != null &&
                            l.getStatus()
                                    .name()
                                    .equals("REJECTED"))

                    .count();

            double totalLoanAmount =
                    loans.stream()

                    .mapToDouble(l ->
                            l.getAmount() != null
                                    ? l.getAmount()
                                    : 0)

                    .sum();

            long activeLoans =
                    approvedLoans;

            // ================= EMI =================

            long paidEmi =
                    approvedLoans;

            long pendingEmi =
                    pendingLoans;

            long overdueEmi =
                    loans.stream()

                    .filter(l ->
                            l.getNextDueDate() != null &&

                            l.getNextDueDate()
                                    .isBefore(
                                            java.time.LocalDate.now()))

                    .count();

            // ================= TRANSACTIONS =================

            List<Transaction> allTxns =
                    Optional.ofNullable(
                            transactionService.getAllTransactions())

                    .orElse(Collections.emptyList());

            List<Transaction> recentTransactions =
                    allTxns.stream()

                    .filter(t ->
                            t.getTimestamp() != null)

                    .sorted(
                            Comparator.comparing(
                                    Transaction::getTimestamp)

                                    .reversed())

                    .limit(5)

                    .collect(Collectors.toList());

            long depositCount =
                    allTxns.stream()

                    .filter(t ->
                            t.getType() != null &&
                            t.getType()
                                    .name()
                                    .equals("CREDIT"))

                    .count();

            long withdrawCount =
                    allTxns.stream()

                    .filter(t ->
                            t.getType() != null &&
                            t.getType()
                                    .name()
                                    .equals("DEBIT"))

                    .count();

            long transferCount =
                    allTxns.stream()

                    .filter(t ->
                            t.getType() != null &&
                            t.getType()
                                    .name()
                                    .equals("TRANSFER"))

                    .count();

            // ================= ALERTS =================

            List<String> alerts =
                    new ArrayList<>();

            if (overdueEmi > 0) {

                alerts.add(
                        "⚠ EMI Overdue: "
                                + overdueEmi);
            }

            if (pendingLoans > 5) {

                alerts.add(
                        "⚠ Too many pending loans");
            }

            if (withdrawCount > depositCount) {

                alerts.add(
                        "⚠ High withdrawals detected");
            }

            if (alerts.isEmpty()) {

                alerts.add(
                        "✅ System Stable");
            }

            // ================= MODEL =================

            model.addAttribute(
                    "accounts",
                    accounts);

            model.addAttribute(
                    "totalUsers",
                    accounts.size());

            model.addAttribute(
                    "totalBalance",
                    totalBalance);

            model.addAttribute(
                    "totalLoans",
                    totalLoans);

            model.addAttribute(
                    "pendingLoans",
                    pendingLoans);

            model.addAttribute(
                    "approvedLoans",
                    approvedLoans);

            model.addAttribute(
                    "rejectedLoans",
                    rejectedLoans);

            model.addAttribute(
                    "totalLoanAmount",
                    totalLoanAmount);

            model.addAttribute(
                    "activeLoans",
                    activeLoans);

            model.addAttribute(
                    "paidEmi",
                    paidEmi);

            model.addAttribute(
                    "pendingEmi",
                    pendingEmi);

            model.addAttribute(
                    "overdueEmi",
                    overdueEmi);

            model.addAttribute(
                    "recentTransactions",
                    recentTransactions);

            model.addAttribute(
                    "alerts",
                    alerts);

            model.addAttribute(
                    "depositCount",
                    depositCount);

            model.addAttribute(
                    "withdrawCount",
                    withdrawCount);

            model.addAttribute(
                    "transferCount",
                    transferCount);

            model.addAttribute(
                    "feedbacks",
                    feedbackRepo.findAll());

            return "admin-dashboard";

        } catch (Exception e) {

            e.printStackTrace();

            return "redirect:/login";
        }
    }

    // ================= DELETE FEEDBACK =================
    @GetMapping("/feedback/delete/{id}")
    public String deleteFeedback(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {

            return "redirect:/login";
        }

        feedbackRepo.deleteById(id);

        return "redirect:/admin/dashboard";
    }

    // ================= DELETE USER =================
    @GetMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {

            return "redirect:/login";
        }

        userService.deleteUser(id);

        return "redirect:/admin/dashboard";
    }

    // ================= SEARCH ACCOUNT =================
    @PostMapping("/search-account")
    public String searchAccount(

            @RequestParam String accountNumber,

            Model model) {

        model.addAttribute(
                "searchedAccount",

                accountService
                        .getAccountByNumber(accountNumber));

        return "admin-dashboard";
    }

    // ================= TRANSACTIONS =================
    @GetMapping("/transactions")
    public String transactions(
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {

            return "redirect:/login";
        }

        model.addAttribute(
                "transactions",

                Optional.ofNullable(
                        transactionService.getAllTransactions())

                        .orElse(Collections.emptyList()));

        return "admin-transactions";
    }

    // ================= OPEN ACCOUNT PAGE =================
    @GetMapping("/open-account")
    public String openAccountPage(
            Model model,
            HttpSession session) {

        if (!isAdmin(session)) {

            return "redirect:/login";
        }

        model.addAttribute(
                "user",
                new User());

        return "admin-open-account";
    }

    // ================= OPEN ACCOUNT =================
    @PostMapping("/open-account")
    public String openAccount(

            @ModelAttribute User user,

            @RequestParam double initialDeposit,

            HttpSession session,

            Model model) {

        if (!isAdmin(session)) {

            return "redirect:/login";
        }

        try {

            user.setRole("USER");

            User saved =
                    userService.register(user);

            accountService.createAccount(
                    saved,
                    initialDeposit);

            return "redirect:/admin/dashboard";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    e.getMessage());

            return "admin-open-account";
        }
    }

    // ================= LOANS =================
    @GetMapping("/loans")
    public String loans(
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {

            return "redirect:/login";
        }

        model.addAttribute(
                "loans",

                Optional.ofNullable(
                        loanService.getAllLoans())

                        .orElse(Collections.emptyList()));

        return "admin-loans";
    }

    // ================= APPROVE LOAN =================
    @GetMapping("/loan/approve/{id}")
    public String approve(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {

            return "redirect:/login";
        }

        loanService.approveLoan(id);

        return "redirect:/admin/loans";
    }

    // ================= REJECT LOAN =================
    @GetMapping("/loan/reject/{id}")
    public String reject(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {

            return "redirect:/login";
        }

        loanService.rejectLoan(id);

        return "redirect:/admin/loans";
    }

    // ================= EXPORT =================
    @GetMapping("/export")
    public String export(HttpSession session) {

        if (!isAdmin(session)) {

            return "redirect:/login";
        }

        return "redirect:/admin/dashboard";
    }
}