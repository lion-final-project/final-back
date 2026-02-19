package com.example.finalproject.global.email.content;

import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.MimeMessageHelper;

public abstract class EmailMessage {

    private final String subject;

    protected EmailMessage(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public abstract void apply(MimeMessageHelper helper) throws MessagingException;
}



