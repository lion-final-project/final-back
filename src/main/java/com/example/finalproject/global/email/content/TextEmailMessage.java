package com.example.finalproject.global.email.content;

import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.MimeMessageHelper;

public class TextEmailMessage extends EmailMessage {

    private final String textBody;

    public TextEmailMessage(String subject, String textBody) {
        super(subject);
        this.textBody = textBody;
    }


    @Override
    public void apply(MimeMessageHelper helper) throws MessagingException {
        helper.setText(textBody, false);
    }
}


