package com.example.finalproject.communication.dto.response;


import com.example.finalproject.communication.domain.Inquiry;
import com.example.finalproject.communication.enums.InquiryCategory;
import com.example.finalproject.communication.enums.InquiryStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetInquiryResponse {
    private Long id;
    private InquiryCategory category;
    private String title;
    private String content;
    private String fileUrl;
    private InquiryStatus status;
    private String answer;
    private LocalDateTime answeredAt;
    private LocalDateTime createdAt;

    public static GetInquiryResponse from(Inquiry inquiry) {
        return GetInquiryResponse.builder()
                .id(inquiry.getId())
                .category(inquiry.getCategory())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .fileUrl(inquiry.getFileUrl())
                .status(inquiry.getStatus())
                .answer(inquiry.getAnswer())
                .answeredAt(inquiry.getAnsweredAt())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}

