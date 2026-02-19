package com.example.finalproject.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostReviewCreateRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Short rating;

    private String content;
}

