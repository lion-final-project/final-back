package com.example.finalproject.order.repository.custom;

import com.example.finalproject.order.domain.QOrder;
import com.example.finalproject.order.domain.QOrderProduct;
import com.example.finalproject.order.domain.QStoreOrder;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.store.domain.QStore;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreOrderRepositoryImpl implements StoreOrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<StoreOrder> findUserStoreOrders(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String keyword,
            Pageable pageable) {

        QStoreOrder storeOrder = QStoreOrder.storeOrder;
        QOrder order = QOrder.order;
        QStore store = QStore.store;

        List<StoreOrder> content = queryFactory
                .selectFrom(storeOrder)
                .join(storeOrder.order, order).fetchJoin()
                .join(storeOrder.store, store).fetchJoin()
                .where(order.user.id.eq(userId)
                        .and(orderedAfter(startDate, order))
                        .and(orderedBefore(endDate, order))
                        .and(keywordCondition(keyword, storeOrder)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(storeOrder.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(storeOrder.count())
                .from(storeOrder)
                .join(storeOrder.order, order)
                .where(order.user.id.eq(userId)
                        .and(orderedAfter(startDate, order))
                        .and(orderedBefore(endDate, order))
                        .and(keywordCondition(keyword, storeOrder)))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression orderedAfter(LocalDateTime startDate, QOrder order) {
        return startDate != null
                ? order.orderedAt.goe(startDate)
                : null;
    }

    private BooleanExpression orderedBefore(LocalDateTime endDate, QOrder order) {
        return endDate != null
                ? order.orderedAt.loe(endDate)
                : null;
    }

    private BooleanExpression keywordCondition(
            String keyword,
            QStoreOrder storeOrder) {

        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        QOrderProduct orderProduct = QOrderProduct.orderProduct;

        return JPAExpressions
                .selectOne()
                .from(orderProduct)
                .where(
                        orderProduct.storeOrder.eq(storeOrder)
                                .and(orderProduct.productNameSnapshot.containsIgnoreCase(keyword))
                )
                .exists();
    }
}
