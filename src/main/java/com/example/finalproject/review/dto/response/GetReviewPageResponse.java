package com.example.finalproject.review.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class GetReviewPageResponse {
    private Page<GetReviewListResponse> reviews;
    private GetReviewStatisticsResponse statistics;
}
