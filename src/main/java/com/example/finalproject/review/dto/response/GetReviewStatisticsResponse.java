package com.example.finalproject.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class GetReviewStatisticsResponse {
    private Long fiveStar;
    private Long fourStar;
    private Long threeStar;
    private Long twoStar;
    private Long oneStar;

    private Double average;
}
