package com.example.finalproject.communication.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InquiryAnsweredEvent {
    private final Long inquiryId;
    private final Long userId;
}
