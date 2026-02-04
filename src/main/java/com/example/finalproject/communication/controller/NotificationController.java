package com.example.finalproject.communication.controller;

import com.example.finalproject.communication.dto.response.NotificationResponse;
import com.example.finalproject.communication.service.NotificationService;
import com.example.finalproject.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            //@AuthenticationPrincipal Authentication authentication
    ) {
        Long userId = 1L;
        return notificationService.subscribe(userId);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> myUnreadNotifications(
            //@AuthenticationPrincipal Authentication authentication
    ) {
        Long userId = 1L;

        List<NotificationResponse> unreadNotifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(unreadNotifications));
    }

    @PatchMapping("/read")
    public ResponseEntity<ApiResponse<Void>> readAll(
            //@AuthenticationPrincipal Authentication authentication
    ) {
        Long userId = 1L;
        notificationService.readAll(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
