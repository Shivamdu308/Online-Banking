package com.springboot.controller;

import com.springboot.entity.Loan;
import com.springboot.entity.User;
import com.springboot.service.AccountService;
import com.springboot.service.LoanService;
import com.springboot.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/loan")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    // ================= COMMON METHOD =================
    private User getUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return null;
        return userService.findById(userId);
    }

    // ================= APPLY PAGE =================
    @GetMapping("/apply")
    public String applyPage(Model model, HttpSession session) {

        Object success = session.getAttribute("success");

        if (success != null) {
            model.addAttribute("success", success);
            session.removeAttribute("success");
        }

        return "apply-loan";
    }

    // ================= APPLY LOAN =================
    @PostMapping("/apply")
    public String applyLoan(
            @RequestParam double amount,
            @RequestParam String type,
            @RequestParam double income,
            @RequestParam String employment,
            @RequestParam int tenure,
            HttpSession session,
            Model model) {

        User user = getUser(session);

        if (user == null) return "redirect:/login";

        // 🔥 VALIDATION
        if (amount <= 0 || income <= 0 || tenure <= 0) {
            model.addAttribute("error", "❌ Invalid input values!");
            return "apply-loan";
        }

        try {
            loanService.applyLoan(user, amount, type, income, employment, tenure);
            session.setAttribute("success", "✅ Loan request submitted successfully!");

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "apply-loan";
        }

        return "redirect:/loan/details";
    }

    // ================= LOAN DETAILS =================
    @GetMapping("/details")
    public String loanDetails(HttpSession session, Model model) {

        User user = getUser(session);
        if (user == null) return "redirect:/login";

        Loan loan = loanService.getLoanByUser(user);

        if (loan == null) {
            model.addAttribute("error", "❌ No loan found!");
        }

        model.addAttribute("loan", loan);

        return "loan-details";
    }

    // ================= EMI PAGE =================
    @GetMapping("/emi")
    public String emiPage(HttpSession session, Model model) {

        User user = getUser(session);
        if (user == null) return "redirect:/login";

        Loan loan = loanService.getApprovedLoan(user);

        if (loan == null) {
            model.addAttribute("error", "❌ No approved loan found!");
            return "emi-payment";
        }

        model.addAttribute("loan", loan);

        // 🔥 UPI QR DATA (Dynamic)
        String upiUrl = "upi://pay?pa=mybank@upi&pn=MyBank&am="
                + (loan.getEmiAmount() != null ? loan.getEmiAmount() : 0)
                + "&cu=INR";

        model.addAttribute("qrData", upiUrl);

        return "emi-payment";
    }

    // ================= EMI PAYMENT =================
    @PostMapping("/pay-emi")
    public String payEmi(HttpSession session, RedirectAttributes ra) {

        User user = getUser(session);
        if (user == null) return "redirect:/login";

        try {
            loanService.payEmi(user);
            ra.addFlashAttribute("success", "✅ EMI Paid Successfully!");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/loan/emi";
        }

        return "redirect:/dashboard";
    }

    // ================= LOAN HISTORY =================
    @GetMapping("/history")
    public String loanHistory(HttpSession session, Model model) {

        User user = getUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("loans", loanService.getUserLoans(user));

        return "loan-history";
    }

    // ================= ADMIN APPROVE =================
    @GetMapping("/admin/approve/{id}")
    public String approveLoan(@PathVariable Long id) {
        loanService.approveLoan(id);
        return "redirect:/admin/loans";
    }

    // ================= ADMIN REJECT =================
    @GetMapping("/admin/reject/{id}")
    public String rejectLoan(@PathVariable Long id) {
        loanService.rejectLoan(id);
        return "redirect:/admin/loans";
    }
}