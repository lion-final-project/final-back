package com.example.finalproject.review.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetReviewListResponse {
    private Long reviewId;
    private Short rating;
    private String content;
    private String writerNickname;
    private LocalDateTime createdAt;

    private String ownerReply;
    private LocalDateTime ownerReplyAt;

    private List<ProductSummary> products;

    @Getter
    @Builder
    public static class ProductSummary {
        private String productName;
        private Integer quantity;
    }
}
