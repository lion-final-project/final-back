package com.example.finalproject.communication.dto.response;


import com.example.finalproject.communication.domain.Inquiry;
import com.example.finalproject.communication.enums.InquiryCategory;
import com.example.finalproject.communication.enums.InquiryStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetInquiriesResponse {
    private Long id;
    private InquiryCategory category;
    private String title;
    private InquiryStatus status;
    private LocalDateTime createdAt;
    private String answer;
    private LocalDateTime answerAt;

    public static GetInquiriesResponse from(Inquiry inquiry) {
        return GetInquiriesResponse.builder()
                .id(inquiry.getId())
                .category(inquiry.getCategory())
                .title(inquiry.getTitle())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .answer(inquiry.getAnswer())
                .answerAt(inquiry.getAnsweredAt())
                .build();
    }
}
