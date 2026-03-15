package com.bank.otp_bank.service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bank.otp_bank.db.entity.RefreshTokenEntity;
import com.bank.otp_bank.db.entity.UserEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenService {

    private final Key signingKey;
    private final Long accessTokenTtlMinutes;
    private final Long refreshTokenTtlDays;

    public TokenService(
        @Value("${app.security.jwt.secret}") 
        String jwtSecret,

        @Value("${app.security.jwt.access-token-ttl-minutes:15}") 
        Long accessTokenTtlMinutes,

        @Value("${app.security.jwt.refresh-token-ttl-days:365}") 
        Long refreshTokenTtlDays
    ) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
    }

    public String generateAccessToken(UserEntity user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(accessTokenTtlMinutes);

        return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("userId", user.getId())
            .claim("email", user.getEmail())
            .claim("phone", user.getPhone())
            .claim("firstName", user.getFirstName())
            .setIssuedAt(toDate(now))
            .setExpiration(toDate(expiresAt))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean isAccessTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractSubject(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    public RefreshTokenEntity buildRefreshToken(UserEntity user) {
        LocalDateTime now = LocalDateTime.now();

        return RefreshTokenEntity.builder()
            .token(UUID.randomUUID().toString())
            .expiresAt(now.plusDays(refreshTokenTtlDays))
            .revoked(false)
            .createdAt(now)
            .user(user)
            .build();
    }

    private Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.toInstant(ZoneOffset.UTC));
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
