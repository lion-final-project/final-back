package com.example.finalproject.store.controller;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.subscription.dto.request.PatchSubscriptionProductRequest;
import com.example.finalproject.subscription.dto.request.PatchSubscriptionProductStatusRequest;
import com.example.finalproject.subscription.dto.request.PostSubscriptionProductRequest;
import com.example.finalproject.subscription.dto.response.PatchSubscriptionProductDeletionResponse;
import com.example.finalproject.subscription.dto.response.GetSubscriptionProductResponse;
import com.example.finalproject.subscription.service.SubscriptionProductService;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import com.example.finalproject.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 마트(사장님) 구독 상품 API.
 * API-SOP-009, API-SOP-010 등 마트 전용 구독 상품 CRUD 엔드포인트.
 */
@RestController
@RequestMapping("/api/store/subscription-products")
@RequiredArgsConstructor
public class StoreSubscriptionProductController {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final SubscriptionProductService subscriptionProductService;

    /**
     * API-SOP-009: 마트 구독 상품 목록 조회.
     * 인증된 마트(사장님)가 등록한 구독 상품 목록을 생성일 역순으로 반환한다.
     * 각 항목에 구독자 수(ACTIVE), 구성 품목이 포함된다.
     * storeId는 JWT/Spring Security 인증 사용자(owner)의 마트 ID로 자동 결정.
     *
     * @return 200 OK, 구독 상품 응답 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GetSubscriptionProductResponse>>> list() {
        Long storeId = resolveStoreId();
        List<GetSubscriptionProductResponse> list = subscriptionProductService.findListByStoreId(storeId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * API-SOP-010: 구독 상품 등록.
     * 마트가 소유한 구독 상품을 새로 등록한다.
     * storeId는 JWT/Spring Security 인증 사용자(owner)의 마트 ID로 자동 결정.
     *
     * @param request 구독 상품 등록 요청 (이름, 가격, 총 배송 횟수, 구성 품목 등)
     * @return 201 Created, 등록된 구독 상품 응답
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GetSubscriptionProductResponse>> create(
            @Valid @RequestBody PostSubscriptionProductRequest request) {
        Long storeId = resolveStoreId();
        GetSubscriptionProductResponse response = subscriptionProductService.create(storeId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * API-SOP-010P: 구독 상품 수정.
     * 마트가 소유한 구독 상품의 이름·설명·가격·배송 횟수·구성 품목을 수정한다.
     * 노출 상태 변경은 PATCH .../status (API-SOP-010S)에서 처리한다.
     * storeId는 JWT/Spring Security 인증 사용자(owner)의 마트 ID로 자동 결정.
     *
     * @param id      구독 상품 ID (path)
     * @param request 수정 요청 (이름, 가격, 총 배송 횟수, 구성 품목 등)
     * @return 200 OK, 수정된 구독 상품 응답
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<GetSubscriptionProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PatchSubscriptionProductRequest request) {
        Long storeId = resolveStoreId();
        GetSubscriptionProductResponse response = subscriptionProductService.update(storeId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * API-SOP-010S: 구독 상품 노출 상태 변경.
     * 마트가 소유한 구독 상품의 노출(ACTIVE)/숨김(INACTIVE) 상태만 변경한다.
     * storeId는 JWT/Spring Security 인증 사용자(owner)의 마트 ID로 자동 결정.
     *
     * @param id      구독 상품 ID (path)
     * @param request 노출 상태 변경 요청 (status: ACTIVE | INACTIVE)
     * @return 200 OK, 변경된 구독 상품 응답
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<GetSubscriptionProductResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody PatchSubscriptionProductStatusRequest request) {
        Long storeId = resolveStoreId();
        GetSubscriptionProductResponse response = subscriptionProductService.updateStatus(storeId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * API-SOP-010D1: 마트 구독 상품 삭제 요청(삭제 예정).
     * UC-S11: 구독 상품 삭제 요청.
     * - 숨김(INACTIVE) 상태에서만 호출 가능.
     * - 구독자가 남아 있으면 삭제 예정(PENDING_DELETE)로 전환한다.
     * - 구독자가 없으면 즉시 삭제한다.
     * storeId는 JWT/Spring Security 인증 사용자(owner)의 마트 ID로 자동 결정.
     *
     * @param id 구독 상품 ID
     * @return 삭제 예정 전환 시 구독 상품 정보(data), 즉시 삭제 시 message만 반환(data=null)
     */
    @PatchMapping("/{id}/deletion")
    public ResponseEntity<ApiResponse<GetSubscriptionProductResponse>> requestDeletion(
            @PathVariable Long id) {
        Long storeId = resolveStoreId();
        PatchSubscriptionProductDeletionResponse result = subscriptionProductService.requestDeletion(storeId, id);
        if (result.getAction() == PatchSubscriptionProductDeletionResponse.Action.DELETED) {
            return ResponseEntity.ok(ApiResponse.success("구독 상품이 삭제되었습니다.", null));
        }
        return ResponseEntity.ok(ApiResponse.success(result.getProduct()));
    }

    /**
     * API-SOP-010D2: 마트 구독 상품 즉시 삭제.
     * 구독자가 0명일 때만 삭제 가능. 삭제 예정 상태에서 구독자가 모두 없어진 후 호출한다.
     * storeId는 JWT/Spring Security 인증 사용자(owner)의 마트 ID로 자동 결정.
     *
     * @param id 구독 상품 ID
     * @return 200 OK, 삭제 완료 메시지
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteImmediately(
            @PathVariable Long id) {
        Long storeId = resolveStoreId();
        subscriptionProductService.deleteImmediately(storeId, id);
        return ResponseEntity.ok(ApiResponse.success("구독 상품이 삭제되었습니다."));
    }

    /**
     * 인증된 사용자(owner)의 마트 ID를 반환한다.
     * JWT/Spring Security 인증 정보에서 사용자 ID를 추출한 뒤, 해당 사용자가 소유한 Store를 조회한다.
     *
     * @return 마트 ID
     * @throws BusinessException 마트를 찾을 수 없을 때 (STORE_NOT_FOUND)
     */
    private Long resolveStoreId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
        Object principal = auth.getPrincipal();
        Long ownerId = null;
        if (principal instanceof CustomUserDetails details) {
            ownerId = details.getUser().getId();
        } else if (principal instanceof String) {
            // JWT 인증 시 subject가 이메일로 설정되어 있음
            User user = userRepository.findByEmail((String) principal)
                    .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
            ownerId = user.getId();
        }
        if (ownerId == null) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
        Store store = storeRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        return store.getId();
    }
}
