package com.example.finalproject.subscription.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.product.domain.Product;
import com.example.finalproject.product.repository.ProductRepository;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.subscription.domain.SubscriptionProduct;
import com.example.finalproject.subscription.domain.SubscriptionProductItem;
import com.example.finalproject.subscription.dto.request.SubscriptionProductRequest;
import com.example.finalproject.subscription.dto.request.SubscriptionProductStatusRequest;
import com.example.finalproject.subscription.dto.response.SubscriptionProductResponse;
import com.example.finalproject.subscription.enums.SubscriptionProductStatus;
import com.example.finalproject.subscription.enums.SubscriptionStatus;
import com.example.finalproject.subscription.repository.SubscriptionProductItemRepository;
import com.example.finalproject.subscription.repository.SubscriptionProductRepository;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionProductService {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final SubscriptionProductRepository subscriptionProductRepository;
    private final SubscriptionProductItemRepository subscriptionProductItemRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 구독 상품을 등록한다(UC-S10).
     * 요청의 구성 품목(productId)은 모두 해당 마트(storeId) 소속 상품이어야 한다.
     *
     * @param storeId 마트 ID
     * @param request 구독 상품 등록 요청 (이름, 가격, 총 배송 횟수, 구성 품목 등)
     * @return 등록된 구독 상품 응답 (API-SOP-010 응답용)
     * @throws BusinessException 마트 없음(STORE_NOT_FOUND), 상품 없음/소속 불일치(PRODUCT_NOT_FOUND)
     */
    @Transactional
    public SubscriptionProductResponse create(Long storeId, SubscriptionProductRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        int deliveryCountOfWeek = request.getTotalDeliveryCount() != null && request.getTotalDeliveryCount() >= 4
                ? Math.max(1, request.getTotalDeliveryCount() / 4)
                : 1;

        SubscriptionProduct product = SubscriptionProduct.builder()
                .store(store)
                .subscriptionProductName(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .totalDeliveryCount(request.getTotalDeliveryCount())
                .deliveryCountOfWeek(deliveryCountOfWeek)
                .subscriptionUrl(null)
                .build();

        SubscriptionProduct saved = subscriptionProductRepository.save(product);

        List<SubscriptionProductItem> items = new ArrayList<>();
        for (SubscriptionProductRequest.SubscriptionProductItemRequest itemReq : request.getItems()) {
            Product p = productRepository.findByIdAndStore_Id(itemReq.getProductId(), storeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
            SubscriptionProductItem item = SubscriptionProductItem.builder()
                    .subscriptionProduct(saved)
                    .product(p)
                    .quantity(itemReq.getQuantity())
                    .build();
            items.add(subscriptionProductItemRepository.save(item));
        }

        return toResponse(saved, 0, items);
    }

    /**
     * 구독 상품을 수정한다 (API-SOP-010P).
     * 해당 구독 상품이 storeId 소유인지 검증한 뒤, 이름·설명·가격·배송 횟수·구성 품목을 갱신한다.
     * 상태(status) 변경은 별도 API(API-SOP-010S)에서만 처리한다.
     *
     * @param storeId              마트 ID
     * @param subscriptionProductId 구독 상품 ID
     * @param request              수정 요청 (이름, 가격, 총 배송 횟수, 구성 품목 등)
     * @return 수정된 구독 상품 응답
     * @throws BusinessException 구독 상품 없음/소속 불일치(SUBSCRIPTION_PRODUCT_NOT_FOUND), 상품 없음/소속 불일치(PRODUCT_NOT_FOUND)
     */
    @Transactional
    public SubscriptionProductResponse update(Long storeId, Long subscriptionProductId,
                                              SubscriptionProductRequest request) {
        SubscriptionProduct product = subscriptionProductRepository.findById(subscriptionProductId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_PRODUCT_NOT_FOUND));
        if (!product.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_PRODUCT_NOT_FOUND);
        }

        int deliveryCountOfWeek = request.getTotalDeliveryCount() != null && request.getTotalDeliveryCount() >= 4
                ? Math.max(1, request.getTotalDeliveryCount() / 4)
                : 1;

        product.updateDetails(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getTotalDeliveryCount(),
                deliveryCountOfWeek
        );
        subscriptionProductRepository.flush();

        subscriptionProductItemRepository.deleteBySubscriptionProduct(product);
        List<SubscriptionProductItem> items = new ArrayList<>();
        for (SubscriptionProductRequest.SubscriptionProductItemRequest itemReq : request.getItems()) {
            Product p = productRepository.findByIdAndStore_Id(itemReq.getProductId(), storeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
            SubscriptionProductItem item = SubscriptionProductItem.builder()
                    .subscriptionProduct(product)
                    .product(p)
                    .quantity(itemReq.getQuantity())
                    .build();
            items.add(subscriptionProductItemRepository.save(item));
        }

        int subscriberCount = (int) subscriptionRepository.countBySubscriptionProductAndStatus(product, SubscriptionStatus.ACTIVE);
        return toResponse(product, subscriberCount, items);
    }

    /**
     * 구독 상품 노출 상태를 변경한다 (API-SOP-010S).
     * 해당 구독 상품이 storeId 소유인지 검증한 뒤, status(ACTIVE/INACTIVE)만 갱신한다.
     *
     * @param storeId              마트 ID
     * @param subscriptionProductId 구독 상품 ID
     * @param request              노출 상태 변경 요청 (status: ACTIVE | INACTIVE)
     * @return 변경된 구독 상품 응답
     * @throws BusinessException 구독 상품 없음/소속 불일치(SUBSCRIPTION_PRODUCT_NOT_FOUND)
     */
    @Transactional
    public SubscriptionProductResponse updateStatus(Long storeId, Long subscriptionProductId,
                                                    SubscriptionProductStatusRequest request) {
        SubscriptionProduct product = subscriptionProductRepository.findById(subscriptionProductId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_PRODUCT_NOT_FOUND));
        if (!product.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_PRODUCT_NOT_FOUND);
        }
        product.updateStatus(request.getStatus());
        subscriptionProductRepository.flush();

        int subscriberCount = (int) subscriptionRepository.countBySubscriptionProductAndStatus(product, SubscriptionStatus.ACTIVE);
        List<SubscriptionProductItem> items = subscriptionProductItemRepository.findBySubscriptionProductOrderById(product);
        return toResponse(product, subscriberCount, items);
    }

    /**
     * 마트가 등록한 구독 상품 목록을 조회한다. (API-SOP-009)
     * 생성일 역순, 각 상품별 구독자 수(ACTIVE) 및 구성 품목 포함.
     *
     * @param storeId 마트 ID
     * @return 구독 상품 응답 목록
     */
    @Transactional(readOnly = true)
    public List<SubscriptionProductResponse> findListByStoreId(Long storeId) {
        if (storeRepository.findById(storeId).isEmpty()) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
        List<SubscriptionProduct> products = subscriptionProductRepository.findByStore_IdOrderByCreatedAtDesc(storeId);
        List<SubscriptionProductResponse> result = new ArrayList<>(products.size());
        for (SubscriptionProduct sp : products) {
            long subscriberCount = subscriptionRepository.countBySubscriptionProductAndStatus(sp, SubscriptionStatus.ACTIVE);
            List<SubscriptionProductItem> items = subscriptionProductItemRepository.findBySubscriptionProductOrderById(sp);
            result.add(toResponse(sp, (int) subscriberCount, items));
        }
        return result;
    }

    /**
     * 고객용: 특정 마트의 구독 상품 목록을 조회한다. (API-STO-005)
     * ACTIVE 상태만 노출할지 여부는 API/컨트롤러에서 필터링할 수 있도록 여기서는 전체 조회 후 호출부에서 필터 가능.
     * 명세상 고객은 마트별 구독 상품 목록 조회이므로, ACTIVE만 반환하는 것이 일반적이다.
     *
     * @param storeId 마트 ID
     * @return 구독 상품 응답 목록 (ACTIVE만 반환)
     */
    @Transactional(readOnly = true)
    public List<SubscriptionProductResponse> findListByStoreIdForCustomer(Long storeId) {
        if (storeRepository.findById(storeId).isEmpty()) {
            return List.of();
        }
        List<SubscriptionProduct> products = subscriptionProductRepository.findByStore_IdOrderByCreatedAtDesc(storeId);
        List<SubscriptionProductResponse> result = new ArrayList<>();
        for (SubscriptionProduct sp : products) {
            if (sp.getStatus() != SubscriptionProductStatus.ACTIVE) {
                continue;
            }
            long subscriberCount = subscriptionRepository.countBySubscriptionProductAndStatus(sp, SubscriptionStatus.ACTIVE);
            List<SubscriptionProductItem> items = subscriptionProductItemRepository.findBySubscriptionProductOrderById(sp);
            result.add(toResponse(sp, (int) subscriberCount, items));
        }
        return result;
    }

    private SubscriptionProductResponse toResponse(SubscriptionProduct sp, int subscriberCount,
                                                   List<SubscriptionProductItem> items) {
        List<SubscriptionProductResponse.SubscriptionProductItemResponse> itemResponses = items == null
                ? List.of()
                : items.stream()
                .map(i -> SubscriptionProductResponse.SubscriptionProductItemResponse.builder()
                        .productId(i.getProduct().getId())
                        .productName(i.getProduct().getProductName())
                        .quantity(i.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return SubscriptionProductResponse.builder()
                .subscriptionProductId(sp.getId())
                .name(sp.getSubscriptionProductName())
                .description(sp.getDescription())
                .price(sp.getPrice())
                .totalDeliveryCount(sp.getTotalDeliveryCount())
                .status(sp.getStatus())
                .subscriberCount(subscriberCount)
                .imageUrl(sp.getSubscriptionUrl())
                .createdAt(sp.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}
