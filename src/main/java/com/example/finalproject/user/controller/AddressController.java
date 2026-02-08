package com.example.finalproject.user.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.user.dto.request.PostAddressCreateRequest;
import com.example.finalproject.user.dto.request.PutAddressUpdateRequest;
import com.example.finalproject.user.dto.response.GetAddressResponse;
import com.example.finalproject.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/addresses")
public class AddressController {

    private final AddressService addressService;

    /**
     * 배송지 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GetAddressResponse>>> getAddresses(
            Authentication authentication) {
        List<GetAddressResponse> response = addressService.getAddresses(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 배송지 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createAddress(
            @Valid @RequestBody PostAddressCreateRequest request,
            Authentication authentication) {
        addressService.createAddress(request, authentication.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success());
    }

    /**
     * 배송지 수정시 기존 데이터 출력을 위한 단건 조회 api
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<GetAddressResponse>> getAddress(
            @PathVariable Long addressId,
            Authentication authentication) {
        GetAddressResponse response = addressService.getAddress(addressId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 배송지 수정
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody PutAddressUpdateRequest request,
            Authentication authentication) {
        addressService.updateAddress(addressId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 기본 배송지 변경
     */
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(
            @PathVariable Long addressId,
            Authentication authentication) {
        addressService.setDefaultAddress(addressId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 배송지 삭제
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long addressId,
            Authentication authentication) {
        addressService.deleteAddress(addressId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
