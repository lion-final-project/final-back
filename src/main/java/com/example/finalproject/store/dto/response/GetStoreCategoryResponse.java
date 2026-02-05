package com.example.finalproject.store.dto.response;

import com.example.finalproject.store.domain.StoreCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetStoreCategoryResponse {

    private Long id;
    private String categoryName;

    public static GetStoreCategoryResponse from(StoreCategory category) {
        return new GetStoreCategoryResponse(category.getId(), category.getCategoryName());
    }

    public static List<GetStoreCategoryResponse> fromList(List<StoreCategory> categories) {
        return categories.stream()
                .map(GetStoreCategoryResponse::from)
                .collect(Collectors.toList());
    }
}
