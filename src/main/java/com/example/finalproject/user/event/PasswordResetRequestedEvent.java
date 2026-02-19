package com.example.finalproject.user.event;

import lombok.Getter;

@Getter
public class PasswordResetRequestedEvent {

    private final String email;
    private final String resetLink;
    private final long expiryMinutes;

    public PasswordResetRequestedEvent(String email, String resetLink, long expiryMinutes) {
        this.email = email;
        this.resetLink = resetLink;
        this.expiryMinutes = expiryMinutes;
    }
}
