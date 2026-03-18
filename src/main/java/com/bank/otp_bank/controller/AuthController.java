package com.bank.otp_bank.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Authentication", description = "Операции аутентификации")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public AuthResponseDto register(
        @Valid 
        @RequestBody 
        CreateUserRequestDto request
    ) {
        AuthResponseDto newUser = userService.register(request);
        log.info("Создан новый пользователь c id: {}", newUser.userId());
        return newUser;
    }

    @PostMapping("/login")
    public AuthResponseDto login(
        @Valid 
        @RequestBody 
        LoginRequestDto request
    ) {
        AuthResponseDto user = userService.login(request);
        log.info("Авторизация пользователя с id: {}", user.userId());
        return user;
    }

    @PostMapping("/refresh")
    public AuthResponseDto refresh(
        @Valid 
        @RequestBody 
        RefreshTokenRequestDto request
    ) {
        AuthResponseDto user = userService.refresh(request);
        log.info("Обновил refresh_token и access_token для пользователя с id: {}", user.userId());
        return user;
    }
}
