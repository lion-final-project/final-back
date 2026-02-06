package com.example.finalproject.product.dto.response;

import com.example.finalproject.product.domain.ProductCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCategoryResponse {

    private Long id;
    private String categoryName;
    private String iconUrl;

    public static GetCategoryResponse from(ProductCategory productCategory) {
        return GetCategoryResponse.builder()
                .id(productCategory.getId())
                .categoryName(productCategory.getCategoryName())
                .iconUrl(productCategory.getIconUrl())
                .build();
    }
}
