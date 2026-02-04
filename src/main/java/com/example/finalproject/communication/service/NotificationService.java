package com.example.finalproject.communication.service;

import com.example.finalproject.communication.domain.Notification;
import com.example.finalproject.communication.dto.response.NotificationResponse;
import com.example.finalproject.communication.enums.NotificationRefType;
import com.example.finalproject.communication.repository.NotificationRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SseService sseService;

    @Transactional
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = sseService.subscribe(userId);

        int unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        sseService.sendToUser(
                userId,
                "UNREAD_COUNT",
                unreadCount
        );

        return emitter;
    }

    @Transactional
    public void notifyUser(
            Long userId,
            String title,
            String content,
            NotificationRefType refType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .referenceType(refType)
                .build();

        notificationRepository.save(notification);

        int unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        sseService.sendToUser(
                userId,
                "UNREAD_COUNT",
                unreadCount
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository
                .findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void readAll(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }
}

