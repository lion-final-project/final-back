package com.example.finalproject.product.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.product.dto.request.PatchProductRequest;
import com.example.finalproject.product.dto.request.PatchProductStatusRequest;
import com.example.finalproject.product.dto.request.PostProductRequest;
import com.example.finalproject.product.dto.request.StockAdjustRequest;
import com.example.finalproject.product.dto.response.CanEditProductResponse;
import com.example.finalproject.product.dto.response.GetCategoryResponse;
import com.example.finalproject.product.dto.response.GetMyProductResponse;
import com.example.finalproject.product.dto.response.GetProductResponse;
import com.example.finalproject.product.dto.response.GetProductStatsResponse;
import com.example.finalproject.product.dto.response.GetStockHistoryResponse;
import com.example.finalproject.product.dto.response.PostProductResponse;
import com.example.finalproject.product.dto.response.StockAdjustResponse;
import com.example.finalproject.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 등록
     * @param request 상품 등록 요청 정보 (카테고리, 상품명, 가격, 할인율 등)
     * @param authentication 현재 인증된 사용자 정보
     * @return 등록된 상품 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostProductResponse>> registerProduct(
            @Valid @RequestBody PostProductRequest request,
            Authentication authentication) {
        
        String userName = authentication.getName();
        PostProductResponse response = productService.registerProduct(userName, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("상품 등록이 완료되었습니다.", response));
    }

    /**
     * 내 마트 상품 목록 조회
     * @param pageable 페이징 정보
     * @param authentication 현재 인증된 사용자 정보
     * @return 내 마트 상품 목록 (페이징)
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<GetMyProductResponse>>> getMyProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        String userName = authentication.getName();
        Page<GetMyProductResponse> response = productService.getMyProducts(userName, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("내 마트 상품 목록 조회가 완료되었습니다.", response));
    }

    /**
     * 상품 카테고리 목록 조회
     * @return 전체 카테고리 목록
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<GetCategoryResponse>>> getCategories() {
        List<GetCategoryResponse> response = productService.getCategories();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("카테고리 목록 조회가 완료되었습니다.", response));
    }

    /**
     * 상품 상세 조회
     * @param productId 상품 ID
     * @return 상품 상세 정보
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<GetProductResponse>> getProduct(@PathVariable Long productId) {
        GetProductResponse response = productService.getProduct(productId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("상품 상세 조회가 완료되었습니다.", response));
    }

    /**
     * 상품 수정 가능 여부 조회 (운영 시간 외에만 수정 가능)
     * @param authentication 현재 인증된 사용자 정보
     * @return 수정 가능 여부 및 메시지
     */
    @GetMapping("/my/can-edit")
    public ResponseEntity<ApiResponse<CanEditProductResponse>> canEditProduct(Authentication authentication) {

        String userName = authentication.getName();
        CanEditProductResponse response = productService.canEditProduct(userName);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("상품 수정 가능 여부 조회가 완료되었습니다.", response));
    }

    /**
     * 상품 정보 수정
     * @param productId 상품 ID
     * @param request 상품 수정 요청 정보
     * @param authentication 현재 인증된 사용자 정보
     * @return 수정된 상품 정보
     */
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<GetProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody PatchProductRequest request,
            Authentication authentication) {


        String userName = authentication.getName();
        GetProductResponse response = productService.updateProduct(userName, productId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("상품 수정이 완료되었습니다.", response));
    }

    /**
     * 상품 삭제 (소프트 삭제)
     * @param productId 상품 ID
     * @param authentication 현재 인증된 사용자 정보
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long productId,
            Authentication authentication) {

        String userName = authentication.getName();
        productService.deleteProduct(userName, productId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("상품 삭제가 완료되었습니다.", null));
    }

    /**
     * 상품 판매 상태 변경 (활성화/비활성화)
     * @param productId 상품 ID
     * @param request 상태 변경 요청 정보
     * @param authentication 현재 인증된 사용자 정보
     * @return 변경된 상품 정보
     */
    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<GetProductResponse>> updateProductStatus(
            @PathVariable Long productId,
            @Valid @RequestBody PatchProductStatusRequest request,
            Authentication authentication) {

        String userName = authentication.getName();
        GetProductResponse response = productService.updateProductStatus(userName, productId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("상품 활성화 상태가 변경되었습니다.", response));
    }

    /**
     * 상품 입고 처리
     * @param productId 상품 ID
     * @param request 입고 수량 요청 정보
     * @param authentication 현재 인증된 사용자 정보
     * @return 입고 처리 결과
     */
    @PostMapping("/{productId}/stock-in")
    public ResponseEntity<ApiResponse<StockAdjustResponse>> stockIn(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustRequest request,
            Authentication authentication) {

        String userName = authentication.getName();
        StockAdjustResponse response = productService.stockIn(userName, productId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("상품 입고가 완료되었습니다.", response));
    }

    /**
     * 상품 출고 처리
     * @param productId 상품 ID
     * @param request 출고 수량 요청 정보
     * @param authentication 현재 인증된 사용자 정보
     * @return 출고 처리 결과
     */
    @PostMapping("/{productId}/stock-out")
    public ResponseEntity<ApiResponse<StockAdjustResponse>> stockOut(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustRequest request,
            Authentication authentication) {

        String userName = authentication.getName();
        StockAdjustResponse response = productService.stockOut(userName, productId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("상품 출고가 완료되었습니다.", response));
    }

    /**
     * 내 마트 상품 통계 조회
     * @param authentication 현재 인증된 사용자 정보
     * @return 상품 통계 정보 (총 상품 수, 활성/비활성 상품 수, 오늘 입출고 수)
     */
    @GetMapping("/my/stats")
    public ResponseEntity<ApiResponse<GetProductStatsResponse>> getProductStats(Authentication authentication) {

        String userName = authentication.getName();
        GetProductStatsResponse response = productService.getProductStats(userName);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("상품 통계 조회가 완료되었습니다.", response));
    }

    /**
     * 입출고 내역 조회
     * @param pageable 페이징 정보
     * @param authentication 현재 인증된 사용자 정보
     * @return 입출고 내역 목록 (페이징)
     */
    @GetMapping("/my/stock-histories")
    public ResponseEntity<ApiResponse<Page<GetStockHistoryResponse>>> getStockHistories(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        String userName = authentication.getName();
        Page<GetStockHistoryResponse> response = productService.getStockHistories(userName, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("입출고 내역 조회가 완료되었습니다.", response));
    }
}
