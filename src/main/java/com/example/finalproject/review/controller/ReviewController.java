package com.example.finalproject.review.controller;


import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.review.dto.request.PostReviewCreateRequest;
import com.example.finalproject.review.dto.response.GetReviewDetailResponse;
import com.example.finalproject.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{storeOrderId}")
    public ResponseEntity<ApiResponse<Void>> createReview(
            @AuthenticationPrincipal String email,
            @PathVariable Long storeOrderId,
            @Valid @RequestBody PostReviewCreateRequest request) {

        reviewService.createReview(email, storeOrderId, request);

        return ResponseEntity.ok(ApiResponse.success("리뷰 작성이 성공적으로 완료되었습니다."));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<GetReviewDetailResponse>> getReviewDetail(
            @PathVariable Long reviewId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewDetail(reviewId)));
    }
}
