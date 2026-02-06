package com.example.finalproject.user.service.interfaces;

import com.example.finalproject.store.dto.response.StoreNearbyResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface UserService {

    /**
     * 유저의 특정 주소 또는 기본 주소를 기준으로 반경 3km 내의 마트 목록을 조회
     * @param userName      유저 이메일
     * @param addressId     주소 식별자
     * @param storeCategoryId   마켓 카테고리
     * @param keyword       검색어
     * @param lastDistance  페이징 : 마지막으로 조회된 데이터의 거리
     * @param lastId        페이징 : 마지막으로 조회된 데이터의 식별자
     * @param pageable      페이지 정보
     * @return 3km 이내 마트 목록 정보를 담은 Slice 객체
     */
    Slice<StoreNearbyResponse> getNearbyStoresByAddress(
            String userName,
            Long addressId,
            Long storeCategoryId ,
            String keyword,
            Double lastDistance,
            Long lastId,
            Pageable pageable
    );

    /**
      * 유저의 특정 주소 또는 기본 주소를 기준으로 반경 3km 내의 마트 목록을 조회
     * @param latitude 위도
     * @param longitude 경도
     * @param keyword       검색어
     * @param lastDistance  페이징 : 마지막으로 조회된 데이터의 거리
     * @param lastId        페이징 : 마지막으로 조회된 데이터의 식별자
     * @param pageable      페이지 정보
     * @return 3km 이내 마트 목록 정보를 담은 Slice 객체
     */
    Slice<StoreNearbyResponse> getNearbyStoresByAddressForNotUser(
            Double latitude,
            Double longitude,
            Long storeCategoryId,
            String keyword,
            Double lastDistance,
            Long lastId,
            Pageable pageable
    );
}
