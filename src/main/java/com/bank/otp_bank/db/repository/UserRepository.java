package com.bank.otp_bank.db.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bank.otp_bank.db.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsUserByEmail(String email);
    boolean existsUserByPhone(String phone);
    Optional<UserEntity> findByEmail(String email);
}
