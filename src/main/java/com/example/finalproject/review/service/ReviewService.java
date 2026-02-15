package com.example.finalproject.review.service;


import com.example.finalproject.global.component.UserLoader;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.review.domain.Review;
import com.example.finalproject.review.dto.request.PostReviewCreateRequest;
import com.example.finalproject.review.dto.response.GetReviewDetailResponse;
import com.example.finalproject.review.repository.ReviewRepository;
import com.example.finalproject.user.domain.User;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final UserLoader userLoader;

    @Transactional
    public void createReview(String email, Long storeOrderId, PostReviewCreateRequest request) {

        User user = userLoader.loadUserByUsername(email);

        StoreOrder storeOrder = storeOrderRepository.findById(storeOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_ORDER_NOT_FOUND));

        if (storeOrder.getOrder().getOrderedAt().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED);
        }

        if (!storeOrder.getOrder().getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (storeOrder.getStatus() != StoreOrderStatus.DELIVERED) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED);
        }

        if (reviewRepository.existsByStoreOrder_Id(storeOrderId)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .storeOrder(storeOrder)
                .user(user)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public GetReviewDetailResponse getReviewDetail(Long reviewId) {

        Review review = reviewRepository.findDetailById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getIsVisible()) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }

        return GetReviewDetailResponse.from(review);
    }
}

