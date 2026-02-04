package com.example.finalproject.product.dto.response;

import com.example.finalproject.product.domain.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GetProductResponse {

    private Long productId;
    private Long categoryId;
    private String categoryName;
    private String productName;
    private String description;
    private Integer price;
    private Integer salePrice;
    private Integer discountRate;
    private Integer stock;
    private String origin;
    private Boolean isActive;
    private Integer orderCount;
    private String productImageUrl;

    public static GetProductResponse from(Product product) {
        return GetProductResponse.builder()
                .productId(product.getId())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getCategoryName())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .discountRate(product.getDiscountRate())
                .stock(product.getStock())
                .origin(product.getOrigin())
                .isActive(product.getIsActive())
                .orderCount(product.getOrderCount())
                .productImageUrl(product.getProductImageUrl())
                .build();
    }
}
