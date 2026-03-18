package com.bank.otp_bank.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.bank.otp_bank.db.entity.CardEntity;

public interface CardRepository extends JpaRepository<CardEntity, Long> {
    boolean existsByCardNumber(String cardNumber);
    Optional<CardEntity> findByCardNumber(String cardNumber);
}
