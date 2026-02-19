package com.example.finalproject.global.email;

import com.example.finalproject.global.email.content.EmailMessage;
import com.example.finalproject.global.email.factory.EmailContentFactory;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailSender {

    private final JavaMailSender mailSender;
    private final Map<EmailType, EmailContentFactory> factoryMap;

    public EmailSender(JavaMailSender mailSender, List<EmailContentFactory> factories) {
        this.mailSender = mailSender;
        this.factoryMap = factories.stream()
                .collect(Collectors.toMap(
                        EmailContentFactory::getType,
                        f -> f
                ));
    }

    @Retryable(
            value = {MailException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void send(EmailType type, String to, Map<String, Object> params) {

        EmailContentFactory factory = factoryMap.get(type);

        if (factory == null) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_EMAIL_TYPE);
        }

        EmailMessage message = factory.create(params);

        try {
            MimeMessage mimeMessage = getMimeMessage(to, message);
            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new MailSendException("메일 생성 실패", e);
        }
    }

    @NotNull
    private MimeMessage getMimeMessage(String to, EmailMessage message) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(message.getSubject());

        message.apply(helper);
        return mimeMessage;
    }

    @Recover
    public void recover(
            MailException e,
            EmailType type,
            String to,
            Map<String, Object> params) {
        log.error(
                "메일 발송 최종 실패 | type={} | to={} | error={}",
                type,
                to,
                e.getMessage()
        );
    }
}

