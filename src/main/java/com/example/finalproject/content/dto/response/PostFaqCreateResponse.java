package com.example.finalproject.content.dto.response;

import com.example.finalproject.content.domain.Faq;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostFaqCreateResponse {
    private Long faqId;
    private String question;
    private String status;
    private LocalDateTime createdAt;

    public static PostFaqCreateResponse from(Faq faq) {
        return PostFaqCreateResponse.builder()
                .faqId(faq.getId())
                .question(faq.getQuestion())
                .status(faq.getStatus().name())
                .createdAt(faq.getCreatedAt())
                .build();
    }
}
