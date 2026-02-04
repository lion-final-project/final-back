package com.example.finalproject.content.dto.response;

import com.example.finalproject.content.domain.Faq;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PatchFaqUpdateResponse {
    private Long faqId;
    private String question;
    private LocalDateTime updatedAt;

    public static PatchFaqUpdateResponse from(Faq faq) {
        return PatchFaqUpdateResponse.builder()
                .faqId(faq.getId())
                .question(faq.getQuestion())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}
