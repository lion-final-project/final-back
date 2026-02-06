package com.example.finalproject.product.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductSearchResponse {

    private Long productId;
    private String productName;
    private Integer price;
    private Integer salePrice;
    private Integer discountRate;
    private String imageUrl;
    private Integer stock;
    private String storeName;
    private Long storeId;
    private Double distance;
}
