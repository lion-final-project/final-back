package com.example.finalproject.store.repository.custom;

import static com.example.finalproject.product.domain.QCategory.category;
import static com.example.finalproject.product.domain.QProduct.product;
import static com.example.finalproject.store.domain.QStore.store;

import com.example.finalproject.store.dto.response.QStoreNearbyResponse;
import com.example.finalproject.store.dto.response.StoreNearbyResponse;
import com.example.finalproject.store.enums.StoreActiveStatus;
import com.example.finalproject.store.enums.StoreStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import com.example.finalproject.global.util.GeometryUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final double SEARCH_RADIUS = 3000.0; // 3km 반경 상수화

    @Override
    public Slice<StoreNearbyResponse> findNearbyStoresByCategory(
            Double latitude,
            Double longitude,
            Long categoryId,
            String keyword,
            Double lastDistance,
            Long lastId,
            Pageable pageable
    ) {
        Point currentLocation = GeometryUtil.createPoint(longitude, latitude);

        // 1. 거리 계산 표현식
        NumberTemplate<Double> distanceExpr = Expressions.numberTemplate(
                Double.class,
                "ST_Distance({0}, {1})",
                store.location,
                currentLocation
        );

        // 2. 최종 결과 조회
        List<StoreNearbyResponse> content = queryFactory
                .select(new QStoreNearbyResponse(
                        store.id,
                        store.storeName,
                        distanceExpr.coalesce(0.0),
                        store.reviewCount.coalesce(0),
                        store.storeImage,
                        store.isActive.eq(StoreActiveStatus.ACTIVE),
                        store.addressLine1
                ))
                .from(store)
                .where(
                        within3km(currentLocation),      // 반경 필터
                        isApprovedAndActive(),           // 상태 필터
                        productSearchCond(categoryId, keyword), // 상품 필터(Exists 사용)
                        cursorCondition(lastDistance, lastId, distanceExpr) // 페이징 커서
                )
                .orderBy(distanceExpr.asc(), store.id.asc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return checkLastPage(pageable, content);
    }

    /**
     * 1. 반경 3km 필터 조건 (is true 구문 유지로 Hibernate 6 에러 방지)
     */
    private BooleanExpression within3km(Point currentLocation) {
        return Expressions.booleanTemplate(
                "ST_DWithin({0}, {1}, {2}) is true",
                store.location, currentLocation, SEARCH_RADIUS
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
        return categoryId != null ? product.category.id.eq(categoryId) : null;
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
    private Slice<StoreNearbyResponse> checkLastPage(Pageable pageable, List<StoreNearbyResponse> content) {
        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }
        return new SliceImpl<>(content, pageable, hasNext);
    }
}