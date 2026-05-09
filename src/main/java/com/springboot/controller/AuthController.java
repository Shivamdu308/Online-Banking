package com.springboot.controller;

import com.springboot.entity.Account;
import com.springboot.entity.User;
import com.springboot.service.AccountService;
import com.springboot.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ================= HOME =================
    @GetMapping("/")
    public String home(HttpSession session) {

        if (session.getAttribute("userId") != null) {

            String role =
                    (String) session.getAttribute("userRole");

            if ("ADMIN".equals(role)) {

                return "redirect:/admin/dashboard";
            }

            return "redirect:/dashboard";
        }

        return "home";
    }

    // ================= LOGIN PAGE =================
    @GetMapping("/login")
    public String loginPage(HttpSession session) {

        Object role =
                session.getAttribute("userRole");

        Object userId =
                session.getAttribute("userId");

        // USER LOGIN
        if (userId != null &&
                "USER".equals(role)) {

            return "redirect:/dashboard";
        }

        // ADMIN LOGIN
        if (userId != null &&
                "ADMIN".equals(role)) {

            return "redirect:/admin/dashboard";
        }

        // NOT LOGIN
        return "login";
    }    // ================= SIGNUP PAGE =================
    @GetMapping("/signup")
    public String signupPage(Model model) {

        model.addAttribute("user", new User());

        return "signup";
    }

    // ================= SIGNUP =================
    @PostMapping("/signup")
    public String register(@Valid
                           @ModelAttribute("user")
                           User user,

                           BindingResult result,
                           Model model) {

        if (result.hasErrors()) {

            model.addAttribute("error",
                    result.getAllErrors()
                    .get(0)
                    .getDefaultMessage());

            return "signup";
        }

        try {

            // 🔥 DEFAULT ROLE
            if (user.getRole() == null ||
                    user.getRole().isBlank()) {

                user.setRole("USER");
            }

            // 🔥 SAVE USER / ADMIN
            User savedUser =
                    userService.register(user);

            // 🔥 ACCOUNT CREATE
            accountService.createAccount(savedUser, 0);

            model.addAttribute("success",
                    "Account created successfully!");

            return "login";

        } catch (Exception e) {

            model.addAttribute("error",
                    e.getMessage());

            return "signup";
        }
    }

 // ================= LOGIN =================
    @PostMapping("/login")
    public String login(

            @RequestParam(required = false)
            String accountNumber,

            @RequestParam(required = false)
            String pin,

            @RequestParam(required = false)
            String adminId,

            @RequestParam(required = false)
            String adminPassword,

            @RequestParam(defaultValue = "USER")
            String role,

            HttpSession session,
            Model model) {

        try {

            // ================= USER LOGIN =================
            if ("USER".equalsIgnoreCase(role)) {

                if (accountNumber == null ||
                        pin == null ||
                        accountNumber.isBlank() ||
                        pin.isBlank()) {

                    model.addAttribute("error",
                            "Enter account number & PIN");

                    return "login";
                }

                Account acc =
                        accountService
                        .findByAccountNumber(accountNumber);

                if (acc == null ||
                        acc.getUser() == null) {

                    model.addAttribute("error",
                            "Invalid account");

                    return "login";
                }

                User user = acc.getUser();

                if (!passwordEncoder.matches(
                        pin,
                        user.getPin())) {

                    model.addAttribute("error",
                            "Invalid PIN");

                    return "login";
                }

                // ✅ USER SUCCESS
                session.setAttribute(
                        "userId",
                        user.getId());

                session.setAttribute(
                        "userName",
                        user.getName());

                session.setAttribute(
                        "accountNumber",
                        acc.getAccountNumber());

                session.setAttribute(
                        "userRole",
                        "USER");

                return "redirect:/dashboard";
            }

            // ================= ADMIN LOGIN =================
         // ================= ADMIN LOGIN =================
            if ("ADMIN".equalsIgnoreCase(role)) {

                User admin =
                        userService.findByAdminId(adminId);

                if (admin != null &&
                        passwordEncoder.matches(
                                adminPassword,
                                admin.getAdminPassword())) {

                    session.setAttribute(
                            "userId",
                            admin.getId());

                    session.setAttribute(
                            "userName",
                            admin.getName());

                    session.setAttribute(
                            "userRole",
                            "ADMIN");

                    return "redirect:/admin/dashboard";
                }

                model.addAttribute(
                        "error",
                        "Invalid Admin credentials");

                return "login";
            }
        } catch (Exception e) {

            e.printStackTrace();

            model.addAttribute(
                    "error",
                    "Login failed");

            return "login";
        }

        return "login";
    }
    
    // ================= FORGOT PIN =================
    @GetMapping("/forgot-pin")
    public String forgotPinPage() {

        return "forgot-pin";
    }

    // ================= SEND OTP =================
    @PostMapping("/send-otp")
    public String sendOtp(

            @RequestParam String email,
            HttpSession session,
            Model model) {

        User user =
                userService.findByEmail(email);

        if (user == null) {

            model.addAttribute("error",
                    "Email not found!");

            return "forgot-pin";
        }

        int otp =
                (int)(Math.random()*900000)+100000;

        // 🔥 SAVE OTP
        session.setAttribute("otp", otp);
        session.setAttribute("email", email);

        session.setAttribute(
                "otpTime",
                System.currentTimeMillis());

        sendOtpEmail(email, otp);

        model.addAttribute("message",
                "OTP sent to your email!");

        return "verify-otp";
    }

    // ================= VERIFY OTP =================
    @PostMapping("/verify-otp")
    public String verifyOtp(

            @RequestParam String otp,
            @RequestParam String newPin,

            HttpSession session,
            Model model) {

        Integer sessionOtp =
                (Integer) session.getAttribute("otp");

        String email =
                (String) session.getAttribute("email");

        Long otpTime =
                (Long) session.getAttribute("otpTime");

        if (sessionOtp == null || email == null) {

            model.addAttribute("error",
                    "Session expired. Try again.");

            return "forgot-pin";
        }

        // 🔥 OTP Expire
        if (System.currentTimeMillis()
                - otpTime > 120000) {

            model.addAttribute("error",
                    "OTP expired!");

            return "verify-otp";
        }

        if (!sessionOtp.toString().equals(otp)) {

            model.addAttribute("error",
                    "Invalid OTP");

            return "verify-otp";
        }

        if (newPin == null ||
                !newPin.matches("\\d{4}")) {

            model.addAttribute("error",
                    "PIN must be 4 digits");

            return "verify-otp";
        }

        // 🔥 UPDATE PIN
        userService.updatePin(email, newPin);

        // 🔥 CLEAR SESSION
        session.removeAttribute("otp");
        session.removeAttribute("email");
        session.removeAttribute("otpTime");

        model.addAttribute("success",
                "PIN updated successfully!");

        return "login";
    }

    // ================= LOGOUT =================
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }

    // ================= EMAIL =================
    private void sendOtpEmail(String to, int otp) {

        SimpleMailMessage msg =
                new SimpleMailMessage();

        msg.setTo(to);

        msg.setSubject("MyBank Secure OTP");

        msg.setText(
                "Your OTP is: " + otp + "\n\n" +
                "Do not share this OTP.\n" +
                "Valid for 2 minutes."
        );

        mailSender.send(msg);
    }

    // ================= FEEDBACK =================
    @Autowired
    private com.springboot.repository
            .FeedbackRepository feedbackRepo;

    @GetMapping("/feedback/list")
    public String showFeedback(Model model) {

        model.addAttribute(
                "feedbacks",
                feedbackRepo.findAll());

        return "feedback-list";
    }

    @GetMapping("/feedback")
    public String openFeedbackPage() {

        return "feedback";
    }

    // ================= SAVE FEEDBACK =================
    @PostMapping("/feedback")
    public String saveFeedback(

            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String rating,
            @RequestParam String feature,
            @RequestParam String message,

            Model model) {

        com.springboot.entity.Feedback f =
                new com.springboot.entity.Feedback();

        f.setName(name);
        f.setEmail(email);
        f.setRating(rating);
        f.setFeature(feature);
        f.setMessage(message);

        feedbackRepo.save(f);

        model.addAttribute("success",
                "Feedback submitted!");

        return "redirect:/";
    }
}