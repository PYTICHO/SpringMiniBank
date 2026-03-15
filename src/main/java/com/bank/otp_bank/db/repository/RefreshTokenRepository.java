package com.bank.otp_bank.db.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bank.otp_bank.db.entity.RefreshTokenEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);
}
