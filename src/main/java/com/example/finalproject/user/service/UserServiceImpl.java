package com.example.finalproject.user.service;

import com.example.finalproject.global.util.GeometryUtil;
import com.example.finalproject.store.dto.response.StoreNearbyResponse;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.dto.request.GetStoreSearchRequest;
import com.example.finalproject.user.repository.AddressRepository;
import com.example.finalproject.user.repository.UserRepository;
import com.example.finalproject.user.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final StoreRepository storeRepository;

    @Override
    public Slice<StoreNearbyResponse> getNearbyStores(GetStoreSearchRequest request) {
        return storeRepository.findNearbyStoresByCategory(request);
    }

}
