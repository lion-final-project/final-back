package com.example.finalproject.global.email.factory;

import com.example.finalproject.global.email.content.EmailMessage;
import com.example.finalproject.global.email.EmailType;
import java.util.Map;

public interface EmailContentFactory {

    EmailType getType();

    EmailMessage create(Map<String, Object> params);
}

