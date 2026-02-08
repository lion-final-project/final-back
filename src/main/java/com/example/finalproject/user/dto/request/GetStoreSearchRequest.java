package com.example.finalproject.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.bind.annotation.BindParam;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetStoreSearchRequest {

    @NotNull(message = "위도는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90도 이하이어야 합니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180도 이하이어야 합니다.")
    private Double longitude;


    @BindParam("store-category-id")
    private Long storeCategoryId;

    @Size(min = 2 ,max = 20, message = "검색어는 2자 이상 20자 이내여야합니다")
    private String keyword;

    @BindParam("last-distance")
    private Double lastDistance;

    @BindParam("last-id")
    private Long lastId;

    @Positive(message = "사이즈는 1 이상이어야 합니다.")
    @Max(value = 100, message = "한 번에 최대 100개까지만 조회 가능합니다.")
    @Builder.Default
    private Integer size = 10;
}
