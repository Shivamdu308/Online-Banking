package com.springboot.service;

import com.springboot.entity.User;
import com.springboot.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ================= REGISTER =================
    public User register(User user) {

        if (user == null) {

            throw new RuntimeException(
                    "Invalid user data");
        }

        String mobile =
                user.getMobile() != null
                ? user.getMobile().trim()
                : "";

        String email =
                user.getEmail() != null
                ? user.getEmail().trim()
                : "";

        // ================= MOBILE =================
        if (mobile.isBlank()) {

            throw new RuntimeException(
                    "Mobile is required");
        }

        if (repo.existsByMobile(mobile)) {

            throw new RuntimeException(
                    "Mobile already registered");
        }

        // ================= EMAIL =================
        if (!email.isBlank() &&
                repo.existsByEmail(email)) {

            throw new RuntimeException(
                    "Email already registered");
        }

        // ================= ROLE =================
        if (user.getRole() == null ||
                user.getRole().isBlank()) {

            user.setRole("USER");
        }

        // ================= USER PIN =================
        if ("USER".equalsIgnoreCase(
                user.getRole())) {

            if (user.getPin() == null ||
                    !user.getPin()
                    .matches("\\d{4}")) {

                throw new RuntimeException(
                        "PIN must be exactly 4 digits");
            }

            // 🔐 ENCRYPT PIN
            user.setPin(
                    passwordEncoder.encode(
                            user.getPin()));
        }

        // ================= ADMIN =================
        if ("ADMIN".equalsIgnoreCase(
                user.getRole())) {

            if (user.getAdminId() == null ||
                    user.getAdminId().isBlank()) {

                throw new RuntimeException(
                        "Admin ID required");
            }

            if (user.getAdminPassword() == null ||
                    user.getAdminPassword().isBlank()) {

                throw new RuntimeException(
                        "Admin Password required");
            }

            // 🔥 ENCRYPT ADMIN PASSWORD
            user.setAdminPassword(
                    passwordEncoder.encode(
                            user.getAdminPassword()));
        }
        user.setMobile(mobile);
        user.setEmail(email);

        return repo.save(user);
    }

    // ================= USER LOGIN =================
    public User login(String mobile, String pin) {

        if (mobile == null ||
                pin == null ||
                mobile.isBlank() ||
                pin.isBlank()) {

            throw new RuntimeException(
                    "Mobile & PIN required");
        }

        User user =
                repo.findByMobile(
                        mobile.trim())

                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        if (!passwordEncoder.matches(
                pin,
                user.getPin())) {

            throw new RuntimeException(
                    "Invalid PIN");
        }

        return user;
    }

    // ================= ADMIN LOGIN =================
    public User findByAdminId(String adminId) {

        return repo.findByAdminId(adminId)
                .orElse(null);
    }

    // ================= FIND =================
    public User findById(Long id) {

        return id != null
                ? repo.findById(id).orElse(null)
                : null;
    }

    public User getById(Long id) {

        return repo.findById(id)

                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));
    }

    public User findByEmail(String email) {

        return email != null &&
                !email.isBlank()

                ? repo.findByEmail(
                email.trim()).orElse(null)

                : null;
    }

    public List<User> getAllUsers() {

        List<User> users =
                repo.findAll();

        return users != null
                ? users
                : Collections.emptyList();
    }

    // ================= UPDATE PIN =================
    public void updatePin(String email,
                          String newPin) {

        if (email == null ||
                email.isBlank()) {

            throw new RuntimeException(
                    "Email required");
        }

        if (newPin == null ||
                !newPin.matches("\\d{4}")) {

            throw new RuntimeException(
                    "PIN must be exactly 4 digits");
        }

        User user =
                repo.findByEmail(email.trim())

                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        user.setPin(
                passwordEncoder.encode(newPin));

        repo.save(user);
    }

    // ================= DELETE =================
    public void deleteUser(Long id) {

        if (id == null ||
                !repo.existsById(id)) {

            throw new RuntimeException(
                    "User not found");
        }

        repo.deleteById(id);
    }

    // ================= VALIDATION =================
    public boolean emailExists(String email) {

        return email != null &&
                !email.isBlank() &&

                repo.existsByEmail(
                        email.trim());
    }

    public boolean mobileExists(String mobile) {

        return mobile != null &&
                !mobile.isBlank() &&

                repo.existsByMobile(
                        mobile.trim());
    }
}