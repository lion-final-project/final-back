package com.example.finalproject.admin.service.finance;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.payment.domain.Payment;
import com.example.finalproject.payment.enums.PaymentStatus;
import com.example.finalproject.settlement.enums.SettlementStatus;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AdminFinanceCommonSupport {

    private final UserRepository userRepository;

    public User validateAdmin(String adminEmail) {
        User admin = userRepository.findByEmailAndDeletedAtIsNull(adminEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_AUTHORITY_REQUIRED));
        if (!admin.isAdmin()) {
            throw new BusinessException(ErrorCode.ADMIN_AUTHORITY_REQUIRED);
        }
        return admin;
    }

    public DateRange resolveMonthRange(String yearMonth) {
        YearMonth targetMonth = (yearMonth == null || yearMonth.isBlank()) ? YearMonth.now() : parseYearMonth(yearMonth);
        LocalDate start = targetMonth.atDay(1);
        LocalDate end = targetMonth.atEndOfMonth();
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endExclusive = targetMonth.plusMonths(1).atDay(1).atStartOfDay();
        return new DateRange(start, end, startDateTime, endExclusive);
    }

    public YearMonth parseYearMonth(String yearMonth) {
        try {
            return YearMonth.parse(yearMonth);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "yearMonth는 yyyy-MM 형식이어야 합니다.");
        }
    }

    public String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    }

    public String normalizePeriod(String period) {
        if (period == null || period.isBlank()) {
            return "weekly";
        }
        String normalized = period.trim().toLowerCase(Locale.ROOT);
        if ("weekly".equals(normalized) || "monthly".equals(normalized) || "yearly".equals(normalized)) {
            return normalized;
        }
        return "weekly";
    }

    public String extractRegion(Store store) {
        if (store == null || store.getAddress() == null || store.getAddress().getAddressLine1() == null) {
            return "미상";
        }
        String address = store.getAddress().getAddressLine1().trim();
        if (address.isBlank()) {
            return "미상";
        }
        String[] tokens = address.split("\\s+");
        return tokens.length > 0 ? tokens[0] : "미상";
    }

    public String toPaymentStatusLabel(Payment payment) {
        if (payment == null || payment.getPaymentStatus() == null) {
            return "확인 대기";
        }
        return switch (payment.getPaymentStatus()) {
            case APPROVED, PARTIAL_REFUNDED -> "지급 처리중";
            case REFUNDED, CANCELLED -> "환불 완료";
            case REFUND_REQUESTED -> "환불 요청";
            case FAILED -> "결제 실패";
            case READY, PENDING -> "확인 대기";
        };
    }

    public String toSettlementStatusLabel(SettlementStatus status) {
        if (status == null) {
            return "확인 대기";
        }
        return switch (status) {
            case COMPLETED -> "지급 완료";
            case PENDING -> "지급 처리중";
            case FAILED -> "지급 실패";
        };
    }

    public String toStoreIdCode(Long storeId) {
        return "STORE-" + storeId;
    }

    public String toRiderIdCode(Long riderId) {
        return "RIDER-" + riderId;
    }

    public record DateRange(LocalDate startDate, LocalDate endDate,
                            LocalDateTime startDateTime, LocalDateTime endExclusiveDateTime) {
    }

    public record SummaryData(long totalAmount, long totalCommission, long totalRefundAmount,
                              long netRevenue, long paymentCount) {
    }
}
