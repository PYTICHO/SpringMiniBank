package com.bank.otp_bank.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.otp_bank.db.status.CurrencyStatus;
import com.bank.otp_bank.db.status.TransactionStatus;
import com.bank.otp_bank.db.status.TransactionType;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private CurrencyStatus currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne   // may be null
    @JoinColumn(name = "from_account_id")
    private AccountEntity fromAccount;

    @ManyToOne   // may be null
    @JoinColumn(name = "to_account_id")
    private AccountEntity toAccount;
}