package com.example.finalproject.user.util;


import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.user.config.PasswordResetProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordResetRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final PasswordResetProperties properties;

    public void checkLimit(String email) {

        String key = properties.getRateLimitPrefix() + email.toLowerCase();

        Boolean result = redisTemplate.opsForValue().setIfAbsent(
                key,
                "1",
                Duration.ofSeconds(properties.getRequestCooldownSeconds())
        );

        if (Boolean.FALSE.equals(result)) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_TOO_MANY_REQUESTS);
        }
    }
}
