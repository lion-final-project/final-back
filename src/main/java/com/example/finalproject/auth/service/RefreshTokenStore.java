package com.example.finalproject.auth.service;

import com.example.finalproject.global.jwt.JwtProperties;
import com.example.finalproject.user.domain.User;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 리프레시 토큰을 Redis에 저장/조회/삭제합니다.
 * - rt:{token} → userId (TTL = 리프레시 토큰 유효기간)
 * - rt:user:{userId} → token (동일 유저 기존 토큰 삭제용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "rt:";
    private static final String USER_KEY_PREFIX = "rt:user:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public void save(User user, String refreshToken) {
        long ttlSeconds = jwtProperties.getRefreshTokenValiditySeconds();
        String tokenKey = KEY_PREFIX + refreshToken;
        String userKey = USER_KEY_PREFIX + user.getId();

        // 동일 유저 기존 토큰 삭제 (한 유저당 하나의 리프레시 토큰)
        Optional<String> existingToken = Optional.ofNullable(redisTemplate.opsForValue().get(userKey));
        existingToken.ifPresent(t -> redisTemplate.delete(KEY_PREFIX + t));

        redisTemplate.opsForValue().set(tokenKey, String.valueOf(user.getId()), ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(userKey, refreshToken, ttlSeconds, TimeUnit.SECONDS);
    }

    public Optional<Long> findUserIdByToken(String refreshToken) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + refreshToken);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            log.warn("[RefreshTokenStore] invalid userId in Redis: {}", value);
            return Optional.empty();
        }
    }

    public void deleteByToken(String refreshToken) {
        String tokenKey = KEY_PREFIX + refreshToken;
        String userId = redisTemplate.opsForValue().get(tokenKey);
        redisTemplate.delete(tokenKey);
        if (userId != null && !userId.isBlank()) {
            redisTemplate.delete(USER_KEY_PREFIX + userId);
        }
    }

    public void deleteByUser(User user) {
        String userKey = USER_KEY_PREFIX + user.getId();
        String token = redisTemplate.opsForValue().get(userKey);
        if (token != null && !token.isBlank()) {
            redisTemplate.delete(KEY_PREFIX + token);
        }
        redisTemplate.delete(userKey);
    }
}
