package com.example.finalproject.communication.dto.response;

import com.example.finalproject.communication.domain.Inquiry;
import com.example.finalproject.communication.enums.InquiryCategory;
import com.example.finalproject.communication.enums.InquiryStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetAdminInquiryDetailResponse {

    private String customerName;
    private String email;
    private String phone;

    private InquiryCategory category;
    private String title;
    private String content;
    private InquiryStatus status;
    private LocalDateTime createdAt;

    private String fileUrl;

    private String answer;

    public static GetAdminInquiryDetailResponse from(Inquiry inquiry) {
        return new GetAdminInquiryDetailResponse(
                inquiry.getUser().getName(),
                inquiry.getUser().getEmail(),
                inquiry.getUser().getPhone(),

                inquiry.getCategory(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getStatus(),
                inquiry.getCreatedAt(),

                inquiry.getFileUrl(),
                inquiry.getAnswer()
        );
    }
}
