package com.example.finalproject.product.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.product.dto.request.ProductSearchRequest;
import com.example.finalproject.product.dto.response.ProductSearchResponse;
import com.example.finalproject.product.service.ProductSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductSearchResponse>>> searchProducts(
            @Valid @ModelAttribute ProductSearchRequest request,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ProductSearchResponse> result = productSearchService.searchProducts(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
