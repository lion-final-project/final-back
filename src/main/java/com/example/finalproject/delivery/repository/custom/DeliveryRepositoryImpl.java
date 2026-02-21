package com.example.finalproject.delivery.repository.custom;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.payment.enums.PaymentStatus;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.finalproject.delivery.domain.QDelivery.delivery;
import static com.example.finalproject.order.domain.QStoreOrder.storeOrder;
import static com.example.finalproject.payment.domain.QPayment.payment;
import static com.example.finalproject.user.domain.QUser.user;
import static com.example.finalproject.order.domain.QOrder.order;

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Delivery> findTrackableByUserIdAndStatuses(Long userId,
                                                         PaymentStatus paymentStatus,
                                                         List<DeliveryStatus> statuses,
                                                         Pageable pageable) {


    // 1. 데이터 조회를 위한 쿼리
    List<Delivery> content = queryFactory
      .selectFrom(delivery)
      .join(delivery.storeOrder, storeOrder)
      .join(storeOrder.order, order)
      .join(order.user, user)
      .join(payment).on(payment.order.eq(order)) // 세타 조인(연관관계 없는 경우) 처리
      .where(
        user.id.eq(userId),
        payment.paymentStatus.eq(paymentStatus),
        delivery.status.in(statuses)
      )
      .orderBy(delivery.createdAt.desc())
      .offset(pageable.getOffset())
      .limit(pageable.getPageSize())
      .fetch();

    // 2. 카운트 쿼리 (Pageable 처리를 위해 필요)
    JPAQuery<Long> countQuery = queryFactory
      .select(delivery.count())
      .from(delivery)
      .join(delivery.storeOrder, storeOrder)
      .join(storeOrder.order, order)
      .join(order.user, user)
      .join(payment).on(payment.order.eq(order))
      .where(
        user.id.eq(userId),
        payment.paymentStatus.eq(paymentStatus),
        delivery.status.in(statuses)
      );

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }
}
