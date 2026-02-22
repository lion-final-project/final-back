package com.example.finalproject.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class PatchReviewUpdateRequest {
    @Min(1)
    @Max(5)
    private Short rating;

    private String content;
}
