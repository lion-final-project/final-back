package com.example.finalproject.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "password-reset")
public class PasswordResetProperties {

    private String redisPrefix;
    private String rateLimitPrefix;
    private long expiryMinutes;
    private String resetUrl;
    private long requestCooldownSeconds;
}
