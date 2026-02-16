package com.example.finalproject.review.service;


import com.example.finalproject.global.component.UserLoader;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.order.repository.OrderProductRepository;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.review.domain.Review;
import com.example.finalproject.review.dto.request.PostReviewCreateRequest;
import com.example.finalproject.review.dto.response.GetReviewDetailResponse;
import com.example.finalproject.review.dto.response.GetReviewListResponse;
import com.example.finalproject.review.dto.response.GetReviewPageResponse;
import com.example.finalproject.review.dto.response.GetReviewStatisticsResponse;
import com.example.finalproject.review.enums.ReviewSortType;
import com.example.finalproject.review.repository.ReviewRepository;
import com.example.finalproject.user.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final OrderProductRepository orderProductRepository;
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

    @Transactional(readOnly = true)
    public GetReviewPageResponse getStoreReviews(
            Long storeId,
            ReviewSortType sortType,
            Pageable pageable) {

        Pageable sortedPageable = createSortedPageable(sortType, pageable);

        Page<Review> reviewPage =
                reviewRepository.findByStoreOrder_Store_IdAndIsVisibleTrue(
                        storeId, sortedPageable);

        Map<Long, List<OrderProduct>> productMap =
                loadProductsGroupedByStoreOrder(reviewPage);

        Page<GetReviewListResponse> responsePage =
                reviewPage.map(review -> toReviewListResponse(review, productMap));

        GetReviewStatisticsResponse statistics =
                getSafeStatistics(storeId);

        return GetReviewPageResponse.builder()
                .reviews(responsePage)
                .statistics(statistics)
                .build();
    }

    private Pageable createSortedPageable(
            ReviewSortType sortType,
            Pageable pageable) {

        Sort sort = switch (sortType) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");

            case HIGH_RATING -> Sort.by(Sort.Direction.DESC, "rating")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));

            case LOW_RATING -> Sort.by(Sort.Direction.ASC, "rating")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        };

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Map<Long, List<OrderProduct>> loadProductsGroupedByStoreOrder(Page<Review> reviewPage) {

        List<Long> storeOrderIds = reviewPage.getContent().stream()
                .map(r -> r.getStoreOrder().getId())
                .toList();

        if (storeOrderIds.isEmpty()) {
            return Map.of();
        }

        List<OrderProduct> products = orderProductRepository.findByStoreOrder_IdIn(storeOrderIds);

        return products.stream().collect(Collectors.groupingBy(op -> op.getStoreOrder().getId()));
    }

    private GetReviewListResponse toReviewListResponse(Review review,
                                                       Map<Long, List<OrderProduct>> productMap) {

        List<GetReviewListResponse.ProductSummary> productSummaries =
                productMap.getOrDefault(
                                review.getStoreOrder().getId(), List.of())
                        .stream()
                        .map(op -> GetReviewListResponse.ProductSummary.builder()
                                .productName(op.getProductNameSnapshot())
                                .quantity(op.getQuantity())
                                .build())
                        .toList();

        return GetReviewListResponse.builder()
                .reviewId(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .writerNickname(review.getUser().getName())
                .createdAt(review.getCreatedAt())
                .ownerReply(review.getOwnerReply())
                .ownerReplyAt(review.getOwnerReplyAt())
                .products(productSummaries)
                .build();
    }

    private GetReviewStatisticsResponse getSafeStatistics(Long storeId) {

        GetReviewStatisticsResponse stats = reviewRepository.getReviewStatistics(storeId);

        if (stats == null || stats.getAverage() == null) {
            return new GetReviewStatisticsResponse(0L, 0L, 0L, 0L, 0L, 0.0);
        }

        return stats;
    }

}

