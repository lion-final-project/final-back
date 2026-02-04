package com.example.finalproject.moderation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicantType {
    STORE, RIDER;

    @JsonCreator
    public static ApplicantType from(String value) {
        return ApplicantType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}
