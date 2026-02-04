package com.example.finalproject.moderation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentType {
    BUSINESS_LICENSE, BUSINESS_REPORT, BANK_PASSBOOK, ID_CARD;

    @JsonCreator
    public static DocumentType from(String value) {
        return DocumentType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}
