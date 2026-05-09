package com.springboot.repository;

import com.springboot.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    // ================= USER LOGIN =================

    Optional<User> findByMobile(String mobile);

    Optional<User> findByEmail(String email);

    // ================= ADMIN LOGIN =================

    Optional<User> findByAdminId(String adminId);

    // ================= VALIDATION =================

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    boolean existsByAdminId(String adminId);
}