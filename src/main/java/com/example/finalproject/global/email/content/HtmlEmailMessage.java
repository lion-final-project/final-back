package com.example.finalproject.global.email.content;

import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.MimeMessageHelper;

public class HtmlEmailMessage extends EmailMessage {

    private final String htmlBody;

    public HtmlEmailMessage(String subject, String htmlBody) {
        super(subject);
        this.htmlBody = htmlBody;
    }


    @Override
    public void apply(MimeMessageHelper helper) throws MessagingException {
        helper.setText(htmlBody, true);
    }
}
