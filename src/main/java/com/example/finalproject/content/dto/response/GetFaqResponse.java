package com.example.finalproject.content.dto.response;

import com.example.finalproject.content.domain.Faq;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GetFaqResponse {
    private Long faqId;
    private String question;
    private LocalDateTime createdAt;

    public static GetFaqResponse from(Faq faq) {
        return GetFaqResponse.builder()
                .faqId(faq.getId())
                .question(faq.getQuestion())
                .createdAt(faq.getCreatedAt())
                .build();
    }
}
