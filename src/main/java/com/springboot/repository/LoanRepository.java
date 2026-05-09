package com.springboot.repository;

import com.springboot.entity.Loan;
import com.springboot.entity.LoanStatus;
import com.springboot.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // ================= USER LOANS =================
    List<Loan> findByUser(User user);

    // 🔥 USER LOANS (LATEST FIRST)
    List<Loan> findByUserOrderByAppliedAtDesc(User user);

    // 🔥 LATEST LOAN
    Optional<Loan> findTopByUserOrderByAppliedAtDesc(User user);

    // ================= STATUS FILTER =================
    List<Loan> findByStatus(LoanStatus status);

    // 🔥 ADMIN (LATEST FIRST)
    List<Loan> findByStatusOrderByAppliedAtDesc(LoanStatus status);

    // ================= USER + STATUS =================
    List<Loan> findByUserAndStatus(User user, LoanStatus status);

    // ================= USER ID BASED =================
    List<Loan> findByUserId(Long userId);

    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);

    // 🔥 EMI / ACTIVE LOAN (IMPORTANT - ONLY ONE METHOD)
    Optional<Loan> findTopByUserAndStatusOrderByAppliedAtDesc(User user, LoanStatus status);

    // ================= PAGINATION =================
    Page<Loan> findAll(Pageable pageable);

    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);
}