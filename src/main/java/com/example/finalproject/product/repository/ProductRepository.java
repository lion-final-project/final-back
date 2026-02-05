package com.example.finalproject.product.repository;

import com.example.finalproject.product.domain.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = """
            SELECT p.id as productId, p.product_name as productName, p.price as price,
                   p.sale_price as salePrice, p.discount_rate as discountRate,
                   p.product_image_url as imageUrl, p.stock as stock,
                   s.store_name as storeName, s.id as storeId,
                   ST_Distance(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) / 1000 as distance
            FROM products p
            JOIN stores s ON p.store_id = s.id
            WHERE p.is_active = true
              AND s.status = 'APPROVED'
              AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 3000)
              AND p.product_name ILIKE '%' || :keyword || '%'
              AND (:categoryId IS NULL OR p.category_id = :categoryId)
            ORDER BY p.id
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM products p
            JOIN stores s ON p.store_id = s.id
            WHERE p.is_active = true
              AND s.status = 'APPROVED'
              AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 3000)
              AND p.product_name ILIKE '%' || :keyword || '%'
              AND (:categoryId IS NULL OR p.category_id = :categoryId)
            """,
            nativeQuery = true)
    Page<ProductSearchProjection> searchByKeywordRecommended(
            @Param("keyword") String keyword,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query(value = """
            SELECT p.id as productId, p.product_name as productName, p.price as price,
                   p.sale_price as salePrice, p.discount_rate as discountRate,
                   p.product_image_url as imageUrl, p.stock as stock,
                   s.store_name as storeName, s.id as storeId,
                   ST_Distance(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) / 1000 as distance
            FROM products p
            JOIN stores s ON p.store_id = s.id
            WHERE p.is_active = true
              AND s.status = 'APPROVED'
              AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 3000)
              AND p.product_name ILIKE '%' || :keyword || '%'
              AND (:categoryId IS NULL OR p.category_id = :categoryId)
            ORDER BY p.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM products p
            JOIN stores s ON p.store_id = s.id
            WHERE p.is_active = true
              AND s.status = 'APPROVED'
              AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 3000)
              AND p.product_name ILIKE '%' || :keyword || '%'
              AND (:categoryId IS NULL OR p.category_id = :categoryId)
            """,
            nativeQuery = true)
    Page<ProductSearchProjection> searchByKeywordNewest(
            @Param("keyword") String keyword,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query(value = """
            SELECT p.id as productId, p.product_name as productName, p.price as price,
                   p.sale_price as salePrice, p.discount_rate as discountRate,
                   p.product_image_url as imageUrl, p.stock as stock,
                   s.store_name as storeName, s.id as storeId,
                   ST_Distance(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) / 1000 as distance
            FROM products p
            JOIN stores s ON p.store_id = s.id
            WHERE p.is_active = true
              AND s.status = 'APPROVED'
              AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 3000)
              AND p.product_name ILIKE '%' || :keyword || '%'
              AND (:categoryId IS NULL OR p.category_id = :categoryId)
            ORDER BY p.order_count DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM products p
            JOIN stores s ON p.store_id = s.id
            WHERE p.is_active = true
              AND s.status = 'APPROVED'
              AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 3000)
              AND p.product_name ILIKE '%' || :keyword || '%'
              AND (:categoryId IS NULL OR p.category_id = :categoryId)
            """,
            nativeQuery = true)
    Page<ProductSearchProjection> searchByKeywordSales(
            @Param("keyword") String keyword,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query(value = """
            SELECT p.id as productId, p.product_name as productName, p.price as price,
                   p.sale_price as salePrice, p.discount_rate as discountRate,
                   p.product_image_url as imageUrl, p.stock as stock,
                   s.store_name as storeName, s.id as storeId,
                   ST_Distance(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) / 1000 as distance
            FROM products p
            JOIN stores s ON p.store_id = s.id
            WHERE p.is_active = true
              AND s.status = 'APPROVED'
              AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 3000)
              AND p.product_name ILIKE '%' || :keyword || '%'
              AND (:categoryId IS NULL OR p.category_id = :categoryId)
            ORDER BY COALESCE(p.sale_price, p.price) ASC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM products p
            JOIN stores s ON p.store_id = s.id
            WHERE p.is_active = true
              AND s.status = 'APPROVED'
              AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 3000)
              AND p.product_name ILIKE '%' || :keyword || '%'
              AND (:categoryId IS NULL OR p.category_id = :categoryId)
            """,
            nativeQuery = true)
    Page<ProductSearchProjection> searchByKeywordPriceAsc(
            @Param("keyword") String keyword,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query(value = """
            SELECT p.id as productId, p.product_name as productName, p.price as price,
                   p.sale_price as salePrice, p.discount_rate as discountRate,
                   p.product_image_url as imageUrl, p.stock as stock,
                   s.store_name as storeName, s.id as storeId,
                   ST_Distance(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) / 1000 as distance
            FROM products p
            JOIN stores s ON p.store_id = s.id
            WHERE p.is_active = true
              AND s.status = 'APPROVED'
              AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 3000)
              AND p.product_name ILIKE '%' || :keyword || '%'
              AND (:categoryId IS NULL OR p.category_id = :categoryId)
            ORDER BY COALESCE(p.sale_price, p.price) DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM products p
            JOIN stores s ON p.store_id = s.id
            WHERE p.is_active = true
              AND s.status = 'APPROVED'
              AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 3000)
              AND p.product_name ILIKE '%' || :keyword || '%'
              AND (:categoryId IS NULL OR p.category_id = :categoryId)
            """,
            nativeQuery = true)
    Page<ProductSearchProjection> searchByKeywordPriceDesc(
            @Param("keyword") String keyword,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    interface ProductSearchProjection {
        Long getProductId();
        String getProductName();
        Integer getPrice();
        Integer getSalePrice();
        java.math.BigDecimal getDiscountRate();
        String getImageUrl();
        Integer getStock();
        String getStoreName();
        Long getStoreId();
        Double getDistance();
    }
}
