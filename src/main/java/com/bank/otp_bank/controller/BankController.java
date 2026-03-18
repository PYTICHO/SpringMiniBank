package com.bank.otp_bank.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.bank.otp_bank.db.dto.CreateCardResponseDto;
import com.bank.otp_bank.db.dto.TransactionRequestDto;
import com.bank.otp_bank.db.dto.TransactionResponseDto;
import com.bank.otp_bank.service.BankService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@Slf4j
@Tag(name = "Bank Operations", description = "Банковские операции")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banking")
public class BankController {

    private final BankService bankService;
    
    // проверка авторизован ли я
    @GetMapping("/checkAuth")
    public String checkAuth() {
        return "You authenticated!";
    }

    @PostMapping("/create_card")
    public CreateCardResponseDto createCard(
        Authentication authentication
    ) {
        CreateCardResponseDto response = bankService.create_card(authentication.getName());
        log.info("Created new card for user with account id: {}, card number: {}", response.accountId(), response.cardNumber());
        return response;
    }
    

    @PostMapping("/make_transaction")
    public TransactionResponseDto makeTransaction(
        Authentication authentication,
        @Valid
        @RequestBody 
        TransactionRequestDto request
    ) {
        TransactionResponseDto response = bankService.make_transaction(authentication.getName(), request);

        return response;
    }
    
}
