package com.example.finalproject.payment.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class BillingKeyEncryptConverter
        implements AttributeConverter<String, String> {

    private static BillingKeyCryptoUtil crypto;

    @Autowired
    public void setCrypto(BillingKeyCryptoUtil util) {
        crypto = util;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return crypto.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return crypto.decrypt(dbData);
    }
}

