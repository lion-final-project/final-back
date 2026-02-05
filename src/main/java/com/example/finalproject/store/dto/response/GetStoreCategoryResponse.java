package com.example.finalproject.store.dto.response;

import com.example.finalproject.store.enums.StoreCategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetStoreCategoryResponse {

    private String code;        // enum name (e.g. FRUIT_VEGETABLE)
    private String displayName; // 표시명 (e.g. 과일/채소)

    public static GetStoreCategoryResponse from(StoreCategoryType type) {
        return new GetStoreCategoryResponse(type.name(), type.getDisplayName());
    }

    public static List<GetStoreCategoryResponse> listAll() {
        return Arrays.stream(StoreCategoryType.values())
                .map(GetStoreCategoryResponse::from)
                .collect(Collectors.toList());
    }
}
