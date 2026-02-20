package com.example.finalproject.store.dto.response;

import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.domain.StoreBusinessHour;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class GetStoreDetailForCustomerResponse {

    private Long storeId;
    private String storeName;
    private String businessNumber;
    private String representativeName;
    private String addressLine1;
    private String addressLine2;
    private String phone;
    private String description;
    private String storeImage;
    private List<BusinessHourItem> businessHours;

    @Getter
    @Builder
    public static class BusinessHourItem {
        private Short dayOfWeek;
        private String openTime;
        private String closeTime;
        private Boolean isClosed;
    }

    private static final java.time.format.DateTimeFormatter TIME_FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("HH:mm");

    public static GetStoreDetailForCustomerResponse from(Store store) {
        if (store == null) return null;
        String addressLine1 = store.getAddress() != null ? store.getAddress().getAddressLine1() : null;
        String addressLine2 = store.getAddress() != null ? store.getAddress().getAddressLine2() : null;
        String businessNumber = store.getSubmittedDocumentInfo() != null
                ? store.getSubmittedDocumentInfo().getBusinessNumber() : null;

        List<BusinessHourItem> hours = store.getBusinessHours().stream()
                .sorted(java.util.Comparator.comparing(StoreBusinessHour::getDayOfWeek))
                .map(GetStoreDetailForCustomerResponse::toBusinessHourItem)
                .collect(Collectors.toList());

        return GetStoreDetailForCustomerResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .businessNumber(businessNumber)
                .representativeName(store.getRepresentativeName())
                .addressLine1(addressLine1)
                .addressLine2(addressLine2)
                .phone(store.getPhone())
                .description(store.getDescription())
                .storeImage(store.getStoreImage())
                .businessHours(hours)
                .build();
    }

    private static BusinessHourItem toBusinessHourItem(StoreBusinessHour bh) {
        return BusinessHourItem.builder()
                .dayOfWeek(bh.getDayOfWeek())
                .openTime(bh.getOpenTime() != null ? bh.getOpenTime().format(TIME_FORMATTER) : null)
                .closeTime(bh.getCloseTime() != null ? bh.getCloseTime().format(TIME_FORMATTER) : null)
                .isClosed(bh.getIsClosed())
                .build();
    }
}
