package com.example.finalproject.user.service;

import com.example.finalproject.user.config.PasswordResetProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private final StringRedisTemplate redisTemplate;
    private final PasswordResetProperties properties;

    public String createToken(Long userId) {

        String rawToken = UUID.randomUUID().toString();
        String key = buildKey(rawToken);

        redisTemplate.opsForValue().set(
                key,
                userId.toString(),
                Duration.ofMinutes(properties.getExpiryMinutes())
        );

        return rawToken;
    }

    public Long consumeToken(String rawToken) {

        String key = buildKey(rawToken);

        String value = redisTemplate.opsForValue().getAndDelete(key);

        if (value == null) {
            return null;
        }

        return Long.valueOf(value);
    }

    private String buildKey(String rawToken) {
        return properties.getRedisPrefix() + sha256(rawToken);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 처리 실패");
        }
    }
}

