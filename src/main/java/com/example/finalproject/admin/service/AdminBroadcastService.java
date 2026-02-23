package com.example.finalproject.admin.service;

import com.example.finalproject.admin.dto.notification.AdminBroadcastCreateRequest;
import com.example.finalproject.admin.dto.notification.AdminBroadcastCreateResponse;
import com.example.finalproject.admin.dto.notification.AdminBroadcastHistoryItemResponse;
import com.example.finalproject.admin.dto.notification.AdminBroadcastHistoryResponse;
import com.example.finalproject.communication.domain.NotificationBroadcast;
import com.example.finalproject.communication.enums.BroadcastRefType;
import com.example.finalproject.communication.enums.NotificationRefType;
import com.example.finalproject.communication.repository.NotificationBroadcastRepository;
import com.example.finalproject.communication.service.NotificationService;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminBroadcastService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final NotificationBroadcastRepository notificationBroadcastRepository;

    @Transactional
    public AdminBroadcastCreateResponse createBroadcast(String adminEmail, AdminBroadcastCreateRequest request) {
        validateAdmin(adminEmail);

        List<User> recipients = findRecipients(request.getTargetType());
        NotificationRefType refType = toNotificationRefType(request.getTargetType());

        for (User recipient : recipients) {
            notificationService.createNotification(
                    recipient.getId(),
                    request.getTitle(),
                    request.getContent(),
                    refType
            );
        }

        NotificationBroadcast saved = notificationBroadcastRepository.save(
                NotificationBroadcast.builder()
                        .title(request.getTitle())
                        .content(request.getContent())
                        .referenceType(request.getTargetType())
                        .build()
        );

        return AdminBroadcastCreateResponse.builder()
                .broadcastId(saved.getId())
                .targetType(saved.getReferenceType())
                .recipientCount(recipients.size())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminBroadcastHistoryResponse getBroadcastHistory(String adminEmail, Pageable pageable) {
        validateAdmin(adminEmail);

        Page<NotificationBroadcast> page = notificationBroadcastRepository.findAll(pageable);
        List<AdminBroadcastHistoryItemResponse> content = page.getContent().stream()
                .map(item -> AdminBroadcastHistoryItemResponse.builder()
                        .broadcastId(item.getId())
                        .title(item.getTitle())
                        .content(item.getContent())
                        .targetType(item.getReferenceType())
                        .createdAt(item.getCreatedAt())
                        .build())
                .toList();

        return AdminBroadcastHistoryResponse.builder()
                .content(content)
                .page(AdminBroadcastHistoryResponse.PageInfo.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .hasNext(page.hasNext())
                        .build())
                .build();
    }

    private List<User> findRecipients(BroadcastRefType targetType) {
        if (targetType == BroadcastRefType.ALL) {
            return userRepository.findAllByDeletedAtIsNull();
        }
        if (targetType == BroadcastRefType.CUSTOMER) {
            return userRepository.findAllActiveByRoleName("CUSTOMER");
        }
        if (targetType == BroadcastRefType.STORE) {
            return userRepository.findAllActiveByRoleName("STORE");
        }
        if (targetType == BroadcastRefType.RIDER) {
            return userRepository.findAllActiveByRoleName("RIDER");
        }
        return List.of();
    }

    private NotificationRefType toNotificationRefType(BroadcastRefType targetType) {
        if (targetType == BroadcastRefType.CUSTOMER) {
            return NotificationRefType.CUSTOMER;
        }
        if (targetType == BroadcastRefType.STORE) {
            return NotificationRefType.STORE;
        }
        if (targetType == BroadcastRefType.RIDER) {
            return NotificationRefType.RIDER;
        }
        return NotificationRefType.PROMOTION;
    }

    private User validateAdmin(String adminEmail) {
        User admin = userRepository.findByEmailAndDeletedAtIsNull(adminEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_AUTHORITY_REQUIRED));
        if (!admin.isAdmin()) {
            throw new BusinessException(ErrorCode.ADMIN_AUTHORITY_REQUIRED);
        }
        return admin;
    }
}

