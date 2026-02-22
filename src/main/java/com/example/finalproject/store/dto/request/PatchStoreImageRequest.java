package com.example.finalproject.store.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PatchStoreImageRequest {

    /** 스토어 대표 이미지 URL (빈 문자열이면 이미지 제거) */
    private String storeImageUrl;
}
