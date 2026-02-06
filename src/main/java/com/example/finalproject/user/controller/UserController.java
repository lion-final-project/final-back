package com.example.finalproject.user.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.store.dto.response.StoreNearbyResponse;
import com.example.finalproject.user.dto.request.GetStoreSearchRequest;
import com.example.finalproject.user.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<Slice<StoreNearbyResponse>>> findNearbyStores(
            @ModelAttribute @Valid GetStoreSearchRequest request
    ){
        Slice<StoreNearbyResponse> response = userService.getNearbyStores(request);
        return ResponseEntity.ok(ApiResponse.success("마켓 조회 성공", response));
    }
}
