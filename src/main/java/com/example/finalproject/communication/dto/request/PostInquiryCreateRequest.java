package com.example.finalproject.communication.dto.request;

import com.example.finalproject.communication.enums.InquiryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class PostInquiryCreateRequest {

    @NotNull(message = "문의 유형은 필수입니다.")
    private InquiryCategory category;

    @NotBlank(message = "문의 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "문의 내용은 필수입니다.")
    private String content;
}
