package com.example.finalproject.admin.store.service;

import com.example.finalproject.admin.store.dto.AdminStoreDetailResponse;
import com.example.finalproject.admin.store.dto.AdminStoreListItemResponse;
import com.example.finalproject.admin.store.dto.AdminStoreListResponse;
import com.example.finalproject.admin.store.dto.AdminStorePageInfo;
import com.example.finalproject.admin.store.dto.AdminStoreStatsResponse;
import com.example.finalproject.admin.store.dto.AdminStoreStatusUpdateRequest;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.repository.ApprovalRepository;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.domain.embedded.SettlementAccount;
import com.example.finalproject.store.domain.embedded.StoreAddress;
import com.example.finalproject.store.domain.embedded.SubmittedDocumentInfo;
import com.example.finalproject.store.enums.StoreActiveStatus;
import com.example.finalproject.store.repository.StoreRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStoreService {
    private final StoreRepository storeRepository;
    private final ApprovalRepository approvalRepository;

    @Transactional(readOnly = true)
    public AdminStoreListResponse getStores(String name, StoreActiveStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Store> result;
        if (name != null && !name.isBlank() && status != null) {
            result = storeRepository.findByStoreNameContainingIgnoreCaseAndIsActive(name, status, pageable);
        } else if (name != null && !name.isBlank()) {
            result = storeRepository.findByStoreNameContainingIgnoreCase(name, pageable);
        } else if (status != null) {
            result = storeRepository.findByIsActive(status, pageable);
        } else {
            result = storeRepository.findAll(pageable);
        }

        List<AdminStoreListItemResponse> content = result.getContent().stream()
                .map(this::toListItem)
                .toList();

        AdminStoreStatsResponse stats = new AdminStoreStatsResponse(
                storeRepository.count(),
                storeRepository.countByIsActive(StoreActiveStatus.ACTIVE),
                storeRepository.countByIsActive(StoreActiveStatus.INACTIVE),
                approvalRepository.countByApplicantTypeAndStatus(ApplicantType.STORE, ApprovalStatus.PENDING)
        );

        AdminStorePageInfo pageInfo = new AdminStorePageInfo(
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );

        return new AdminStoreListResponse(stats, content, pageInfo);
    }

    @Transactional(readOnly = true)
    public AdminStoreDetailResponse getStoreDetail(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        return toDetail(store);
    }

    @Transactional
    public AdminStoreDetailResponse updateStoreStatus(Long storeId, AdminStoreStatusUpdateRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        store.setActiveStatus(toActiveStatus(request.getIsActive()));
        store.setStatusReason(request.getReason());
        return toDetail(store);
    }

    private AdminStoreListItemResponse toListItem(Store store) {
        StoreAddress address = store.getAddress();
        return AdminStoreListItemResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .representativeName(store.getRepresentativeName())
                .ownerName(store.getOwner().getName())
                .ownerPhone(store.getOwner().getPhone())
                .addressLine1(address != null ? address.getAddressLine1() : null)
                .addressLine2(address != null ? address.getAddressLine2() : null)
                .status(store.getStatus() != null ? store.getStatus().name() : null)
                .isActive(toIsActive(store.getIsActive()))
                .createdAt(store.getCreatedAt())
                .build();
    }

    private AdminStoreDetailResponse toDetail(Store store) {
        StoreAddress address = store.getAddress();
        SubmittedDocumentInfo docs = store.getSubmittedDocumentInfo();
        SettlementAccount settlement = store.getSettlementAccount();
        return AdminStoreDetailResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .phone(store.getPhone())
                .description(store.getDescription())
                .representativeName(store.getRepresentativeName())
                .representativePhone(store.getRepresentativePhone())
                .postalCode(address != null ? address.getPostalCode() : null)
                .addressLine1(address != null ? address.getAddressLine1() : null)
                .addressLine2(address != null ? address.getAddressLine2() : null)
                .storeImage(store.getStoreImage())
                .ownerName(store.getOwner().getName())
                .ownerEmail(store.getOwner().getEmail())
                .ownerPhone(store.getOwner().getPhone())
                .businessOwnerName(docs != null ? docs.getBusinessOwnerName() : null)
                .businessNumber(docs != null ? docs.getBusinessNumber() : null)
                .telecomSalesReportNumber(docs != null ? docs.getTelecomSalesReportNumber() : null)
                .settlementBankName(settlement != null ? settlement.getBankName() : null)
                .settlementBankAccount(settlement != null ? settlement.getBankAccount() : null)
                .settlementAccountHolder(settlement != null ? settlement.getAccountHolder() : null)
                .status(store.getStatus() != null ? store.getStatus().name() : null)
                .isActive(toIsActive(store.getIsActive()))
                .statusReason(store.getStatusReason())
                .commissionRate(store.getCommissionRate())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }

    private boolean toIsActive(StoreActiveStatus activeStatus) {
        return activeStatus == StoreActiveStatus.ACTIVE;
    }

    private StoreActiveStatus toActiveStatus(Boolean isActive) {
        return Boolean.TRUE.equals(isActive) ? StoreActiveStatus.ACTIVE : StoreActiveStatus.INACTIVE;
    }
}
