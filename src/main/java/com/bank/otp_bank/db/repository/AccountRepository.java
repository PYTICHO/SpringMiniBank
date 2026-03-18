package com.bank.otp_bank.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.bank.otp_bank.db.entity.AccountEntity;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    boolean existsByAccountNumber(String accountNumber);
    Optional<AccountEntity> findByUserId(Long userId);

}
