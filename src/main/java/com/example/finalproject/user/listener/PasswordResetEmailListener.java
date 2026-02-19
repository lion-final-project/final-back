package com.example.finalproject.user.listener;

import com.example.finalproject.global.email.EmailSender;
import com.example.finalproject.global.email.EmailType;
import com.example.finalproject.user.event.PasswordResetRequestedEvent;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordResetEmailListener {

    private final EmailSender emailSender;

    @Async("mailExecutor")
    @EventListener
    public void handle(PasswordResetRequestedEvent event) {

        Map<String, Object> params = Map.of(
                "link", event.getResetLink(),
                "expiry", event.getExpiryMinutes()
        );

        emailSender.send(
                EmailType.PASSWORD_RESET,
                event.getEmail(),
                params
        );
    }
}


