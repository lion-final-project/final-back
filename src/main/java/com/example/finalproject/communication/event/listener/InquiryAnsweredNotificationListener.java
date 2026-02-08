package com.example.finalproject.communication.event.listener;

import com.example.finalproject.communication.domain.Inquiry;
import com.example.finalproject.communication.enums.NotificationRefType;
import com.example.finalproject.communication.event.InquiryAnsweredEvent;
import com.example.finalproject.communication.repository.InquiryRepository;
import com.example.finalproject.communication.service.NotificationService;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
class InquiryAnsweredNotificationListener {

    private final InquiryRepository inquiryRepository;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(InquiryAnsweredEvent event) {
        try {
            Inquiry inquiry = inquiryRepository.findById(event.getInquiryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));

            notificationService.createNotification(
                    event.getUserId(),
                    "문의 답변 등록",
                    "문의에 대한 답변이 등록되었습니다: " + inquiry.getTitle(),
                    NotificationRefType.CUSTOMER
            );

        } catch (Exception e) {
            log.error("[이벤트] 문의 답변 알림 생성 실패: inquiryId={}, userId={}, error={}",
                    event.getInquiryId(), event.getUserId(), e.getMessage(), e);
        }
    }
}
