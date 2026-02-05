package com.example.finalproject.user.dto.response;

import com.example.finalproject.user.domain.Address;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetAddressResponse {

    private Long addressId;
    private String addressName;
    private String postalCode;
    private String addressLine1;
    private String addressLine2;
    private String contact;
    private Boolean isDefault;
    private Double latitude;
    private Double longitude;

    public static GetAddressResponse from(Address address) {
        Double lat = null;
        Double lng = null;
        if (address.getLocation() != null) {
            lat = address.getLocation().getY();  // 위도
            lng = address.getLocation().getX();  // 경도
        }
        return GetAddressResponse.builder()
                .addressId(address.getId())
                .addressName(address.getAddressName())
                .postalCode(address.getPostalCode())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .contact(address.getContact())
                .isDefault(address.getIsDefault())
                .latitude(lat)
                .longitude(lng)
                .build();
    }
}
