package com.springboot.repository;

import com.springboot.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ✅ RECENT TRANSACTIONS
    List<Transaction> findTop5ByFromAccount_AccountNumberOrToAccountNumberOrderByTimestampDesc(
            String fromAcc, String toAcc
    );

    // ✅ FULL HISTORY
    List<Transaction> findByFromAccount_AccountNumberOrToAccountNumberOrderByTimestampDesc(
            String fromAcc, String toAcc
    );
}