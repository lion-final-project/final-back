package com.example.finalproject.global.jwt;

import com.example.finalproject.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties; 
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret이 설정되지 않았습니다.");//JWT secret 설정 확인
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret은 HS256 기준 32바이트(32자) 이상이어야 합니다.");//JWT secret 길이 확인
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user, List<String> roles) {
        return generateToken(user, roles, jwtProperties.getAccessTokenValiditySeconds()); //AT 생성
    }

    public String generateRefreshToken(User user, List<String> roles) {
        return generateToken(user, roles, jwtProperties.getRefreshTokenValiditySeconds()); //RT 생성
    }

    public Claims parseClaims(String token) {
        return Jwts.parser() //토큰 파싱
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload(); //토큰 페이로드 조회
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String generateToken(User user, List<String> roles, long validitySeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("roles", roles)
                .claim("ver", user.getTokenVersion())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(validitySeconds)))
                .signWith(key)
                .compact();
    }
}
