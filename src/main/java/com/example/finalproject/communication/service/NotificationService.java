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
    public SseEmitter subscribe(String email) {
        User user = getUser(email);

        SseEmitter emitter = sseService.subscribe(user.getId());

        int unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());

        sseService.sendToUser(
                user.getId(),
                "UNREAD_COUNT",
                unreadCount
        );

        return emitter;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
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
    public List<NotificationResponse> getUnreadNotifications(String email) {
        User user = getUser(email);

        return notificationRepository
                .findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void readAll(String email) {
        User user = getUser(email);

        notificationRepository.markAllReadByUserEmail(email);

        int unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        sseService.sendToUser(user.getId(), "UNREAD_COUNT", unreadCount);
    }

    @Transactional
    public void readNotification(String email, Long notificationId) {
        User user = getUser(email);

        if (!notificationRepository.existsByIdAndUserId(notificationId, user.getId())) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notificationRepository.markAsReadByNotificationIdAndUserId(notificationId, user.getId());

        int unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        sseService.sendToUser(user.getId(), "UNREAD_COUNT", unreadCount);
    }
}

