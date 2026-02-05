package com.example.finalproject.product.dto.request;

import com.example.finalproject.product.enums.ProductSortType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchRequest {

    @NotBlank(message = "검색어는 필수입니다.")
    @Size(min = 2, max = 8, message = "검색어는 2자 이상 8자 이하여야 합니다.")
    private String keyword;

    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

    private Long categoryId;

    private ProductSortType sort = ProductSortType.RECOMMENDED;
}
