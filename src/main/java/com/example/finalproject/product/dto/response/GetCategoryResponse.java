package com.example.finalproject.product.dto.response;

import com.example.finalproject.product.domain.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCategoryResponse {

    private Long id;
    private String categoryName;
    private String iconUrl;

    public static GetCategoryResponse from(Category category) {
        return GetCategoryResponse.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .iconUrl(category.getIconUrl())
                .build();
    }
}
