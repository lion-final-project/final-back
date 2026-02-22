package com.example.finalproject.review.dto.response;

import com.example.finalproject.review.domain.Review;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetReviewDetailResponse {

    private Long reviewId;
    private Short rating;
    private String content;
    private String writerNickname;
    private LocalDateTime createdAt;

    public static GetReviewDetailResponse from(Review review) {
        return GetReviewDetailResponse.builder()
                .reviewId(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .writerNickname(review.getUser().getName())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
