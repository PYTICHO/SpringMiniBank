package com.bank.otp_bank.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

import com.bank.otp_bank.db.dto.*;
import com.bank.otp_bank.db.entity.*;
import com.bank.otp_bank.db.repository.*;
import com.bank.otp_bank.db.status.CurrencyStatus;
import com.bank.otp_bank.exception.InvalidCredentialsException;
import com.bank.otp_bank.exception.InvalidRefreshTokenException;
import com.bank.otp_bank.exception.UserAlreadyExistsException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.*;


@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    

    @Transactional
    public AuthResponseDto register(CreateUserRequestDto requestDto) {
        String normalizedPhoneNumber = GlobalFunctions.normalizePhone(requestDto.phone());

        if (userRepository.existsUserByEmail(requestDto.email())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        if (userRepository.existsUserByPhone(normalizedPhoneNumber)) {
            throw new UserAlreadyExistsException("Пользователь с таким телефоном уже существует");
        }

        UserEntity newUser = UserEntity.builder()
                                .firstName(requestDto.firstName())
                                .lastName(requestDto.lastName())
                                .phone(normalizedPhoneNumber)
                                .email(requestDto.email())
                                .passwordHash(passwordEncoder.encode(requestDto.password()))
                                .createdAt(LocalDateTime.now())
                                .build();

        AccountEntity newAccount = AccountEntity.builder()
                                    .accountNumber(generateAccountNumber())
                                    .balance(BigDecimal.ZERO)
                                    .currency(CurrencyStatus.RUB)
                                    .createdAt(LocalDateTime.now())
                                    .user(newUser)
                                    .build();
        
        newUser.setAccount(newAccount);
        UserEntity savedUser = userRepository.save(newUser);
        
        RefreshTokenEntity newRefreshToken = tokenService.buildRefreshToken(savedUser);
        RefreshTokenEntity savedRefreshToken = refreshTokenRepository.save(newRefreshToken);

        return buildAuthResponse(savedUser, savedRefreshToken);
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto requestDto) {
        UserEntity user = userRepository.findByEmail(requestDto.email())
            .orElseThrow(() -> new InvalidCredentialsException("Неверный email или пароль"));

        if (!passwordEncoder.matches(requestDto.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Неверный email или пароль");
        }

        RefreshTokenEntity newRefreshToken = tokenService.buildRefreshToken(user);
        RefreshTokenEntity savedRefreshToken = refreshTokenRepository.save(newRefreshToken);

        return buildAuthResponse(user, savedRefreshToken);
    }

    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto requestDto) {
        RefreshTokenEntity existingRefreshToken = refreshTokenRepository.findByToken(requestDto.refreshToken())
            .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token не найден"));

        if (existingRefreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token уже отозван");
        }

        if (existingRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token истек");
        }

        existingRefreshToken.setRevoked(true);

        UserEntity user = existingRefreshToken.getUser();

        RefreshTokenEntity newRefreshToken = tokenService.buildRefreshToken(user);
        RefreshTokenEntity savedRefreshToken = refreshTokenRepository.save(newRefreshToken);

        return buildAuthResponse(user, savedRefreshToken);
    }
    


    
    // Functions
    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber;

        for (int i=0; i<50; i++) {
            accountNumber = new StringBuilder("40817");
            for (int j = 0; j < 15; j++) {
                accountNumber.append(random.nextInt(10));
            }
             
            if (!accountRepository.existsByAccountNumber(accountNumber.toString())) {
                return accountNumber.toString();
            }
        }
        
        throw new RuntimeException("Не удалось сгенерировать уникальный номер счёта");
    }

    private AuthResponseDto buildAuthResponse(UserEntity user, RefreshTokenEntity refreshToken) {
        return new AuthResponseDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
            user.getEmail(),
            tokenService.generateAccessToken(user),
            refreshToken.getToken(),
            refreshToken.getExpiresAt()
        );
    }
}
