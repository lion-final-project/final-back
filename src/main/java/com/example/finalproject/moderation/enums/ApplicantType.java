package com.example.finalproject.moderation.enums;

public enum ApplicantType {
    STORE, RIDER;

    public static ApplicantType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("ApplicantType is null");
        }
        String normalized = value.trim().toUpperCase();
        for (ApplicantType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ApplicantType: " + value);
    }
}
