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
import com.example.finalproject.review.dto.request.PatchReviewUpdateRequest;
import com.example.finalproject.review.dto.request.PostReviewCreateRequest;
import com.example.finalproject.review.dto.request.PostReviewReplyRequest;
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
import java.util.Optional;
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

        validateStoreOrderOwner(storeOrder, user);
        validateDelivered(storeOrder);
        validateWithinSevenDays(storeOrder.getOrder().getOrderedAt());

        Review existing = reviewRepository
                .findByStoreOrder_Id(storeOrderId)
                .orElse(null);

        if (existing != null) {
            handleExistingReview(existing, request);
            return;
        }

        createNewReview(storeOrder, user, request);
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

        Page<Review> reviewPage = reviewRepository.findByStoreOrder_Store_IdAndIsVisibleTrue(storeId, sortedPageable);

        // n+1문제?
        Map<Long, List<OrderProduct>> productMap = loadProductsGroupedByStoreOrder(reviewPage);

        Page<GetReviewListResponse> responsePage = reviewPage.map(review -> toReviewListResponse(review, productMap));

        GetReviewStatisticsResponse statistics = getSafeStatistics(storeId);

        return GetReviewPageResponse.builder()
                .reviews(responsePage)
                .statistics(statistics)
                .build();
    }

    @Transactional
    public void updateReview(
            String email,
            Long reviewId,
            PatchReviewUpdateRequest request) {

        User user = userLoader.loadUserByUsername(email);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        validateReviewWriter(user, review);
        validateWithinSevenDays(review.getStoreOrder().getOrder().getOrderedAt());

        review.update(request.getContent(), request.getRating());
    }

    @Transactional
    public void deleteReview(
            String email,
            Long reviewId) {
        User user = userLoader.loadUserByUsername(email);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        validateReviewWriter(user, review);
        validateWithinSevenDays(review.getStoreOrder().getOrder().getOrderedAt());

        review.delete();
    }

    @Transactional
    public void addOwnerReply(
            String email,
            Long reviewId,
            PostReviewReplyRequest request) {

        User owner = userLoader.loadUserByUsername(email);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        validateReviewVisible(review);
        validateStoreOwner(review, owner);
        validateReplyNotExists(review);

        review.addOwnerReply(request.getContent());
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
        return Optional.ofNullable(reviewRepository.getReviewStatistics(storeId))
                .filter(stats -> stats.getAverage() != null)
                .orElse(new GetReviewStatisticsResponse(0L, 0L, 0L, 0L, 0L, 0.0));
    }

    private void validateReviewWriter(User user, Review review) {

        if (!review.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }


    private void validateStoreOrderOwner(StoreOrder storeOrder, User user) {
        if (!storeOrder.getOrder().getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }


    private void validateDelivered(StoreOrder storeOrder) {
        if (storeOrder.getStatus() != StoreOrderStatus.DELIVERED) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED);
        }
    }

    private void validateReviewVisible(Review review) {
        if (!review.getIsVisible()) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }
    }

    private void validateStoreOwner(Review review, User owner) {

        Long storeOwnerId = review.getStoreOrder().getStore().getOwner().getId();

        if (!storeOwnerId.equals(owner.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateReplyNotExists(Review review) {

        if (review.getOwnerReply() != null) {
            throw new BusinessException(ErrorCode.REVIEW_REPLY_ALREADY_EXISTS);
        }
    }

    private void validateWithinSevenDays(LocalDateTime orderedAt) {
        LocalDateTime deadline = orderedAt.plusDays(7);

        if (LocalDateTime.now().isAfter(deadline)) {
            throw new BusinessException(ErrorCode.REVIEW_MODIFICATION_PERIOD_EXPIRED);
        }
    }

    private void handleExistingReview(Review existing, PostReviewCreateRequest request) {
        if (existing.getIsVisible()) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        existing.restore(request.getContent(), request.getRating());
    }

    private void createNewReview(StoreOrder storeOrder, User user, PostReviewCreateRequest request) {
        Review review = Review.builder()
                .storeOrder(storeOrder)
                .user(user)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        reviewRepository.save(review);
    }
}

