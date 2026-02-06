package com.example.finalproject.user.service;

import com.example.finalproject.global.util.GeometryUtil;
import com.example.finalproject.store.dto.response.StoreNearbyResponse;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.User;
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
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    //비회원용 조회함수
    @Override
    public Slice<StoreNearbyResponse> getNearbyStoresByAddressForNotUser(
            Double latitude,
            Double longitude,
            Long storeCategoryId,
            String keyword,
            Double lastDistance,
            Long lastId,
            Pageable pageable
    ) {
        return storeRepository.findNearbyStoresByCategory(
                latitude,
                longitude,
                storeCategoryId,
                keyword,
                lastDistance,
                lastId,
                pageable
        );
    }

    //회원용 조회함수
    @Override
    public Slice<StoreNearbyResponse> getNearbyStoresByAddress(
            String userName,
            Long addressId,
            Long storeCategoryId,
            String keyword,
            Double lastDistance,
            Long lastId,
            Pageable pageable
    ) {

        User user = findUserByEmail(userName);
        Address address = findAddressById(addressId);


        if(!address.getUser().equals(user)){
            throw new RuntimeException("User not found");
        }

        return storeRepository.findNearbyStoresByCategory(
                GeometryUtil.getLatitude(address.getLocation()),
                GeometryUtil.getLongitude(address.getLocation()),
                storeCategoryId,
                keyword,
                lastDistance,
                lastId,
                pageable
        );
    }

    private Address findAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
    }
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
