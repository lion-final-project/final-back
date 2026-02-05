package com.example.finalproject.order.dto.response;

import com.example.finalproject.order.domain.CartProduct;
import com.example.finalproject.product.domain.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetCartItemResponse {

    private Long cartProductId;
    private Long productId;
    private String productName;

    private Integer unitPrice;
    private Integer quantity;
    private Integer lineTotalPrice;

    private Integer stock;
    private String imgUrl;
    private boolean lowStock;
    private boolean active;

    public GetCartItemResponse(
            Long cartProductId,
            Long productId,
            String productName,
            Integer unitPrice,
            Integer quantity,
            Integer lineTotalPrice,
            Integer stock,
            String imgUrl,
            boolean lowStock,
            boolean active) {
        this.cartProductId = cartProductId;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotalPrice = lineTotalPrice;
        this.stock = stock;
        this.imgUrl = imgUrl;
        this.lowStock = lowStock;
        this.active = active;
    }

    public static GetCartItemResponse from(CartProduct cp) {
        Product product = cp.getProduct();

        int unitPrice = product.getEffectivePrice();
        int lineTotal = cp.getLinePrice();

        return new GetCartItemResponse(
                cp.getId(),
                product.getId(),
                product.getProductName(),
                unitPrice,
                cp.getQuantity(),
                lineTotal,
                product.getStock(),
                product.getProductImageUrl(),
                product.isLowStock(),
                Boolean.TRUE.equals(product.getIsActive())
        );
    }
}
