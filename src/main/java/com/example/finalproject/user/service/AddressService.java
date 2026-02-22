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
import org.jetbrains.annotations.NotNull;
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

        //최대 등록 개수 5개 검증
        validateAddressLimit(user.getId());
        //같은 이름의 배송지가 존재하는지 검증
        validateDuplicateAddressName(user.getId(), request.getAddressName());
        //같은 배송지가 존재하는지 검증
        validateDuplicateAddress(user.getId(), request.getAddressLine1(), request.getAddressLine2());

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
        log.info("Address created: id={}, userId={}", address.getId(), address.getUser().getId());
    }

    // 수정 시 기존 데이터 출력을 위한 단건 조회 메서드
    public GetAddressResponse getAddress(Long addressId, String username) {
        Address address = getAddressWithOwnerCheck(addressId, username);
        return GetAddressResponse.from(address);
    }

    @Transactional
    public void updateAddress(Long addressId, PutAddressUpdateRequest request, String username) {
        Address address = getAddressWithOwnerCheck(addressId, username);

        // 중복 검증 (자기 자신 제외)
        validateDuplicateAddressName(address.getUser().getId(), request.getAddressName(), addressId);
        validateDuplicateAddress(address.getUser().getId(), request.getAddressLine1(), request.getAddressLine2(), addressId);

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
        Address address = getAddressWithOwnerCheck(addressId, username);

        addressRepository.clearDefaultByUser(address.getUser());
        address.changeDefault();
    }

    @Transactional
    public void deleteAddress(Long addressId, String username) {
        Address address = getAddressWithOwnerCheck(addressId, username);

        if (addressRepository.countByUserId(address.getUser().getId()) <= 1) {
            throw new BusinessException(ErrorCode.ADDRESS_DELETE_MIN_REQUIRED);
        }

        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);

        // 기본 배송지 삭제 시 → 가장 최근 배송지를 기본으로
        if (wasDefault) {
            addressRepository.flush();
            addressRepository.findFirstByUserIdOrderByCreatedAtDesc(address.getUser().getId())
                    .ifPresent(Address::changeDefault);
        }
    }

    @NotNull
    private Address getAddressWithOwnerCheck(Long addressId, String username) {
        Address address = addressRepository.findByIdWithUser(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getUser().getEmail().equals(username)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return address;
    }

    private void validateAddressLimit(Long userId) {
        long count = addressRepository.countByUserId(userId);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.ADDRESS_LIMIT_EXCEEDED);
        }
    }

    private void validateDuplicateAddressName(Long userId, String addressName) {
        if (addressRepository.existsByUserIdAndAddressName(userId, addressName)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ADDRESS_NAME);
        }
    }

    private void validateDuplicateAddressName(Long userId, String addressName, Long excludeId) {
        if (addressRepository.existsByUserIdAndAddressNameAndIdNot(userId, addressName, excludeId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ADDRESS_NAME);
        }
    }

    private void validateDuplicateAddress(Long userId, String addressLine1, String addressLine2) {
        if (addressRepository.existsByUserIdAndAddressLine1AndAddressLine2(userId, addressLine1, addressLine2)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ADDRESS);
        }
    }

    private void validateDuplicateAddress(Long userId, String addressLine1, String addressLine2, Long excludeId) {
        if (addressRepository.existsByUserIdAndAddressLine1AndAddressLine2AndIdNot(userId, addressLine1, addressLine2, excludeId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ADDRESS);
        }
    }
}
