package com.example.finalproject.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class PostProductRequest {

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 200, message = "상품명은 200자를 초과할 수 없습니다.")
    private String productName;

    @Size(max = 2000, message = "상품 설명은 2000자를 초과할 수 없습니다.")
    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 1, message = "가격은 1 이상이어야 합니다.")
    private Integer price;

    @Min(value = 0, message = "할인율은 0 이상이어야 합니다.")
    @Max(value = 99, message = "할인율은 99 이하이어야 합니다.")
    private Integer discountRate;

    @Size(max = 100, message = "원산지는 100자를 초과할 수 없습니다.")
    private String origin;

    @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다.")
    private String productImageUrl;
}
