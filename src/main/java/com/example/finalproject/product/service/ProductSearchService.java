package com.example.finalproject.product.service;

import com.example.finalproject.product.dto.request.ProductSearchRequest;
import com.example.finalproject.product.dto.response.ProductSearchResponse;
import com.example.finalproject.product.enums.ProductSortType;
import com.example.finalproject.product.repository.ProductRepository;
import com.example.finalproject.product.repository.ProductRepository.ProductSearchProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchService {

    private final ProductRepository productRepository;

    public Page<ProductSearchResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
        String keyword = request.getKeyword();
        Double latitude = request.getLatitude();
        Double longitude = request.getLongitude();
        Long categoryId = request.getCategoryId();
        ProductSortType sortType = request.getSort() != null ? request.getSort() : ProductSortType.RECOMMENDED;

        Page<ProductSearchProjection> projections = switch (sortType) {
            case NEWEST -> productRepository.searchByKeywordNewest(keyword, latitude, longitude, categoryId, pageable);
            case SALES -> productRepository.searchByKeywordSales(keyword, latitude, longitude, categoryId, pageable);
            case PRICE_ASC -> productRepository.searchByKeywordPriceAsc(keyword, latitude, longitude, categoryId, pageable);
            case PRICE_DESC -> productRepository.searchByKeywordPriceDesc(keyword, latitude, longitude, categoryId, pageable);
            default -> productRepository.searchByKeywordRecommended(keyword, latitude, longitude, categoryId, pageable);
        };

        return projections.map(this::toResponse);
    }

    private ProductSearchResponse toResponse(ProductSearchProjection projection) {
        Integer discountRate = null;
        if (projection.getDiscountRate() != null) {
            discountRate = projection.getDiscountRate().intValue();
        }

        return ProductSearchResponse.builder()
                .productId(projection.getProductId())
                .productName(projection.getProductName())
                .price(projection.getPrice())
                .salePrice(projection.getSalePrice())
                .discountRate(discountRate)
                .imageUrl(projection.getImageUrl())
                .stock(projection.getStock())
                .storeName(projection.getStoreName())
                .storeId(projection.getStoreId())
                .distance(projection.getDistance() != null ?
                        Math.round(projection.getDistance() * 10.0) / 10.0 : null)
                .build();
    }
}
