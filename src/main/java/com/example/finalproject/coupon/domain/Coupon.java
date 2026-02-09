package com.example.finalproject.coupon.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 보유 쿠폰. 주문 시 적용 시 할인 금액(discountAmount)만큼 차감
 * 추가 기능 확장 시 사용 여부/만료일 등 확장 가능
 * 추가 구현 내용으로 임시 생성
 */
@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    //할인 금액(원) 쿠폰 적용 시 이 금액만큼 차감
    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_coupons_user"))
    private User user;

    @Builder
    public Coupon(String name, Integer discountAmount, User user) {
        this.name = name;
        this.discountAmount = discountAmount != null ? discountAmount : 0;
        this.user = user;
    }
}
