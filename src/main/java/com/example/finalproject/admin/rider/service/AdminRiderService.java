package com.example.finalproject.admin.rider.service;

import com.example.finalproject.admin.rider.dto.AdminRiderDetailResponse;
import com.example.finalproject.admin.rider.dto.AdminRiderListItemResponse;
import com.example.finalproject.admin.rider.dto.AdminRiderListResponse;
import com.example.finalproject.admin.rider.dto.AdminRiderPageInfo;
import com.example.finalproject.admin.rider.dto.AdminRiderStatsResponse;
import com.example.finalproject.admin.rider.dto.AdminRiderStatusUpdateRequest;
import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.enums.RiderApprovalStatus;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.repository.ApprovalRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRiderService {
    private final RiderRepository riderRepository;
    private final ApprovalRepository approvalRepository;

    @Transactional(readOnly = true)
    public AdminRiderListResponse getRiders(String name, String phone, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String searchName = (name != null && !name.isBlank()) ? name : null;
        String searchPhone = (phone != null && !phone.isBlank()) ? phone : null;

        Page<Rider> result;
        if (searchName != null) {
            List<Rider> allRiders = riderRepository.findByStatus(RiderApprovalStatus.APPROVED, Sort.by(Sort.Direction.DESC, "createdAt"));
            String normalizedName = searchName.toLowerCase();
            List<Rider> filtered = allRiders.stream()
                    .filter(rider -> {
                        String riderName = rider.getUser().getName();
                        if (riderName == null) return false;
                        boolean nameMatches = riderName.toLowerCase().contains(normalizedName);
                        boolean phoneMatches = true;
                        if (searchPhone != null && !searchPhone.isBlank()) {
                            String riderPhone = rider.getUser().getPhone();
                            phoneMatches = riderPhone != null && riderPhone.contains(searchPhone);
                        }
                        return nameMatches && phoneMatches;
                    })
                    .toList();

            int start = Math.min((int) pageable.getOffset(), filtered.size());
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            List<Rider> pageContent = filtered.subList(start, end);
            result = new PageImpl<>(pageContent, pageable, filtered.size());
        } else if (searchPhone != null) {
            result = riderRepository.findByStatusAndUser_PhoneContaining(RiderApprovalStatus.APPROVED, searchPhone, pageable);
        } else {
            result = riderRepository.findByStatus(RiderApprovalStatus.APPROVED, pageable);
        }

        List<AdminRiderListItemResponse> content = result.getContent().stream()
                .map(this::toListItem)
                .toList();

        long total = riderRepository.countByStatus(RiderApprovalStatus.APPROVED);
        long operating = riderRepository.countByStatusAndOperationStatus(RiderApprovalStatus.APPROVED, RiderOperationStatus.ONLINE)
                + riderRepository.countByStatusAndOperationStatus(RiderApprovalStatus.APPROVED, RiderOperationStatus.DELIVERING);
        long unavailable = riderRepository.countByStatusAndOperationStatus(RiderApprovalStatus.APPROVED, RiderOperationStatus.OFFLINE);
        long pendingCount = approvalRepository.countByApplicantTypeAndStatus(ApplicantType.RIDER, ApprovalStatus.PENDING);

        AdminRiderStatsResponse stats = new AdminRiderStatsResponse(
                total,
                operating,
                unavailable,
                pendingCount
        );

        AdminRiderPageInfo pageInfo = new AdminRiderPageInfo(
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );

        return new AdminRiderListResponse(stats, content, pageInfo);
    }

    @Transactional(readOnly = true)
    public AdminRiderDetailResponse getRiderDetail(Long riderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RIDER_NOT_FOUND));
        return toDetail(rider);
    }

    @Transactional
    public AdminRiderDetailResponse updateRiderStatus(Long riderId, AdminRiderStatusUpdateRequest request) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RIDER_NOT_FOUND));
        rider.setOperationStatus(toOperationStatus(request.getIsActive()));
        rider.setStatusReason(request.getReason());
        return toDetail(rider);
    }

    private AdminRiderListItemResponse toListItem(Rider rider) {
        return AdminRiderListItemResponse.builder()
                .riderId(rider.getId())
                .name(rider.getUser().getName())
                .phone(rider.getUser().getPhone())
                .bankName(rider.getBankName())
                .bankAccount(rider.getBankAccount())
                .accountHolder(rider.getAccountHolder())
                .operationStatus(rider.getOperationStatus().name())
                .isActive(isActive(rider.getOperationStatus()))
                .createdAt(rider.getCreatedAt())
                .build();
    }

    private AdminRiderDetailResponse toDetail(Rider rider) {
        return AdminRiderDetailResponse.builder()
                .riderId(rider.getId())
                .name(rider.getUser().getName())
                .phone(rider.getUser().getPhone())
                .bankName(rider.getBankName())
                .bankAccount(rider.getBankAccount())
                .accountHolder(rider.getAccountHolder())
                .operationStatus(rider.getOperationStatus().name())
                .isActive(isActive(rider.getOperationStatus()))
                .statusReason(rider.getStatusReason())
                .createdAt(rider.getCreatedAt())
                .updatedAt(rider.getUpdatedAt())
                .build();
    }

    private boolean isActive(RiderOperationStatus status) {
        return status == RiderOperationStatus.ONLINE || status == RiderOperationStatus.DELIVERING;
    }

    private RiderOperationStatus toOperationStatus(Boolean isActive) {
        return Boolean.TRUE.equals(isActive) ? RiderOperationStatus.ONLINE : RiderOperationStatus.OFFLINE;
    }
}
