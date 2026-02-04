package com.example.finalproject.content.dto.response;

import com.example.finalproject.content.domain.Faq;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GetFaqDetailResponse {
    private Long faqId;
    private String question;
    private String answer;
    private LocalDateTime createdAt;

    public static GetFaqDetailResponse from(Faq faq) {
        return GetFaqDetailResponse.builder()
                .faqId(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .createdAt(faq.getCreatedAt())
                .build();
    }
}
