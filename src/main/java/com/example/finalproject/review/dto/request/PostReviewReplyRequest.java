package com.example.finalproject.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PostReviewReplyRequest {
    @NotBlank
    private String content;
}
