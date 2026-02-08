package com.example.finalproject.user.service.interfaces;

import com.example.finalproject.store.dto.response.StoreNearbyResponse;
import com.example.finalproject.user.dto.request.GetStoreSearchRequest;
import org.springframework.data.domain.Slice;

public interface UserService {
    /**
     * 근방 3km 이내의 store 조회
     */
    Slice<StoreNearbyResponse> getNearbyStores(GetStoreSearchRequest request);
}
