package com.example.finalproject.review.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.order.domain.StoreOrder;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_reviews_store_order"))
    private StoreOrder storeOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reviews_user"))
    private User user;

    @Column(nullable = false)
    private Short rating;

    @Column(length = 100)
    private String content;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

    @Column(name = "owner_reply", length = 500)
    private String ownerReply;

    @Column(name = "owner_reply_at")
    private LocalDateTime ownerReplyAt;

    @Builder
    public Review(StoreOrder storeOrder, User user, Short rating, String content) {
        this.storeOrder = storeOrder;
        this.user = user;
        this.rating = rating;
        this.content = content;
    }

    public void update(String content, Short rating) {
        this.content = content;
        this.rating = rating;
    }

    public void delete() {
        this.isVisible = false;
    }

    public void addOwnerReply(String reply) {

        if (this.ownerReply != null) {
            throw new IllegalStateException("이미 답글이 존재합니다.");
        }

        this.ownerReply = reply;
        this.ownerReplyAt = LocalDateTime.now();
    }

    public void restore(String content, short rating) {
        this.isVisible = true;
        this.content = content;
        this.rating = rating;
    }

}