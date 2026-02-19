package com.example.finalproject.global.email.factory;

import com.example.finalproject.global.email.EmailType;
import com.example.finalproject.global.email.content.EmailMessage;
import com.example.finalproject.global.email.content.HtmlEmailMessage;
import com.example.finalproject.global.util.TemplateRenderer;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordResetEmailFactory implements EmailContentFactory {
    private final TemplateRenderer renderer;

    @Override
    public EmailType getType() {
        return EmailType.PASSWORD_RESET;
    }

    @Override
    public EmailMessage create(Map<String, Object> params) {

        String subject = "동네 마켓 비밀번호 재설정";

        String body = renderer.render(
                "email/password-reset",
                params
        );

        return new HtmlEmailMessage(subject, body);
    }
}

