package com.example.finalproject.admin.controller;

import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.enums.RiderApprovalStatus;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.repository.ApprovalRepository;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.enums.StoreActiveStatus;
import com.example.finalproject.store.repository.StoreRepository;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminLegacyController {

    private final StoreRepository storeRepository;
    private final RiderRepository riderRepository;
    private final ApprovalRepository approvalRepository;

    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<LegacyPagedResponse<LegacyStoreListItem>>> getStores(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<Store> stores = storeRepository.findAll().stream()
                .sorted(Comparator.comparing(Store::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        String keyword = normalize(name);
        List<Store> filtered = stores.stream()
                .filter(store -> keyword.isEmpty() || normalize(store.getStoreName()).contains(keyword))
                .toList();

        List<LegacyStoreListItem> mapped = filtered.stream()
                .map(this::toStoreListItem)
                .toList();

        LegacyPagedResponse<LegacyStoreListItem> response = buildPagedResponse(
                mapped,
                page,
                size,
                Map.of(
                        "total", mapped.size(),
                        "active", (int) mapped.stream().filter(LegacyStoreListItem::getIsActive).count(),
                        "inactive", (int) mapped.stream().filter(item -> !item.getIsActive()).count(),
                        "pending", (int) filtered.stream().filter(store -> !isStoreApproved(store)).count()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponse<LegacyStoreDetailResponse>> getStoreDetail(@PathVariable Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("store not found"));

        LegacyStoreDetailResponse data = LegacyStoreDetailResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .addressLine1(store.getAddress() != null ? store.getAddress().getAddressLine1() : null)
                .addressLine2(store.getAddress() != null ? store.getAddress().getAddressLine2() : null)
                .representativeName(store.getRepresentativeName())
                .representativePhone(store.getRepresentativePhone())
                .phone(store.getPhone())
                .ownerPhone(store.getOwner() != null ? store.getOwner().getPhone() : null)
                .businessNumber(store.getSubmittedDocumentInfo() != null ? store.getSubmittedDocumentInfo().getBusinessNumber() : null)
                .settlementBankName(store.getSettlementAccount() != null ? store.getSettlementAccount().getBankName() : null)
                .settlementBankAccount(store.getSettlementAccount() != null ? store.getSettlementAccount().getBankAccount() : null)
                .settlementAccountHolder(store.getSettlementAccount() != null ? store.getSettlementAccount().getAccountHolder() : null)
                .description(store.getDescription())
                .isActive(store.getIsActive() == StoreActiveStatus.ACTIVE)
                .build();

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/stores/{storeId}/status")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> updateStoreStatus(
            @PathVariable Long storeId,
            @RequestBody LegacyStatusUpdateRequest request
    ) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("store not found"));

        store.setActiveStatus(Boolean.TRUE.equals(request.getIsActive())
                ? StoreActiveStatus.ACTIVE
                : StoreActiveStatus.INACTIVE);
        storeRepository.save(store);
        return ResponseEntity.ok(ApiResponse.success("updated"));
    }

    @GetMapping("/riders")
    public ResponseEntity<ApiResponse<LegacyPagedResponse<LegacyRiderListItem>>> getRiders(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<Rider> riders = riderRepository.findAll().stream()
                .sorted(Comparator.comparing(Rider::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        String nameKeyword = normalize(name);
        String phoneKeyword = normalize(phone);

        List<Rider> filtered = riders.stream()
                .filter(rider -> rider.getStatus() == RiderApprovalStatus.APPROVED)
                .filter(rider -> nameKeyword.isEmpty() || normalize(rider.getDisplayName()).contains(nameKeyword))
                .filter(rider -> phoneKeyword.isEmpty() || normalize(rider.getDisplayPhone()).contains(phoneKeyword))
                .toList();

        List<LegacyRiderListItem> mapped = filtered.stream()
                .map(this::toRiderListItem)
                .toList();

        LegacyPagedResponse<LegacyRiderListItem> response = buildPagedResponse(
                mapped,
                page,
                size,
                Map.of(
                        "total", mapped.size(),
                        "operating", (int) mapped.stream().filter(LegacyRiderListItem::getIsActive).count(),
                        "unavailable", (int) mapped.stream().filter(item -> !item.getIsActive()).count(),
                        "idCardPending", getPendingRiderApprovalCount()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/riders/{riderId}")
    public ResponseEntity<ApiResponse<LegacyRiderDetailResponse>> getRiderDetail(@PathVariable Long riderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("rider not found"));

        LegacyRiderDetailResponse data = LegacyRiderDetailResponse.builder()
                .riderId(rider.getId())
                .name(rider.getDisplayName())
                .phone(rider.getDisplayPhone())
                .bankName(rider.getBankName())
                .bankAccount(rider.getBankAccount())
                .accountHolder(rider.getAccountHolder())
                .isActive(isRiderActive(rider))
                .build();

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/riders/{riderId}/status")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> updateRiderStatus(
            @PathVariable Long riderId,
            @RequestBody LegacyStatusUpdateRequest request
    ) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("rider not found"));

        rider.setOperationStatus(Boolean.TRUE.equals(request.getIsActive())
                ? RiderOperationStatus.ONLINE
                : RiderOperationStatus.OFFLINE);
        riderRepository.save(rider);
        return ResponseEntity.ok(ApiResponse.success("updated"));
    }

    private boolean isStoreApproved(Store store) {
        return store.getIsActive() == StoreActiveStatus.ACTIVE;
    }

    private boolean isRiderActive(Rider rider) {
        return rider.getOperationStatus() == RiderOperationStatus.ONLINE
                || rider.getOperationStatus() == RiderOperationStatus.DELIVERING;
    }

    private int getPendingRiderApprovalCount() {
        long pendingCount = approvalRepository
                .findByApplicantTypeAndStatusIn(ApplicantType.RIDER, List.of(ApprovalStatus.PENDING))
                .stream()
                .filter(approval -> riderRepository.findByUserId(approval.getUser().getId())
                        .map(rider -> rider.getStatus() != RiderApprovalStatus.APPROVED)
                        .orElse(false))
                .count();
        return Math.toIntExact(pendingCount);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private LegacyStoreListItem toStoreListItem(Store store) {
        return LegacyStoreListItem.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .addressLine1(store.getAddress() != null ? store.getAddress().getAddressLine1() : null)
                .addressLine2(store.getAddress() != null ? store.getAddress().getAddressLine2() : null)
                .representativeName(store.getRepresentativeName())
                .isActive(store.getIsActive() == StoreActiveStatus.ACTIVE)
                .build();
    }

    private LegacyRiderListItem toRiderListItem(Rider rider) {
        boolean active = isRiderActive(rider);
        return LegacyRiderListItem.builder()
                .riderId(rider.getId())
                .name(rider.getDisplayName())
                .phone(rider.getDisplayPhone())
                .bankName(rider.getBankName())
                .bankAccount(rider.getBankAccount())
                .accountHolder(rider.getAccountHolder())
                .isActive(active)
                .idCardStatus(Boolean.TRUE.equals(rider.getIdCardVerified()) ? "DONE" : "PENDING")
                .build();
    }

    private <T> LegacyPagedResponse<T> buildPagedResponse(List<T> fullList, int page, int size, Map<String, Integer> stats) {
        int safeSize = size <= 0 ? 10 : size;
        int safePage = Math.max(page, 0);
        int fromIndex = safePage * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, fullList.size());
        List<T> content = fromIndex >= fullList.size()
                ? new ArrayList<>()
                : new ArrayList<>(fullList.subList(fromIndex, toIndex));

        int totalPages = fullList.isEmpty() ? 0 : (int) Math.ceil((double) fullList.size() / safeSize);
        LegacyPageInfo pageInfo = new LegacyPageInfo(
                safePage,
                safeSize,
                fullList.size(),
                totalPages,
                safePage + 1 < totalPages
        );

        return new LegacyPagedResponse<>(content, stats, pageInfo);
    }

    @Getter
    @AllArgsConstructor
    public static class LegacyPagedResponse<T> {
        private List<T> content;
        private Map<String, Integer> stats;
        private LegacyPageInfo page;
    }

    @Getter
    @AllArgsConstructor
    public static class LegacyPageInfo {
        private int page;
        private int size;
        private int totalElements;
        private int totalPages;
        private boolean hasNext;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LegacyStoreListItem {
        private Long storeId;
        private String storeName;
        private String addressLine1;
        private String addressLine2;
        private String representativeName;
        private Boolean isActive;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LegacyStoreDetailResponse {
        private Long storeId;
        private String storeName;
        private String addressLine1;
        private String addressLine2;
        private String representativeName;
        private String representativePhone;
        private String phone;
        private String ownerPhone;
        private String businessNumber;
        private String settlementBankName;
        private String settlementBankAccount;
        private String settlementAccountHolder;
        private String description;
        private Boolean isActive;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LegacyRiderListItem {
        private Long riderId;
        private String name;
        private String phone;
        private String bankName;
        private String bankAccount;
        private String accountHolder;
        private String idCardStatus;
        private Boolean isActive;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LegacyRiderDetailResponse {
        private Long riderId;
        private String name;
        private String phone;
        private String bankName;
        private String bankAccount;
        private String accountHolder;
        private Boolean isActive;
    }

    @Getter
    @NoArgsConstructor
    public static class LegacyStatusUpdateRequest {
        @NotNull
        private Boolean isActive;
        private String reason;
    }
}

