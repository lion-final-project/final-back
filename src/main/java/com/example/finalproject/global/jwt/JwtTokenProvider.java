package com.example.finalproject.global.jwt;

import com.example.finalproject.auth.dto.SocialSignupStateClaims;
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

    private static final String SUBJECT_SOCIAL_SIGNUP_STATE = "social_signup_state";
    private static final String CLAIM_REG = "reg";
    private static final String CLAIM_PUID = "puid";
    private static final String CLAIM_NICK = "nick";
    /** 소셜 추가 가입 state 토큰 유효 시간(초) */
    private static final long SOCIAL_SIGNUP_STATE_VALIDITY_SECONDS = 600L;

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

    /**
     * 소셜 추가 가입 완료 시 사용하는 단기 state JWT 발급.
     * 리다이렉트 URL의 state 쿼리로 전달되며, 프론트가 POST /api/auth/social-signup/complete 시 body에 담아 보냄.
     */
    public String generateSocialSignupStateToken(String registrationId, String providerUserId, String nickname) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(SUBJECT_SOCIAL_SIGNUP_STATE)
                .claim(CLAIM_REG, registrationId != null ? registrationId : "")
                .claim(CLAIM_PUID, providerUserId != null ? providerUserId : "")
                .claim(CLAIM_NICK, nickname != null ? nickname : "")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(SOCIAL_SIGNUP_STATE_VALIDITY_SECONDS)))
                .signWith(key)
                .compact();
    }

    /**
     * 소셜 가입 state 토큰 검증 및 클레임 추출.
     * 유효하지 않거나 만료되면 예외.
     */
    public SocialSignupStateClaims parseSocialSignupStateToken(String token) {
        Claims claims = parseClaims(token);
        if (!SUBJECT_SOCIAL_SIGNUP_STATE.equals(claims.getSubject())) {
            throw new IllegalArgumentException("Invalid social signup state token subject");
        }
        String reg = claims.get(CLAIM_REG, String.class);
        String puid = claims.get(CLAIM_PUID, String.class);
        String nick = claims.get(CLAIM_NICK, String.class);
        return new SocialSignupStateClaims(reg != null ? reg : "", puid != null ? puid : "", nick != null ? nick : "");
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
