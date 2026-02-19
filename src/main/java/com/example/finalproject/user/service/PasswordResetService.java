package com.example.finalproject.user.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.user.config.PasswordResetProperties;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.event.PasswordResetRequestedEvent;
import com.example.finalproject.user.repository.UserRepository;
import com.example.finalproject.user.util.PasswordResetRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetRateLimiter rateLimiter;
    private final PasswordResetTokenService tokenService;
    private final PasswordResetProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;


    public void requestReset(String email) {

        rateLimiter.checkLimit(email);

        userRepository.findByEmail(email)
                .ifPresent(this::publishPasswordResetEvent);
    }

    @Transactional
    public void confirmReset(String token, String newPassword) {

        Long userId = tokenService.consumeToken(token);

        if (userId == null) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.changePassword(passwordEncoder.encode(newPassword));

        user.increaseTokenVersion();
    }

    private void publishPasswordResetEvent(User user) {
        String token = tokenService.createToken(user.getId());
        String link = properties.getResetUrl() + "?token=" + token;

        eventPublisher.publishEvent(
                new PasswordResetRequestedEvent(
                        user.getEmail(),
                        link,
                        properties.getExpiryMinutes()
                )
        );
    }
}

