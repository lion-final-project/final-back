package com.example.finalproject.moderation.enums;

public enum DocumentType {
    BUSINESS_LICENSE, BUSINESS_REPORT, BANK_PASSBOOK, ID_CARD;

    public static DocumentType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("DocumentType is null");
        }
        String normalized = value.trim().toUpperCase();
        for (DocumentType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown DocumentType: " + value);
    }
}
