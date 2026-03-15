package com.bank.otp_bank.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.otp_bank.db.dto.AuthResponseDto;
import com.bank.otp_bank.db.dto.CreateUserRequestDto;
import com.bank.otp_bank.db.dto.LoginRequestDto;
import com.bank.otp_bank.db.dto.RefreshTokenRequestDto;
import com.bank.otp_bank.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public AuthResponseDto register(
        @Valid 
        @RequestBody 
        CreateUserRequestDto requestDto
    ) {
        return userService.register(requestDto);
    }

    @PostMapping("/login")
    public AuthResponseDto login(
        @Valid 
        @RequestBody 
        LoginRequestDto requestDto
    ) {
        return userService.login(requestDto);
    }

    @PostMapping("/refresh")
    public AuthResponseDto refresh(
        @Valid 
        @RequestBody 
        RefreshTokenRequestDto requestDto
    ) {
        return userService.refresh(requestDto);
    }
}
