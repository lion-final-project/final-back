package com.example.finalproject.store.repository.custom;

import com.example.finalproject.store.dto.response.QStoreNearbyResponse;
import com.example.finalproject.store.dto.response.StoreNearbyResponse;
import com.example.finalproject.store.enums.StoreActiveStatus;
import com.example.finalproject.store.enums.StoreStatus;

import com.example.finalproject.user.dto.request.GetStoreSearchRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import com.example.finalproject.global.util.GeometryUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import static com.example.finalproject.product.domain.QProduct.product;
import static com.example.finalproject.store.domain.QStore.store;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private static final double SEARCH_RADIUS = 3000.0;

    @Override
    public Slice<StoreNearbyResponse> findNearbyStoresByCategory(GetStoreSearchRequest request) {
        Point currentLocation = GeometryUtil.createPoint(request.getLongitude(), request.getLatitude());

        // 1. 거리 계산 쿼리
        NumberTemplate<Double> distanceExpr = Expressions.numberTemplate(
                Double.class,
                "ST_Distance({0}, {1})",
                store.address.location,
                currentLocation
        );

        NumberTemplate<Double> latitudeExpr = Expressions.numberTemplate(
                Double.class,
                "ST_Y(ST_GeometryFromText(ST_AsText({0})))",
                store.address.location
        );

        NumberTemplate<Double> longitudeExpr = Expressions.numberTemplate(
                Double.class,
                "ST_X(ST_GeometryFromText(ST_AsText({0})))",
                store.address.location
        );

        List<StoreNearbyResponse> content = queryFactory
                .select(new QStoreNearbyResponse(
                        store.id,
                        store.storeName,
                        distanceExpr.coalesce(0.0),
                        store.reviewCount.coalesce(0),
                        store.storeImage,
                        store.isActive.eq(StoreActiveStatus.ACTIVE),
                        store.address.addressLine1,
                        store.address.addressLine2,
                        latitudeExpr,
                        longitudeExpr
                ))
                .from(store)
                .where(
                        within3km(currentLocation),
                        isApprovedAndActive(),
                        storeCategoryEq(request.getStoreCategoryId()),    // 마트 카테고리 조건 (직접 필터링)
                        productKeywordCond(request.getKeyword()),         // 상품 키워드 조건 (EXISTS 서브쿼리)
                        cursorCondition(request.getLastDistance(), request.getLastId(), distanceExpr)
                )
                .orderBy(distanceExpr.asc(), store.id.asc())
                .limit(request.getSize() + 1)
                .fetch();

        return checkLastPage(request.getSize(), content);
    }

    /**
     * 마트 카테고리 필터 조건 (Store 엔티티 직접 참조)
     */
    private BooleanExpression storeCategoryEq(Long storeCategoryId) {
        return storeCategoryId != null ? store.storeCategory.id.eq(storeCategoryId) : null;
    }

    /**
     * 상품 키워드 검색 조건 (해당 키워드의 상품을 하나라도 가지고 있는 마트인지 확인)
     */
    private BooleanExpression productKeywordCond(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return JPAExpressions
                .selectOne()
                .from(product)
                .where(
                        product.store.id.eq(store.id),
                        product.isActive.isTrue(),
                        product.productName.containsIgnoreCase(keyword)
                ).exists();
    }

    /**
     * 1. 반경 3km 필터 조건 (is true 구문 유지로 Hibernate 6 에러 방지)
     */
    private BooleanExpression within3km(Point currentLocation) {
        return Expressions.booleanTemplate(
                "ST_DWithin({0}, {1}, {2}) is true",
                store.address.location, currentLocation, SEARCH_RADIUS
        );
    }

    /**
     * 2. 승인됨, 영업중, 삭제되지 않음 기본 상태 필터
     */
    private BooleanExpression isApprovedAndActive() {
        return store.status.eq(StoreStatus.APPROVED)
                .and(store.isActive.eq(StoreActiveStatus.ACTIVE))
                .and(store.deletedAt.isNull());
    }

    /**
     * 3. 상품 조건 필터
     */
    private BooleanExpression productSearchCond(Long categoryId, String keyword) {
        if (categoryId == null && (keyword == null || keyword.isBlank())) {
            return null;
        }

        return JPAExpressions
                .selectOne()
                .from(product)
                .where(
                        product.store.id.eq(store.id), // 마트와 연관관계
                        product.isActive.isTrue(),
                        categoryIdEq(categoryId),
                        productNameContains(keyword)
                ).exists();
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? product.productCategory.id.eq(categoryId) : null;
    }

    private BooleanExpression productNameContains(String keyword) {
        return (keyword != null && !keyword.isBlank()) ? product.productName.containsIgnoreCase(keyword) : null;
    }

    /**
     * 4. No-Offset 무한 스크롤 페이징을 위한 커서 조건
     */
    private BooleanExpression cursorCondition(Double lastDistance, Long lastId, NumberTemplate<Double> distanceExpr) {
        if (lastDistance == null || lastId == null) return null;

        return distanceExpr.gt(lastDistance)
                .or(distanceExpr.eq(lastDistance).and(store.id.gt(lastId)));
    }

    /**
     * 5. 다음 페이지 존재 여부 판단 및 Slice 생성
     */
    private Slice<StoreNearbyResponse> checkLastPage(int size, List<StoreNearbyResponse> content) {
        boolean hasNext = false;
        if (content.size() > size) {
            content.remove(size);
            hasNext = true;
        }
        return new SliceImpl<>(content, PageRequest.of(0, size), hasNext);
    }
}