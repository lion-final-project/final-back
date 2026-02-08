package com.example.finalproject.user.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.util.GeometryUtil;
import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.dto.request.PostAddressCreateRequest;
import com.example.finalproject.user.dto.request.PutAddressUpdateRequest;
import com.example.finalproject.user.dto.response.GetAddressResponse;
import com.example.finalproject.user.repository.AddressRepository;
import com.example.finalproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<GetAddressResponse> getAddresses(String username) {
        return addressRepository.findByUserEmail(username)
                .stream()
                .map(GetAddressResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createAddress(PostAddressCreateRequest request, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        /**
         * todo:
         *  1. 최대 등록 개수 5개 확인
         *  2. 배송지 주소 중복 불가
         *  3. 배송지 이름 중복 불가
         */

        if (request.getIsDefault()) {
            addressRepository.clearDefaultByUser(user);
        }

        Point point = GeometryUtil.createPoint(request.getLongitude(), request.getLatitude());

        Address address = Address.builder()
                .user(user)
                .contact(request.getContact())
                .addressName(request.getAddressName())
                .postalCode(request.getPostalCode())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .location(point)
                .isDefault(request.getIsDefault())
                .build();

        addressRepository.save(address);
        log.info("Address created: {}", address.toString());
    }

    // 수정 시 기존 데이터 출력을 위한 단건 조회 메서드
    public GetAddressResponse getAddress(Long addressId, String username) {
        Address address = addressRepository.findByIdWithUser(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getUser().getEmail().equals(username)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return GetAddressResponse.from(address);
    }

    @Transactional
    public void updateAddress(Long addressId, PutAddressUpdateRequest request, String username) {
        Address address = addressRepository.findByIdWithUser(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getUser().getEmail().equals(username)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (request.getIsDefault()) {
            addressRepository.clearDefaultByUser(address.getUser());
        }

        Point point = GeometryUtil.createPoint(request.getLongitude(), request.getLatitude());

        address.update(
                request.getAddressName(),
                request.getPostalCode(),
                request.getAddressLine1(),
                request.getAddressLine2(),
                request.getContact(),
                point,
                request.getIsDefault()
        );
    }

    @Transactional
    public void setDefaultAddress(Long addressId, String username) {
        Address address = addressRepository.findByIdWithUser(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getUser().getEmail().equals(username)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        addressRepository.clearDefaultByUser(address.getUser());
        address.changeDefault();
    }

    @Transactional
    public void deleteAddress(Long addressId, String username) {
        Address address = addressRepository.findByIdWithUser(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        //todo: 1개 남았을때 삭제 불가

        if (!address.getUser().getEmail().equals(username)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        addressRepository.delete(address);
    }
}
