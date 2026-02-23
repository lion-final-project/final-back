package com.example.finalproject.admin.service;

import com.example.finalproject.admin.dto.user.AdminUserDetailResponse;
import com.example.finalproject.admin.dto.user.AdminUserListItemResponse;
import com.example.finalproject.admin.dto.user.AdminUserListResponse;
import com.example.finalproject.admin.dto.user.AdminUserStatusUpdateRequest;
import com.example.finalproject.communication.repository.InquiryRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.repository.OrderRepository;
import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.domain.UserStatusHistory;
import com.example.finalproject.user.enums.UserStatus;
import com.example.finalproject.user.repository.AddressRepository;
import com.example.finalproject.user.repository.UserRepository;
import com.example.finalproject.user.repository.UserStatusHistoryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final InquiryRepository inquiryRepository;
    private final UserStatusHistoryRepository userStatusHistoryRepository;

    @Transactional(readOnly = true)
    public AdminUserListResponse getUsers(String adminEmail, String keyword, UserStatus status, Pageable pageable) {
        validateAdmin(adminEmail);

        Page<User> userPage = userRepository.searchUsersForAdmin(keyword, status, pageable);

        List<AdminUserListItemResponse> content = userPage.getContent().stream()
                .map(user -> AdminUserListItemResponse.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .status(user.getStatus())
                        .isActive(user.getStatus() == UserStatus.ACTIVE)
                        .addressCount(Math.toIntExact(addressRepository.countByUserId(user.getId())))
                        .orderCount(orderRepository.countByUserId(user.getId()))
                        .joinedAt(user.getCreatedAt())
                        .build())
                .toList();

        LocalDate now = LocalDate.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        AdminUserListResponse.Stats stats = AdminUserListResponse.Stats.builder()
                .total(userRepository.countByDeletedAtIsNull())
                .active(userRepository.countByDeletedAtIsNullAndStatus(UserStatus.ACTIVE))
                .suspended(userRepository.countByDeletedAtIsNullAndStatus(UserStatus.SUSPENDED))
                .newThisMonth(userRepository.countByDeletedAtIsNullAndCreatedAtBetween(monthStart, nextMonthStart))
                .build();

        AdminUserListResponse.PageInfo pageInfo = AdminUserListResponse.PageInfo.builder()
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .hasNext(userPage.hasNext())
                .build();

        return AdminUserListResponse.builder()
                .content(content)
                .stats(stats)
                .page(pageInfo)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(String adminEmail, Long userId) {
        validateAdmin(adminEmail);

        User user = findActiveUserById(userId);

        List<String> addresses = addressRepository.findByUserOrderByIsDefaultDesc(user).stream()
                .map(address -> formatAddress(address.getAddressLine1(), address.getAddressLine2()))
                .toList();

        List<AdminUserDetailResponse.InquirySummary> inquiryHistory = inquiryRepository
                .findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(inquiry -> AdminUserDetailResponse.InquirySummary.builder()
                        .inquiryId(inquiry.getId())
                        .category(inquiry.getCategory().name())
                        .title(inquiry.getTitle())
                        .status(inquiry.getStatus().name())
                        .createdAt(inquiry.getCreatedAt())
                        .build())
                .toList();

        List<AdminUserDetailResponse.StatusHistory> statusHistory = userStatusHistoryRepository
                .findTop10ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(history -> AdminUserDetailResponse.StatusHistory.builder()
                        .historyId(history.getId())
                        .beforeStatus(history.getBeforeStatus())
                        .afterStatus(history.getAfterStatus())
                        .reason(history.getReason())
                        .changedByEmail(history.getChangedBy().getEmail())
                        .changedAt(history.getCreatedAt())
                        .build())
                .toList();

        return AdminUserDetailResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .isActive(user.getStatus() == UserStatus.ACTIVE)
                .orderCount(orderRepository.countByUserId(user.getId()))
                .inquiryCount(inquiryRepository.countByUserId(user.getId()))
                .joinedAt(user.getCreatedAt())
                .addresses(addresses)
                .inquiryHistory(inquiryHistory)
                .statusHistory(statusHistory)
                .build();
    }

    @Transactional
    public AdminUserDetailResponse updateUserStatus(
            String adminEmail,
            Long userId,
            AdminUserStatusUpdateRequest request
    ) {
        User admin = validateAdmin(adminEmail);
        User target = findActiveUserById(userId);

        if (admin.getId().equals(target.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "관리자 본인 계정 상태는 변경할 수 없습니다.");
        }
        if (target.isAdmin()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "관리자 계정 상태는 변경할 수 없습니다.");
        }
        if (request.getStatus() == UserStatus.SUSPENDED && isBlank(request.getReason())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "계정 정지 시 사유 입력이 필요합니다.");
        }

        UserStatus beforeStatus = target.getStatus();
        UserStatus afterStatus = request.getStatus();

        if (beforeStatus == afterStatus) {
            return getUserDetail(adminEmail, userId);
        }

        applyStatus(target, afterStatus);

        userStatusHistoryRepository.save(UserStatusHistory.builder()
                .user(target)
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .reason(request.getReason())
                .changedBy(admin)
                .build());

        return getUserDetail(adminEmail, userId);
    }

    private User validateAdmin(String adminEmail) {
        User admin = userRepository.findByEmailAndDeletedAtIsNull(adminEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_AUTHORITY_REQUIRED));
        if (!admin.isAdmin()) {
            throw new BusinessException(ErrorCode.ADMIN_AUTHORITY_REQUIRED);
        }
        return admin;
    }

    private User findActiveUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void applyStatus(User user, UserStatus status) {
        if (status == UserStatus.ACTIVE) {
            user.activate();
            return;
        }
        if (status == UserStatus.SUSPENDED) {
            user.suspend();
            return;
        }
        user.inactivate();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String formatAddress(String line1, String line2) {
        if (isBlank(line2)) {
            return line1;
        }
        return line1 + " " + line2;
    }
}
