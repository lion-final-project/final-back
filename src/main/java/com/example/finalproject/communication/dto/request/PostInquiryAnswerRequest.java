package com.example.finalproject.communication.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostInquiryAnswerRequest {

    @NotBlank(message = "답변 내용은 필수입니다.")
    private String answer;
}
