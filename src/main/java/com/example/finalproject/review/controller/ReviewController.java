package com.example.finalproject.review.controller;


import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.review.dto.request.PatchReviewUpdateRequest;
import com.example.finalproject.review.dto.request.PostReviewCreateRequest;
import com.example.finalproject.review.dto.request.PostReviewReplyRequest;
import com.example.finalproject.review.dto.response.GetReviewDetailResponse;
import com.example.finalproject.review.dto.response.GetReviewPageResponse;
import com.example.finalproject.review.enums.ReviewSortType;
import com.example.finalproject.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponse<GetReviewPageResponse>> getStoreReviews(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "LATEST") ReviewSortType sortType,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getStoreReviews(storeId, sortType, pageable)));
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> updateReview(
            @AuthenticationPrincipal String email,
            @PathVariable Long reviewId,
            @RequestBody @Valid PatchReviewUpdateRequest request) {
        reviewService.updateReview(email, reviewId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal String email,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(email, reviewId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ApiResponse<Void>> addOwnerReply(
            @AuthenticationPrincipal String email,
            @PathVariable Long reviewId,
            @RequestBody @Valid PostReviewReplyRequest request) {
        reviewService.addOwnerReply(email, reviewId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
