package com.example.finalproject.admin.dto.user;

import com.example.finalproject.user.enums.UserStatus;
import com.example.finalproject.store.enums.StoreActiveStatus;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserDetailResponse {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private UserStatus status;
    private Boolean isActive;
    private Long orderCount;
    private Long inquiryCount;
    private LocalDateTime joinedAt;
    private List<String> addresses;
    private List<InquirySummary> inquiryHistory;
    private List<StatusHistory> statusHistory;
    private StoreInfo ownedStore;
    private RiderInfo riderProfile;

    @Getter
    @Builder
    public static class InquirySummary {
        private Long inquiryId;
        private String category;
        private String title;
        private String status;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class StatusHistory {
        private Long historyId;
        private UserStatus beforeStatus;
        private UserStatus afterStatus;
        private String reason;
        private String changedByEmail;
        private LocalDateTime changedAt;
    }

    @Getter
    @Builder
    public static class StoreInfo {
        private Long storeId;
        private String storeName;
        private String representativePhone;
        private StoreActiveStatus activeStatus;
    }

    @Getter
    @Builder
    public static class RiderInfo {
        private Long riderId;
        private String riderName;
        private String riderPhone;
        private RiderOperationStatus operationStatus;
    }
}
