package com.example.finalproject.user.service.interfaces;

import com.example.finalproject.store.dto.response.StoreNearbyResponse;
import com.example.finalproject.user.dto.request.GetStoreSearchRequest;
import com.example.finalproject.user.dto.response.GetMyProfileResponse;
import com.example.finalproject.user.dto.response.GetWithdrawalCheckResponse;
import com.example.finalproject.user.dto.response.PostWithdrawalConfirmResponse;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;

public interface UserService {
    /**
     * 근방 3km 이내의 store 조회
     */
    Slice<StoreNearbyResponse> getNearbyStores(GetStoreSearchRequest request);

    GetMyProfileResponse getMyProfile(Authentication authentication);

    GetWithdrawalCheckResponse checkWithdrawalEligibility(Authentication authentication);

    PostWithdrawalConfirmResponse withdraw(Authentication authentication);
}
