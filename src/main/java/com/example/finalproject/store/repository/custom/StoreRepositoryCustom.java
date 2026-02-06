package com.example.finalproject.store.repository.custom;

import com.example.finalproject.store.dto.response.StoreNearbyResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface StoreRepositoryCustom {

    /**
     * 근방 3km 이내의 store 조회
     * @param latitude 위도
     * @param longitude 경도
     * @param storeCategoryId 마트 자체의 카테고리 ID
     * @param keyword 상품명 키워드 검색
     * @param lastDistance 마지막 조회 위치(스크롤)
     * @param lastId 마지막으로 조회된 마트(스크롤)
     * @param pageable 한번에 가져올 페이지 사이즈
     */
    Slice<StoreNearbyResponse> findNearbyStoresByCategory(
            Double latitude,
            Double longitude,
            Long storeCategoryId,
            String keyword,
            Double lastDistance,
            Long lastId,
            Pageable pageable
    );
}
