package com.example.finalproject.store.dto.request;

import com.example.finalproject.store.enums.StoreCategoryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostStoreRegistrationRequest {

    @NotNull(message = "카테고리 선택은 필수입니다.")
    private StoreCategoryType storeCategory;

    @NotBlank(message = "사업자명은 필수입니다.")
    @Size(max = 50, message = "사업자명은 50자를 초과할 수 없습니다.")
    private String storeOwnerName;

    @NotBlank(message = "상호명은 필수입니다.")
    @Size(max = 100, message = "상호명은 100자를 초과할 수 없습니다.")
    private String storeName;

    @NotBlank(message = "매장 주소는 필수입니다.")
    @Size(max = 255, message = "매장 주소는 255자를 초과할 수 없습니다.")
    private String addressLine;

    @NotNull(message = "위치 정보는 필수입니다.")
    private Double latitude;

    @NotNull(message = "위치 정보는 필수입니다.")
    private Double longitude;

    @NotBlank(message = "대표자명은 필수입니다.")
    @Size(max = 50, message = "대표자명은 50자를 초과할 수 없습니다.")
    private String representativeName;

    @NotBlank(message = "대표자 연락처는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    private String representativePhone;

    @NotBlank(message = "마트 연락처는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    private String storePhone;

    @Size(max = 1000, message = "마트 소개는 1000자를 초과할 수 없습니다.")
    private String storeDescription;

    @NotBlank(message = "마트 대표 사진은 필수입니다.")
    @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다.")
    private String storeImageUrl;

    @NotBlank(message = "사업자등록번호는 필수입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)")
    private String businessNumber;

    @NotBlank(message = "사업자등록증 이미지는 필수입니다.")
    @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다.")
    private String businessLicenseUrl;

    @NotBlank(message = "통신판매업 신고번호는 필수입니다.")
    @Size(max = 50, message = "통신판매업 신고번호는 50자를 초과할 수 없습니다.")
    private String telecomSalesReportNumber;

    @NotBlank(message = "통신판매업 신고증 이미지는 필수입니다.")
    @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다.")
    private String telecomSalesReportUrl;

    @NotBlank(message = "은행명은 필수입니다.")
    @Size(max = 50, message = "은행명은 50자를 초과할 수 없습니다.")
    private String settlementBankName;

    @NotBlank(message = "계좌번호는 필수입니다.")
    @Size(max = 255, message = "계좌번호는 255자를 초과할 수 없습니다.")
    private String settlementBankAccount;

    @NotBlank(message = "예금주는 필수입니다.")
    @Size(max = 50, message = "예금주는 50자를 초과할 수 없습니다.")
    private String settlementAccountHolder;

    @NotBlank(message = "통장 사본 이미지는 필수입니다.")
    @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다.")
    private String bankPassbookUrl;

    private List<String> regularHolidays;

    @NotNull(message = "운영 시간 정보는 필수입니다.")
    @Size(min = 7, max = 7, message = "운영 시간은 7일치(일~토) 모두 입력해야 합니다.")
    @Valid
    private List<PostStoreBusinessHourRequest> businessHours;
}
