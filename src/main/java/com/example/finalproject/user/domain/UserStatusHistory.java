package com.example.finalproject.user.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.user.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "user_status_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatusHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_status_histories_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "before_status", nullable = false)
    private UserStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "after_status", nullable = false)
    private UserStatus afterStatus;

    @Column(name = "reason", length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_status_histories_changed_by"))
    private User changedBy;

    @Builder
    public UserStatusHistory(User user, UserStatus beforeStatus, UserStatus afterStatus, String reason, User changedBy) {
        this.user = user;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.reason = reason;
        this.changedBy = changedBy;
    }
}
