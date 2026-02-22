package com.example.finalproject.communication.dto.response;


import com.example.finalproject.communication.domain.Inquiry;
import com.example.finalproject.communication.enums.InquiryCategory;
import com.example.finalproject.communication.enums.InquiryStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetAdminIquiriesResponse {

    private Long inquiryId;
    private InquiryCategory category;
    private String title;
    private String customerName;
    private InquiryStatus status;
    private LocalDateTime createdAt;

    public static GetAdminIquiriesResponse from(Inquiry inquiry) {
        return new GetAdminIquiriesResponse(
                inquiry.getId(),
                inquiry.getCategory(),
                inquiry.getTitle(),
                inquiry.getUser().getName(), // 고객명
                inquiry.getStatus(),
                inquiry.getCreatedAt()
        );
    }
}
